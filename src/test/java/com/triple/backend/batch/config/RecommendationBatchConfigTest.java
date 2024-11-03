package com.triple.backend.batch.config;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;

import com.triple.backend.batch.dto.ChildHistoryDto;
import com.triple.backend.book.entity.Book;
import com.triple.backend.book.repository.BookTraitsRepository;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.recbook.entity.RecBook;

@ExtendWith(MockitoExtension.class)
@SpringBatchTest
@SpringJUnitConfig({RecommendationBatchConfig.class, DataSourceConfig.class})
class RecommendationBatchConfigTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private Job recommendBookJob;

	@Autowired
	private JdbcPagingItemReader<ChildHistoryDto> recommendBookItemReader;

	@Autowired
	private ItemProcessor<ChildHistoryDto, List<RecBook>> recommendBookItemProcessor;

	@Autowired
	private ItemWriter<List<RecBook>> recommendBookWriter;

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Mock
	private JdbcTemplate jdbcTemplate;

	@MockBean
	private BookTraitsRepository bookTraitsRepository;

	@MockBean
	private ChildRepository childRepository;

	@BeforeEach
	void setUp() {
		jobLauncherTestUtils.setJob(recommendBookJob);
	}

	@Nested
	@DisplayName("recommendBookStep")
	class recommendBookJob {

		@DisplayName("정상적으로 추천책 배치 작업이 실행된다.")
		@Test
		void job_success () throws Exception {
			// given -- 테스트의 상태 설정
			JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParameters();

			// when -- 테스트하고자 하는 행동
			JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

			// then -- 예상되는 변화 및 결과
			assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		}
	}

	@Nested
	@DisplayName("recommendBookStep")
	class recommendBookStep {

		@DisplayName("recommendBookJob 이 실행되면 정상적으로 스탭이 실행된다.")
		@Test
		void step_success() throws Exception {
			// given -- 테스트의 상태 설정
			JobExecution jobExecution = jobLauncherTestUtils.launchStep("recommendBookStep");

			// when -- 테스트하고자 하는 행동

			// then -- 예상되는 변화 및 결과
			assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		}
	}

	@Nested
	@DisplayName("recommendBookItemReader")
	class recommendBookItemReader {

		@DisplayName("JdbcPagingItemReader 가 데이터를 성공적으로 읽어온다.")
		@Test
		void read_success() throws Exception {
			// given -- 테스트의 상태 설정
			JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParameters();
			jobLauncherTestUtils.getJobRepository().createJobExecution("recommendBookJob", jobParameters);

			ExecutionContext executionContext = new ExecutionContext();
			recommendBookItemReader.open(executionContext);

			// when -- 테스트하고자 하는 행동
			ChildHistoryDto result = recommendBookItemReader.read();

			// then -- 예상되는 변화 및 결과
			assertSoftly(softAssertions -> {
				softAssertions.assertThat(result).isNotNull();
				softAssertions.assertThat(result.getChildId()).isNotZero();
				softAssertions.assertThat(result.getHistoryId()).isNotZero();
				softAssertions.assertThat(result.getTraitScores()).hasSize(4);
			});
		}

		@DisplayName("최근 히스토리에 해당하는 아이 성향 4개를 pageSize 만큼 추출한다.")
		@Test
		void read_pageSize_success() throws Exception {
			// given -- 테스트의 상태 설정
			JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParameters();
			jobLauncherTestUtils.getJobRepository().createJobExecution("recommendBookJob", jobParameters);

			ExecutionContext executionContext = new ExecutionContext();
			recommendBookItemReader.open(executionContext);

			int pageSize = 100;
			int itemCount = 0;
			ChildHistoryDto item;

			// when -- 테스트하고자 하는 행동
			while ((item = recommendBookItemReader.read()) != null) {
				itemCount++;
				assertThat(item).isNotNull();
				assertThat(item.getChildId()).isNotZero();
				assertThat(item.getHistoryId()).isNotZero();
				assertThat(item.getTraitScores()).hasSize(4);
			}

			// then -- 예상되는 변화 및 결과
			assertThat(itemCount).isGreaterThan(pageSize);
		}
	}

	@Nested
	@DisplayName("recommendBookItemProcessor")
	class recommendBookItemProcessor {

		@DisplayName("아이 성향 ±5 범위에 해당하는 추천책 리스트를 생성한다.")
		@Test
		void processor_success () throws Exception {
			// given -- 테스트의 상태 설정
			int[] childScores = new int[]{45, 55, 65, 75};
			ChildHistoryDto item = new ChildHistoryDto(1L, 1L, childScores);
			Child child = mock(Child.class);

			List<Book> books = List.of(
				new Book("Book A", "Author A", "Publisher A", "4-7", "Summary of Book A", "010", "http://example.com/imageA.jpg", "2022-01-01"),
				new Book("Book B", "Author B", "Publisher B", "0-3", "Summary of Book B", "020", "http://example.com/imageB.jpg", "2023-05-15")
			);

			given(bookTraitsRepository.findBooksByTraitScoreBetween(anyInt(), anyInt(), any(Pageable.class))).willReturn(books);
			given(childRepository.findById(anyLong())).willReturn(Optional.of(child));

			// when -- 테스트하고자 하는 행동
			List<RecBook> result = recommendBookItemProcessor.process(item);

			// then -- 예상되는 변화 및 결과
			assertSoftly(softAssertions -> {
				softAssertions.assertThat(result).isNotNull();
				softAssertions.assertThat(result).hasSize(2);
				softAssertions.assertThat(result.get(0).getChild()).isEqualTo(child);
				softAssertions.assertThat(result)
					.extracting(RecBook::getBook)
					.extracting(Book::getTitle)
					.contains("Book A", "Book B");
			});
		}
	}

	// @Nested
	// @DisplayName("recommendBookWriter")
	// class recommendBookWriter {
	//
	// 	@DisplayName("추천책 데이터를 MySQL 에 성공적으로 저장한다.")
	// 	@Test
	// 	void write_success() throws Exception {
	// 		// given -- 테스트의 상태 설정
	// 		// given(namedParameterJdbcTemplate.getJdbcTemplate()).willReturn(jdbcTemplate);
	//
	// 		Child child = mock(Child.class);
	// 		Book book = mock(Book.class);
	//
	// 		given(child.getChildId()).willReturn(1L);
	// 		given(book.getBookId()).willReturn(1L);
	//
	// 		RecBook recBook = new RecBook(child, book);
	//
	// 		// SqlParameterSource 배열 생성
	// 		SqlParameterSource[] batchParams = new SqlParameterSource[] {
	// 			new MapSqlParameterSource()
	// 				.addValue("childId", recBook.getChild().getChildId())
	// 				.addValue("bookId", recBook.getBook().getBookId())
	// 		};
	//
	// 		Chunk<List<RecBook>> chunk = new Chunk<>(List.of(List.of(recBook)));
	//
	// 		// when -- 테스트하고자 하는 행동
	// 		recommendBookWriter.write(chunk);
	//
	// 		// then -- 예상되는 변화 및 결과
	// 		verify(namedParameterJdbcTemplate).batchUpdate(anyString(), eq(batchParams));
	// 		// verify(namedParameterJdbcTemplate).batchUpdate(anyString(), any(SqlParameterSource[].class));
	// 	}
	// }
}