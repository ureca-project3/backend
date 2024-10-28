package com.triple.backend.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j(topic = "Batch Scheduler")
@Configuration
@RequiredArgsConstructor
public class BatchScheduler {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Scheduled(cron = "0 0 1 * * *") // 시간 수정 필요. 현재는 매일 자정 1시
    public void syncFeedbackFromRedisToMySQL() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        log.info("피드백 계산 및 MySQL 동기화 배치 스케줄링 시작 Time: {}", start);

        // Spring Batch를 이용하여 syncFeedbackAndUpdateTraitsJob이라는 Job을 실행하는 로직

        /*
        jobRegistry에서 이름이 'syncFeedbackAndUpdateTraitsJob"인 Job 객체를 가져온다.
        여기서 해당 job은 Spring Batch에서 정의된 배치 작업이다.
        JobRegistry는 여러 배치 작업을 등록하고 관리하는 역할이며, 이름을 통해 작업을 찾는다.
         */
        Job job = jobRegistry.getJob("syncFeedbackAndUpdateTraitsJob");

        /*
        JobParametersBuilder를 사용하여 배치 작업에 전달할 파라미터들을 생성한다.

        .addLong()은 현재 시간을 밀리초로 기록한 값을 파라미터로 추가한다. 배치 작업의 중복 실행을 방지한다.
        각 JobInstance는 고유한 JobParameters 조합이 있어야 하므로, 해당 파라미터는 고유한 인스턴스를 만들 수 있게 해 준다.

        .addString()은 현재 날짜를 문자열로 추가한다. 로그성 정보다.
         */
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("date", LocalDate.now().toString())
                .toJobParameters();

        /*
        jobLauncher를 사용하여 Job을 실행한다.
         */
        jobLauncher.run(job, jobParameters);

        LocalDateTime end = LocalDateTime.now();
        log.info("피드백 계산 및 MySQL 동기화 배치 스케줄링 종료 Time: {}, elapsed: {}", end, Duration.between(start, end));
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void recommendBook() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        log.info("책 추천 배치 스케줄링 시작 Time: {}", start);

        Job job = jobRegistry.getJob("recommendBookJob");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("date", LocalDate.now().toString())
                .toJobParameters();

        jobLauncher.run(job, jobParameters);

        LocalDateTime end = LocalDateTime.now();
        log.info("책 추천 배치 스케줄링 종료 Time: {}, elapsed: {}", end, Duration.between(start, end));
    }
}
