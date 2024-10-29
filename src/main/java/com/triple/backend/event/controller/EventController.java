package com.triple.backend.event.controller;

import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.event.dto.EventApplyResponse;
import com.triple.backend.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;

    @GetMapping("/result/{eventId}")
    public ResponseEntity<?> getEvent(@PathVariable(name = "eventId") Long eventId) {
        return CommonResponse.ok("Get eventWinner Success", eventService.getEventWinner(eventId));
    }

    @PostMapping("/participate")
    public ResponseEntity<?> participateInEvent(
            @RequestParam Long eventId,
            @RequestParam Long memberId) {

        EventApplyResponse response = eventService.insertEventParticipate(eventId, memberId);
        return  CommonResponse.ok(response.getMessage(), response);
    }
}