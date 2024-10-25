package com.triple.backend.batch.config;

import com.triple.backend.batch.exception.CustomSkipListener;
import com.triple.backend.recbook.entity.RecBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RecommendationBatchConfig {

    @Bean
    public Job bookRecommendationJob(JobRepository jobRepository,
                                              Step processFeedbackStep) {
        return new JobBuilder("bookRecommendationJob", jobRepository)
                .start(processFeedbackStep)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step processFeedbackStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                    @Qualifier("mainDataSource") DataSource dataSource) {
        return new StepBuilder("processFeedbackStep", jobRepository)
                .<RecBook, RecBook>chunk(100, transactionManager)
//                .reader(pagingItemReader(dataSource))
//                .writer(recommendBookWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .listener(new CustomSkipListener("processFeedbackStep"))
                .build();
    }

}
