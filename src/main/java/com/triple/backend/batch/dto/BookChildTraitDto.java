package com.triple.backend.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BookChildTraitDto {
    private Long childId;
    private Long historyId;
    private int[] childTraitsArray;
    private int[] traitsChangeArray;
    private List<int[]> likedBookTraitsList;
    private List<int[]> hatedBookTraitsList;
}
