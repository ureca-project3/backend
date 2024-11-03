package com.triple.backend.batch.config;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.transaction.PlatformTransactionManager;

import com.triple.backend.batch.dto.ChildHistoryDto;
import com.triple.backend.book.entity.Book;
import com.triple.backend.book.repository.BookTraitsRepository;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.recbook.entity.RecBook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RecommendationBatchConfig {

	private final DataSource dataSource;
	private final PlatformTransactionManager platformTransactionManager;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final BookTraitsRepository bookTraitsRepository;
	private final ChildRepository childRepository;

	/**
	 * Job
	 */
	@Bean
	public Job recommendBookJob(JobRepository jobRepository) {

		return new JobBuilder("recommendBookJob", jobRepository)
			.incrementer(new RunIdIncrementer())
			.flow(recommendBookStep(jobRepository))
			.end()
			.build();
	}

	/**
	 * Step
	 */
	@Bean
	@JobScope
	public Step recommendBookStep(JobRepository jobRepository) {

		return new StepBuilder("recommendBookStep", jobRepository)
			.<ChildHistoryDto, List<RecBook>>chunk(100, platformTransactionManager)
			.reader(recommendBookItemReader())
			.processor(recommendBookItemProcessor())
			.writer(recommendBookWriter())
			.build();
	}

	/**
	 * PagingReader (JDBC)
	 */
	@Bean
	@StepScope
	public JdbcPagingItemReader<ChildHistoryDto> recommendBookItemReader() {
		return new JdbcPagingItemReaderBuilder<ChildHistoryDto>()
			.name("recommendBookReader")
			.dataSource(dataSource)
			.selectClause("""
				SELECT c.child_id, mh.history_id AS mbti_history_id,
					   ct1.trait_score AS trait1, ct2.trait_score AS trait2,
					   ct3.trait_score AS trait3, ct4.trait_score AS trait4
        	""")
			.fromClause("""
				FROM child c
				JOIN mbti_history mh ON c.child_id = mh.child_id
				LEFT JOIN child_traits ct1 ON mh.history_id = ct1.history_id AND ct1.trait_id = 1
				LEFT JOIN child_traits ct2 ON mh.history_id = ct2.history_id AND ct2.trait_id = 2
				LEFT JOIN child_traits ct3 ON mh.history_id = ct3.history_id AND ct3.trait_id = 3
				LEFT JOIN child_traits ct4 ON mh.history_id = ct4.history_id AND ct4.trait_id = 4
			""")
			.whereClause("mh.created_at = (SELECT MAX(created_at) FROM mbti_history WHERE child_id = c.child_id)")
			.sortKeys(Collections.singletonMap("c.child_id", Order.ASCENDING))
			.pageSize(100)
			.rowMapper((rs, rowNum) -> new ChildHistoryDto(
				rs.getLong("child_id"),
				rs.getLong("mbti_history_id"),
				new int[]{
					rs.getInt("trait1"),
					rs.getInt("trait2"),
					rs.getInt("trait3"),
					rs.getInt("trait4")
				}
			))
			.build();
	}

	/**
	 * Processor
	 */
	@Bean
	@StepScope
	public ItemProcessor<ChildHistoryDto, List<RecBook>> recommendBookItemProcessor() {
		return childData -> {
			int[] childTraitScore = childData.getTraitScores();
			Set<Book> recBooks = new HashSet<>();

			for (int i = 0; i < 4; i++) {
				int minScore = Math.max(0, childTraitScore[i] - 5);
				int maxScore = Math.min(100, childTraitScore[i] + 5);
				List<Book> books = bookTraitsRepository.findBooksByTraitScoreBetween(minScore, maxScore, PageRequest.of(0, 10));
				recBooks.addAll(books);

				if (recBooks.size() >= 20) {
					break;
				}
			}

			Optional<Child> child = childRepository.findById(childData.getChildId());

			return recBooks.stream()
				.map(book -> RecBook.builder()
					.child(child.get())
					.book(book)
					.build())
				.collect(Collectors.toList());
		};
	}

	/**
	 * Writer
	 */
	@Bean
	public ItemWriter<List<RecBook>> recommendBookWriter() {
		return new ItemWriter<List<RecBook>>() {
			@Override
			public void write(Chunk<? extends List<RecBook>> chunk) throws Exception {
				log.info("mysql 추천책 write 시작");

				LocalDateTime now = LocalDateTime.now();

				// 1. 임시 테이블에 삽입할 파라미터 리스트 생성
				List<SqlParameterSource> batchParams = chunk.getItems().stream()
					.flatMap(List::stream)
					.map(recBook -> new MapSqlParameterSource()
						.addValue("child_id", recBook.getChild().getChildId())
						.addValue("book_id", recBook.getBook().getBookId())
						.addValue("created_at", now)
						.addValue("modified_at", now))
					.collect(Collectors.toList());

				// 2. 임시 테이블에 데이터 배치 삽입
				String dropTempTable = "DROP TABLE IF EXISTS temp_rec_book";
				String createTempTable = """
						CREATE TABLE temp_rec_book (
							child_id BIGINT NOT NULL,
							book_id BIGINT NOT NULL,
							created_at TIMESTAMP NOT NULL,
							modified_at TIMESTAMP NOT NULL,
							PRIMARY KEY (child_id, book_id)
						) ENGINE=InnoDB
					""";
				namedParameterJdbcTemplate.getJdbcTemplate().execute(dropTempTable);
				namedParameterJdbcTemplate.getJdbcTemplate().execute(createTempTable);

				String tempInsertQuery = """
						INSERT INTO temp_rec_book (child_id, book_id, created_at, modified_at)
						VALUES (:child_id, :book_id, :created_at, :modified_at)
					""";
				namedParameterJdbcTemplate.batchUpdate(tempInsertQuery, batchParams.toArray(new SqlParameterSource[0]));

				// 3. 중복 데이터에 대해 rec_book 테이블을 UPDATE
				String updateQuery = """
						UPDATE rec_book AS r
						JOIN temp_rec_book AS t
						ON r.child_id = t.child_id AND r.book_id = t.book_id
						SET r.modified_at = t.modified_at
					""";
				namedParameterJdbcTemplate.getJdbcTemplate().execute(updateQuery);

				// 4. 새 데이터에 대해 rec_book 테이블에 INSERT
				String insertQuery = """
						INSERT INTO rec_book (child_id, book_id, created_at, modified_at)
						SELECT t.child_id, t.book_id, t.created_at, t.modified_at
						FROM temp_rec_book t
						LEFT JOIN rec_book r
						ON t.child_id = r.child_id AND t.book_id = r.book_id
						WHERE r.child_id IS NULL
					""";
				namedParameterJdbcTemplate.getJdbcTemplate().execute(insertQuery);

				log.info("mysql 추천책 write 종료");

				// 5. 임시 테이블 데이터 삭제
				namedParameterJdbcTemplate.getJdbcTemplate().execute("TRUNCATE TABLE temp_rec_book");
			}
		};
	}
}