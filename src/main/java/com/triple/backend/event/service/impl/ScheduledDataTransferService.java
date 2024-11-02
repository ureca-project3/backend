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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

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

    // Redis에서 참여자 데이터를 가져오는 메소드
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

        // createdAt을 기준으로 정렬
        participants.sort(Comparator.comparing(EventPartRequestDto::getCreatedAt));

        return participants;
    }

    // Redis에 있는 데이터를 MySQL에 저장하는 스케줄링 메서드
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void saveEventParticipantsToDatabase() {
        Set<String> eventIds = redisTemplate.keys("event:participant:*");
        if (eventIds == null || eventIds.isEmpty()) return;

        for (String eventKey : eventIds) {
            Long eventId = Long.parseLong(eventKey.split(":")[2]);

            // 종료된 이벤트인지 확인
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> NotFoundException.entityNotFound("이벤트"));
            if (event.getEndTime().isAfter(LocalDateTime.now())) continue;
//            if (event.getEndTime().isBefore(LocalDateTime.now())) continue;

            // Redis에서 event:total_count 값 가져오기
            String totalCountStr = redisTemplate.opsForValue().get("event:total_count:" + eventId);
            Long totalCount = totalCountStr != null ? Long.parseLong(totalCountStr) : 0L;

            event.updateTotalCnt(totalCount);
            Event updateEvent = eventRepository.save(event);

            List<EventPartRequestDto> participants = getEventParticipants(eventId);
            AtomicLong winnerCount = new AtomicLong(Optional.ofNullable(event.getWinnerCnt()).orElse(0L));

            participants.forEach(dto -> {
                // EventPart 저장
                Member member = memberRepository.findById(dto.getMemberId())
                        .orElseThrow(() -> NotFoundException.entityNotFound("회원"));

                EventPart eventPart = EventPart.builder()
                        .member(member)
                        .event(updateEvent)
                        .createdAt(dto.getCreatedAt())
                        .name(dto.getName())
                        .phone(dto.getPhone())
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

//                // Winning 저장
//                // 이름, 연락처 검증 로직 추가
//                if(winnerCount.get() > 0 && dto.getName().equals(member.getName()) && dto.getPhone().equals(member.getPhone())) {
//                    Winning winning = new Winning(eventPart);
//                    winningRepository.save(winning);
//                    winnerCount.decrementAndGet(); // winnerCount 감소
//                }

                // Winning 저장 부분 수정
                if(winnerCount.get() > 0 && isValidParticipant(dto, member)) {
                    Winning winning = new Winning(eventPart);
                    winningRepository.save(winning);
                    winnerCount.decrementAndGet(); // winnerCount 감소
                }

            });

            // 저장 후 Redis 데이터 삭제
            clearEventParticipants(eventId);
        }
    }

    // 특정 이벤트 ID에 대한 Redis 데이터를 삭제하는 메서드
    public void clearEventParticipants(Long eventId) {
        Set<String> memberIds = redisTemplate.opsForSet().members("event:participant:" + eventId);
        if (memberIds != null) {
            memberIds.forEach(memberId -> redisTemplate.delete("event:data:" + eventId + ":" + memberId));
        }
        redisTemplate.delete("event:participant:" + eventId);
    }

    // 참가자 정보 유효성 검증을 위한 메서드 추가
    private boolean isValidParticipant(EventPartRequestDto dto, Member member) {
        // dto나 member가 null인 경우 체크
        if (dto == null || member == null) return false;

        // 이름과 전화번호가 모두 존재하고 일치하는지 확인
        String dtoName = dto.getName();
        String dtoPhone = dto.getPhone();
        String memberName = member.getName();
        String memberPhone = member.getPhone();

        return dtoName != null && dtoPhone != null
                && memberName != null && memberPhone != null
                && dtoName.equals(memberName)
                && dtoPhone.equals(memberPhone);
    }

}
