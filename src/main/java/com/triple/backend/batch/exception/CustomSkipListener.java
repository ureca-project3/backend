package com.triple.backend.batch.exception;

import com.triple.backend.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomSkipListener implements SkipListener<Object, Object> {

    private final String stepName;
    private final NotificationService notificationService;

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("{} 읽기 작업 도중 문제가 발생했습니다. 해당 아이템을 스킵합니다", stepName, t);
        sendSlackAlert(t);
    }

    public void onSkipInWrite(Object item, Throwable t) {
        log.error("{} 쓰기 작업 도중 문제가 발생했습니다. 해당 아이템을 스킵합니다 : {}", stepName, item, t);
        sendSlackAlert(t);
    }

    public void onSkipInProcess(Object item, Throwable t) {
        log.error("{} 프로세스 작업 도중 문제가 발생했습니다. 해당 아이템을 스킵합니다: {}", stepName, item, t);
        sendSlackAlert(t);
    }

    private void sendSlackAlert(Throwable t) {
        try {
            notificationService.sendSlackNotification((Exception) t);
        } catch (IOException e) {
            log.error("Slack 알림 전송 중 오류가 발생했습니다.", e);
        }
    }

}
