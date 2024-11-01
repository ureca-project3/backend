package com.triple.backend.event.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triple.backend.event.dto.*;
import com.triple.backend.event.entity.Event;
import com.triple.backend.event.repository.EventRepository;
import com.triple.backend.event.repository.WinningRepository;
import com.triple.backend.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import com.triple.backend.event.exception.EventProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {
    private final WinningRepository winningRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EventRepository eventRepository;
    private final DefaultRedisScript<Long> eventParticipationScript;  // 추가
    private final ObjectMapper objectMapper;  // 추가
    private static final String EVENT_PARTICIPANT_KEY = "event:participant:";
    private static final String EVENT_DATA_KEY = "event:data:";
    private static final String EVENT_START_TIME_KEY = "event:start:";
    private static final String EVENT_END_TIME_KEY = "event:end:";
    private static final String EVENT_TOTAL_COUNT_KEY = "event:total_count:";

    @Override
    public EventResultResponseDto getEventWinner(Long eventId) {
        List<WinnerResponseDto> winnerList = winningRepository.findWinningDataByEventId(eventId);
        return new EventResultResponseDto(winnerList);
    }

    @Override
    @Transactional
    public void insertEvent(EventRequestDto eventRequestDto) {
        try {
            Event event = Event.builder()
                    .eventName(eventRequestDto.getEventName())
                    .startTime(eventRequestDto.getStartTime())
                    .endTime(eventRequestDto.getEndTime())
                    .winnerCnt(eventRequestDto.getWinnerCnt())
                    .totalCnt(eventRequestDto.getTotalCnt())
                    .announceTime(eventRequestDto.getAnnounceTime())
                    .status(true)
                    .build();
            Event savedEvent = eventRepository.save(event);
            saveEventTimeToRedis(savedEvent.getEventId(), savedEvent.getStartTime(), savedEvent.getEndTime());
        } catch (Exception e) {
            log.error("Error saving event times to Redis", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public EventApplyResponseDto applyEvent(EventApplyRequestDto request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            request.setCreatedAt(now);
            String jsonData;
            try {
                jsonData = objectMapper.writeValueAsString(request);
            } catch (Exception e) {
                log.error("JSON serialization failed", e);
                throw new EventProcessingException("JSON 변환 중 오류 발생");
            }
            // Redis Lua 스크립트 실행
            Long result = redisTemplate.execute(
                    eventParticipationScript,
                    List.of(
                            EVENT_PARTICIPANT_KEY + request.getEventId(),
                            EVENT_DATA_KEY + request.getEventId() + ":" + request.getMemberId(),
                            EVENT_START_TIME_KEY + request.getEventId(),
                            EVENT_END_TIME_KEY + request.getEventId(),
                            EVENT_TOTAL_COUNT_KEY + request.getEventId()
                    ),
                    request.getMemberId().toString(),
                    String.valueOf(now.toEpochSecond(ZoneOffset.of("+09:00"))),
                    jsonData
            );
            if (result == null) {
                return EventApplyResponseDto.failed("시스템 오류가 발생했습니다.");
            }
            return switch (result.intValue()) {
                case -1 -> EventApplyResponseDto.failed("이미 참여하셨습니다.");
                case -2 -> EventApplyResponseDto.failed("유효하지 않은 이벤트입니다.");
                case -3 -> EventApplyResponseDto.failed("이벤트가 아직 시작되지 않았습니다.");
                case -4 -> EventApplyResponseDto.failed("이벤트가 종료되었습니다.");
                default -> EventApplyResponseDto.success("이벤트 응모가 완료되었습니다. 참여 순서: " + result);
            };
        } catch (Exception e) {
            log.error("이벤트 참여 처리 실패. 상세 오류: ", e);
            throw new EventProcessingException("이벤트 참여 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void saveEventTimeToRedis(Long eventId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            String startTimeValue = String.valueOf(startTime.toEpochSecond(ZoneOffset.of("+09:00")));
            String endTimeValue = String.valueOf(endTime.toEpochSecond(ZoneOffset.of("+09:00")));
            boolean startTimeSaved = Boolean.TRUE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(EVENT_START_TIME_KEY + eventId, startTimeValue));
            boolean endTimeSaved = Boolean.TRUE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(EVENT_END_TIME_KEY + eventId, endTimeValue));
            if (!startTimeSaved || !endTimeSaved) {
                log.warn("Failed to save time to Redis - StartTime saved: {}, EndTime saved: {}",
                        startTimeSaved, endTimeSaved);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save event times to Redis", e);
        }
    }
}