package com.triple.backend.batch.config;

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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;

import com.triple.backend.book.entity.Book;
import com.triple.backend.book.repository.BookTraitsRepository;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.recbook.entity.RecBook;
import com.triple.backend.recbook.repository.RecBookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RecommendationBatchConfig {

	private final DataSource dataSource;
	private final PlatformTransactionManager platformTransactionManager;
	private final ChildTraitsRepository childTraitsRepository;
	private final BookTraitsRepository bookTraitsRepository;
	private final RecBookRepository recBookRepository;
	private final MbtiHistoryRepository mbtiHistoryRepository;
	private final ChildRepository childRepository;

	/**
	 * Job
	 */
	@Bean
	public Job recommendBookJob(JobRepository jobRepository) {

		// TODO: Skip Exception Handling
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
			.<Long, List<RecBook>>chunk(10, platformTransactionManager)
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
	public JdbcPagingItemReader<Long> recommendBookItemReader() {

		return new JdbcPagingItemReaderBuilder<Long>()
			.name("recommendBookReader")
			.dataSource(dataSource)
			.selectClause("SELECT child_id")
			.fromClause("FROM child")
			.sortKeys(Collections.singletonMap("child_id", Order.ASCENDING))
			.pageSize(10)
			.rowMapper((rs, rowNum) -> rs.getLong("child_id"))
			.build();
	}

	/**
	 * Processor
	 */
	@Bean
	@StepScope
	public ItemProcessor<Long, List<RecBook>> recommendBookItemProcessor() {

		return childId -> {
			MbtiHistory mbtiHistory = mbtiHistoryRepository.findTopByChild_ChildIdOrderByCreatedAtDesc(childId)
				.orElse(null);
			List<ChildTraits> childTraits = childTraitsRepository.findLatestTraitsByHistoryId(mbtiHistory.getHistoryId());

			if (childTraits.isEmpty()) {
				throw NotFoundException.entityNotFound("아이성향");
			}

			int[] childTraitScore = new int[4];

			for (int i = 0; i < 4; i++) {
				childTraitScore[i] = childTraits.get(i).getTraitScore();
			}

			Set<Book> recBooks = new HashSet<>();

			for (int i = 0; i < 4; i++){
				int minScore = Math.max(0, childTraitScore[i] - 10);
				int maxScore = Math.min(100, childTraitScore[i] + 10);

				List<Book> books = bookTraitsRepository.findBooksByTraitScoreBetween(minScore, maxScore, PageRequest.of(0, 15));

				recBooks.addAll(books);
			}

			Optional<Child> child = childRepository.findById(childId);

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
		return recBooks -> {
			for (List<RecBook> recBook : recBooks) {
				recBookRepository.saveAll(recBook);
			}
		};
	}
}
