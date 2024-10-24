package com.triple.backend.batch.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BookChildTraitDto {
    private Long childId;
    private Long historyId;
    private int[] childTraitScore;
    private int[] traitsChangeScore;
    private List<int[]> likedBookTraitsList;
    private List<int[]> hatedBookTraitsList;
}
