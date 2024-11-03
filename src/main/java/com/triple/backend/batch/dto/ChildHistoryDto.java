package com.triple.backend.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChildHistoryDto {
	private Long childId;
	private Long historyId;
	private int[] traitScores;

	public ChildHistoryDto(Long childId, Long historyId, int[] traitScores) {
		this.childId = childId;
		this.historyId = historyId;
		this.traitScores = traitScores;
	}
}
