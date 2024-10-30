package com.triple.backend.event.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.event.dto.EventPartRequestDto;
import com.triple.backend.event.entity.*;
import com.triple.backend.event.repository.*;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduledDataTransferService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private final EventPartRepository eventPartRepository;
    private final EventAnswerRepository eventAnswerRepository;
    private final WinningRepository winningRepository;
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final EventQuestionRepository eventQuestionRepository;

    // Redis에서 참여자 데이터를 가져오는 메서드
    public List<EventPartRequestDto> getEventParticipants(Long eventId) {
        Set<String> memberIds = redisTemplate.opsForSet().members("event:participant:" + eventId);
        List<EventPartRequestDto> participants = new ArrayList<>();

        if (memberIds != null) {
            for (String memberId : memberIds) {
                String jsonData = redisTemplate.opsForValue().get("event:data:" + eventId + ":" + memberId);
                if (jsonData != null) {
                    try {
                        EventPartRequestDto participant = objectMapper.readValue(jsonData, EventPartRequestDto.class);
                        participants.add(participant);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return participants;
    }

    // Redis에 있는 데이터를 MySQL에 저장하는 스케줄링 메서드
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void saveEventParticipantsToDatabase() {
        Set<String> eventIds = redisTemplate.keys("event:participant:*");
        if (eventIds == null || eventIds.isEmpty()) return;

        eventIds.forEach(eventKey -> {
            Long eventId = Long.parseLong(eventKey.split(":")[2]);
            List<EventPartRequestDto> participants = getEventParticipants(eventId);

            participants.forEach(dto -> {
                // EventPart 저장
                Member member = memberRepository.findById(dto.getMemberId())
                        .orElseThrow(() -> NotFoundException.entityNotFound("회원"));
                Event event = eventRepository.findById(dto.getEventId())
                        .orElseThrow(() -> NotFoundException.entityNotFound("이벤트"));

                EventPart eventPart = EventPart.builder()
                        .member(member)
                        .event(event)
                        .createdAt(dto.getCreatedAt())
                        .build();
                eventPart = eventPartRepository.save(eventPart);

                // EventAnswer 저장
                dto.getAnswerList().forEach((questionIdStr, answerText) -> {
                    Long questionId = Long.parseLong(questionIdStr);

                    EventQuestion eventQuestion = eventQuestionRepository.findById(questionId)
                            .orElseThrow(() -> NotFoundException.entityNotFound("이벤트 질문"));

                    EventAnswerId eventAnswerId = new EventAnswerId(member, eventQuestion);
                    EventAnswer eventAnswer = new EventAnswer(eventAnswerId, answerText.toString());

                    eventAnswerRepository.save(eventAnswer);
                });

                // Winning 저장
                Winning winning = new Winning(eventPart);
                winningRepository.save(winning);
            });

            // 저장 후 Redis 데이터 삭제
            clearEventParticipants(eventId);
        });
    }

    // 특정 이벤트 ID에 대한 Redis 데이터를 삭제하는 메서드
    public void clearEventParticipants(Long eventId) {
        Set<String> memberIds = redisTemplate.opsForSet().members("event:participant:" + eventId);
        if (memberIds != null) {
            memberIds.forEach(memberId -> redisTemplate.delete("event:data:" + eventId + ":" + memberId));
        }
        redisTemplate.delete("event:participant:" + eventId);
    }

}
