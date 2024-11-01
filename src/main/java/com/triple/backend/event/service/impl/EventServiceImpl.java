package com.triple.backend.event.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.event.dto.*;
import com.triple.backend.event.entity.Event;
import com.triple.backend.event.entity.EventPart;
import com.triple.backend.event.repository.EventPartRepository;
import com.triple.backend.event.repository.EventRepository;
import com.triple.backend.event.repository.WinningRepository;
import com.triple.backend.event.service.EventService;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private final MemberRepository memberRepository;
    private final EventPartRepository eventPartRepository;
    private final WinningRepository winningRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EventRepository eventRepository;
    private final DefaultRedisScript<Long> eventParticipationScript;  // 추가
    private final ObjectMapper objectMapper;  // 추가

//    private static final String EVENT_PARTICIPANT_KEY = "eventParticipant:";
//    private static final String EVENT_COUNTER_KEY = "eventCounter:";

    private static final String EVENT_PARTICIPANT_KEY = "event:participant:";
    private static final String EVENT_DATA_KEY = "event:data:";
    private static final String EVENT_COUNTER_KEY = "event:counter:";

    private static final String EVENT_START_TIME_KEY = "event:start:";
    private static final String EVENT_END_TIME_KEY = "event:end:";

    @Value("${event.max-participants:100}")
    private int maxParticipants;

    @Override
    public EventResultResponseDto getEventWinner(Long eventId) {
        List<WinnerResponseDto> winnerList = winningRepository.findWinningDataByEventId(eventId);
        return new EventResultResponseDto(winnerList);
    }

    @Override
    @Transactional
    public EventApplyResponseDto insertEventParticipate(Long eventId, Long memberId) {
        try {
            LocalDateTime now = LocalDateTime.now();

            // 1. 동시성을 고려한 참가자 수 확인 및 증가
            String counterKey = EVENT_COUNTER_KEY + eventId;
            Long currentParticipants = redisTemplate.opsForValue().increment(counterKey);

            if (currentParticipants > maxParticipants) {
                redisTemplate.opsForValue().decrement(counterKey);
                return EventApplyResponseDto.failed("이벤트 마감되었습니다");
            }

            // 2. 이벤트 유효성 검증
            Event event = validateEventAndMember(eventId, memberId);

            // 3. 이벤트 시간 검증
            if (!isEventTimeValid(event, now)) {
                redisTemplate.opsForValue().decrement(counterKey);
                return EventApplyResponseDto.failed(getEventTimeErrorMessage(event, now));
            }

            // 4. 중복 참여 검증
            if (isAlreadyParticipated(eventId, memberId)) {
                redisTemplate.opsForValue().decrement(counterKey);
                return EventApplyResponseDto.failed("이미 참여하셨습니다");
            }
            saveEventParticipation(event, memberId);

            return EventApplyResponseDto.success(currentParticipants);

        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("이벤트 참여 중 오류 발생", e);
            throw new RuntimeException("이벤트 참여 처리 중 오류가 발생했습니다");
        }
    }

    private Event validateEventAndMember(Long eventId, Long memberId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> NotFoundException.entityNotFound("이벤트"));

        memberRepository.findById(memberId)
                .orElseThrow(() -> NotFoundException.entityNotFound("회원"));

        return event;
    }

    private boolean isEventTimeValid(Event event, LocalDateTime now) {
        return !now.isBefore(event.getStartTime()) && !now.isAfter(event.getEndTime());
    }

    private String getEventTimeErrorMessage(Event event, LocalDateTime now) {
        if (now.isBefore(event.getStartTime())) {
            return "이벤트가 시작되지 않았습니다.";
        }
        return "이벤트가 종료되었습니다.";
    }

    private boolean isAlreadyParticipated(Long eventId, Long memberId) {
        String participantKey = EVENT_PARTICIPANT_KEY + eventId;
        return redisTemplate.opsForSet().add(participantKey, memberId.toString()) == 0;
    }

    private void saveEventParticipation(Event event, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> NotFoundException.entityNotFound("회원"));

        EventPart eventPart = EventPart.builder()
                .event(event)
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();

        eventPartRepository.save(eventPart);
    }

    @Override
    @Transactional
    public EventApplyResponseDto applyEvent(EventApplyRequestDto request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            request.setCreatedAt(now);

//            log.info("Attempting to execute Lua script for event: {}, member: {}",
//                    request.getEventId(), request.getMemberId());

            // Redis에 저장된 이벤트 시간 정보 확인
//            log.info("Checking stored event times in Redis - Start time: {}, End time: {}",
//                    redisTemplate.opsForValue().get(EVENT_START_TIME_KEY + request.getEventId()),
//                    redisTemplate.opsForValue().get(EVENT_END_TIME_KEY + request.getEventId()));

            String jsonData;
            try {
                jsonData = objectMapper.writeValueAsString(request);
//                log.info("Request converted to JSON: {}", jsonData);
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
                            EVENT_END_TIME_KEY + request.getEventId()
                    ),
                    request.getMemberId().toString(),
                    String.valueOf(now.toEpochSecond(ZoneOffset.of("+09:00"))),
                    jsonData
            );

            log.info("Lua script execution result: {}", result);

            if (result == null) {
                return EventApplyResponseDto.failed("시스템 오류가 발생했습니다.");
            }

            return switch (result.intValue()) {
                case -1 -> EventApplyResponseDto.failed("이미 참여하셨습니다.");
                case -2 -> EventApplyResponseDto.failed("유효하지 않은 이벤트입니다.");
                case -3 -> EventApplyResponseDto.failed("이벤트가 아직 시작되지 않았습니다.");
                case -4 -> EventApplyResponseDto.failed("이벤트가 종료되었습니다.");
                default -> EventApplyResponseDto.success("이벤트 응모가 완료되었습니다.");
            };

        } catch (Exception e) {
            log.error("이벤트 참여 처리 실패. 상세 오류: ", e);
            throw new EventProcessingException("이벤트 참여 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void insertEvent(EventRequestDto eventRequestDto) {

        try {
            log.info("Inserting event with data: {}", eventRequestDto);

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
//            log.info("Event saved with ID: {}", savedEvent.getEventId());

            // Redis에 시간 저장
//            log.info("Saving times to Redis - Start: {}, End: {}",
//                    savedEvent.getStartTime(),
//                    savedEvent.getEndTime());
            saveEventTimeToRedis(savedEvent.getEventId(), savedEvent.getStartTime(), savedEvent.getEndTime());

            // Redis에 저장된 값 확인
//            log.info("Stored times in Redis - Start: {}, End: {}",
//                    redisTemplate.opsForValue().get(EVENT_START_TIME_KEY + savedEvent.getEventId()),
//                    redisTemplate.opsForValue().get(EVENT_END_TIME_KEY + savedEvent.getEventId())
//            );
        } catch (Exception e) {
            log.error("Error saving event times to Redis", e);
            throw e;
        }
    }


    private void saveEventTimeToRedis(Long eventId, LocalDateTime startTime, LocalDateTime endTime) {
//        redisTemplate.opsForValue().set(EVENT_START_TIME_KEY + eventId, startTime.toString());
//        redisTemplate.opsForValue().set(EVENT_END_TIME_KEY + eventId, endTime.toString());
        try {
            String startTimeValue = String.valueOf(startTime.toEpochSecond(ZoneOffset.of("+09:00")));
            String endTimeValue = String.valueOf(endTime.toEpochSecond(ZoneOffset.of("+09:00")));

//            log.info("Saving to Redis - EventId: {}, StartTime: {}, EndTime: {}",
//                    eventId, startTimeValue, endTimeValue);

            boolean startTimeSaved = Boolean.TRUE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(EVENT_START_TIME_KEY + eventId, startTimeValue));

            boolean endTimeSaved = Boolean.TRUE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(EVENT_END_TIME_KEY + eventId, endTimeValue));

            if (!startTimeSaved || !endTimeSaved) {
                log.warn("Failed to save time to Redis - StartTime saved: {}, EndTime saved: {}",
                        startTimeSaved, endTimeSaved);
            }
        } catch (Exception e) {
            log.error("Error in saveEventTimeToRedis - EventId: " + eventId, e);
            throw new RuntimeException("Failed to save event times to Redis", e);
        }
    }

}