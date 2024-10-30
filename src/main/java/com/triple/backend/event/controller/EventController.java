package com.triple.backend.event.controller;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.common.dto.ErrorResponse;
import com.triple.backend.event.dto.EventApplyRequestDto;
import com.triple.backend.event.dto.EventApplyResponseDto;
import com.triple.backend.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindException;
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

        EventApplyResponseDto response = eventService.insertEventParticipate(eventId, memberId);
        return  CommonResponse.ok(response.getMessage(), response);
    }

    // 이벤트 응모 - lua로 동시성 해결
    @PostMapping("/apply")
    public ResponseEntity<?> applyEvent(@Valid @RequestBody EventApplyRequestDto request, @AuthenticationPrincipal CustomMemberDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        request.setMemberId(memberId);

        EventApplyResponseDto response = eventService.applyEvent(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(HttpStatus.BAD_REQUEST, response.getMessage()));
        }

        return CommonResponse.ok(response.getMessage(), response);
    }

    // Validation 실패 시 처리
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult()
                .getAllErrors()
                .get(0)
                .getDefaultMessage();

        return ResponseEntity.badRequest()
                .body(ErrorResponse.error(HttpStatus.BAD_REQUEST, errorMessage));
    }
}