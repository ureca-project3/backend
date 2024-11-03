package com.triple.backend.notification;

import com.slack.api.Slack;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.webhook.WebhookPayloads;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    private final StringBuilder sb = new StringBuilder();

    // Slack 알림 전송
    public void sendSlackNotification(Exception error, HttpServletRequest request) throws IOException {

        // 메시지 내용 생성
        List<LayoutBlock> layoutBlocks = generateLayoutBlock(error, request);

        // Slack API 호출하여 메시지 전송
        Slack.getInstance().send(webhookUrl, WebhookPayloads
                .payload(p -> p.username("Exception detected 🚨")
                        .iconUrl("https://yt3.googleusercontent.com/ytc/AGIKgqMVUzRrhoo1gDQcqvPo0PxaJz7e0gqDXT0D78R5VQ=s900-c-k-c0x00ffffff-no-rj")
                        .blocks(layoutBlocks)));
    }

    // Slack 배치용 알림 전송
    public void sendSlackNotification(Exception error) throws IOException {

        System.out.println("slack 배치용 알림 전송");

        // 메시지 내용 생성
        List<LayoutBlock> layoutBlocks = generateLayoutBlockForBatch(error);

        // Slack API 호출하여 메시지 전송
        Slack.getInstance().send(webhookUrl, WebhookPayloads
                .payload(p -> p.username("Exception detected 🚨")
                        .iconUrl("https://yt3.googleusercontent.com/ytc/AGIKgqMVUzRrhoo1gDQcqvPo0PxaJz7e0gqDXT0D78R5VQ=s900-c-k-c0x00ffffff-no-rj")
                        .blocks(layoutBlocks)));
    }

    // LayoutBlock 생성
    private List<LayoutBlock> generateLayoutBlock(Exception error, HttpServletRequest request) {
        return Blocks.asBlocks(
                getHeader("서버 측 오류로 예상되는 예외 상황이 발생하였습니다."),
                Blocks.divider(),
                getSection(generateErrorMessage(error)),
                Blocks.divider(),
                getSection(generateErrorPointMessage(request))
        );
    }

    private List<LayoutBlock> generateLayoutBlockForBatch(Exception error) {
        return Blocks.asBlocks(
                getHeader("서버 측 오류로 예상되는 예외 상황이 발생하였습니다."),
                Blocks.divider(),
                getSection(generateErrorMessage(error))
        );
    }

    // 예외 메시지 생성
    private String generateErrorMessage(Exception error) {
        sb.setLength(0);
        sb.append("*[🔥 Exception]*\n" + error.toString() + "\n\n");
        sb.append("*[📩 From]*\n" + readRootStackTrace(error) + "\n\n");
        return sb.toString();
    }

    // 요청 정보 메시지 생성
    private String generateErrorPointMessage(HttpServletRequest request) {
        sb.setLength(0);
        sb.append("*[🧾세부정보]*\n");
        sb.append("Request URL: " + request.getRequestURL().toString() + "\n");
        sb.append("Request Method: " + request.getMethod() + "\n");
        sb.append("Request Time: " + new Date() + "\n");
        return sb.toString();
    }

    // 예외 발생 위치 반환
    private String readRootStackTrace(Exception error) {
        return error.getStackTrace()[0].toString();
    }

    // Slack 메시지의 제목 생성
    private LayoutBlock getHeader(String text) {
        return Blocks.header(h -> h.text(
                BlockCompositions.plainText(pt -> pt.emoji(true).text(text))));
    }

    // Slack 메시지 섹션 생성
    private LayoutBlock getSection(String message) {
        return Blocks.section(s -> s.text(
                BlockCompositions.markdownText(message)));
    }
}
