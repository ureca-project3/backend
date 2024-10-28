package com.triple.backend.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class FeedbackAndTraitsDto {
    private Long childId;
    private Long bookId;
    private boolean LikeStatus;
    private boolean HateStatus;

    private List<ChildTraitsDto> childTraits;
    private List<BookTraitsDto> bookTraits;
    private List<TraitsChangeDto> traitsChanges;
}
