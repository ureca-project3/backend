package com.triple.backend.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FeedbackDto {
    private Long childId;
    private Long bookId;
    private boolean likeStatus;
    private boolean hateStatus;
}
