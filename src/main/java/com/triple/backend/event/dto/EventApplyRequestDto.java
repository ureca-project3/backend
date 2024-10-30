package com.triple.backend.event.dto;

import lombok.AccessLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventApplyRequestDto {
    @NotNull(message = "이벤트 ID는 필수입니다.")
    private Long eventId;

    private Long memberId;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phone;

    private LocalDateTime createAt;

    @NotNull(message = "설문 응답은 필수입니다.")
    private Map<Long, String> answerList = new LinkedHashMap<>();


}