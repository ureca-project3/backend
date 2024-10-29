package com.triple.backend.batch.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;

@Slf4j
public class CustomSkipListener implements SkipListener<Object, Object> {

    String stepName;

    public CustomSkipListener(String stepName) {
        this.stepName = stepName;
    }

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("{} 읽기 작업 도중 문제가 발생했습니다. 해당 아이템을 스킵합니다", stepName, t);
    }

    public void onSkipInWrite(Object item, Throwable t) {
        log.error("{} 쓰기 작업 도중 문제가 발생했습니다. 해당 아이템을 스킵합니다 : {}", stepName, item, t);
    }

    public void onSkipInProcess(Object item, Throwable t) {
        log.error("{} 프로세스 작업 도중 문제가 발생했습니다. 해당 아이템을 스킵합니다: {}", stepName, item, t);
    }
}
