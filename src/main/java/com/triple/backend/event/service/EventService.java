package com.triple.backend.event.service;

import com.triple.backend.event.dto.EventApplyResponse;
import com.triple.backend.event.dto.EventResultResponseDto;

import com.triple.backend.event.dto.EventResponseDto;

public interface EventService {

    /**
     * 이벤트 참여 신청을 처리합니다.
     *
     * @param eventId 이벤트 ID
     * @param memberId 사용자 ID
     * @return 이벤트 참여 결과
     */
    EventApplyResponse insertEventParticipate(Long eventId, Long memberId);
    /**
     * 이벤트 결과를 처리하고 당첨자를 선정합니다.
     * 매일 자정에 실행됩니다.
     */
//    void batchSaveEventParticipants(Long eventId);
    EventResultResponseDto getEventWinner(Long eventId);

    // 선착순 응모 이벤트 조회
    EventResponseDto getEvent(Long eventId);

}
