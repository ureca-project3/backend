package com.triple.backend.event.controller;

import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.event.dto.EventApplyResponse;
import com.triple.backend.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;

    // 이벤트 결과 페이지 접속 api
    @GetMapping("/result/{eventId}")
    public ResponseEntity<?> getEventWinner(@PathVariable(name = "eventId") Long eventId) {
        return CommonResponse.ok("Get eventWinner Success", eventService.getEventWinner(eventId));
    }

    // 이벤트 페이지 접속 api
    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEvent(@PathVariable(name = "eventId") Long eventId) {
        return CommonResponse.ok("Get event Success", eventService.getEvent(eventId));
    }

    // 이벤트 응모 제출 api
    @PostMapping("/participate")
    public ResponseEntity<?> participateInEvent(
            @RequestParam Long eventId,
            @RequestParam Long memberId) {

        EventApplyResponse response = eventService.insertEventParticipate(eventId, memberId);
        return  CommonResponse.ok(response.getMessage(), response);
    }
}