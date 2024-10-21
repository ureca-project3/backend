package com.triple.backend.test.dto;

import com.triple.backend.child.entity.Child;
import com.triple.backend.test.entity.Test;
import com.triple.backend.test.entity.TestParticipation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TestParticipationRequestDto {

    private Long testId;
    private Long childId;

    public TestParticipationRequestDto(Long testId, Long childId) {
        this.testId = testId;
        this.childId = childId;
    }

    public TestParticipation toEntity(Test test, Child child) {
        return TestParticipation.builder()
                .test(test)
                .child(child)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
