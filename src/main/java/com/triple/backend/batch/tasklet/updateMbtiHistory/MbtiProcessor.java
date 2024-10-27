package com.triple.backend.batch.tasklet.updateMbtiHistory;

import com.triple.backend.batch.dto.MbtiWithTraitScoreDto;
import com.triple.backend.batch.dto.MbtiDto;
import com.triple.backend.common.utils.MbtiCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Component
public class MbtiProcessor implements ItemProcessor<List<MbtiWithTraitScoreDto>, MbtiDto>  {
    @Override
    public MbtiDto process(List<MbtiWithTraitScoreDto> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return null; // 데이터가 없으면 null 반환
        }

        Long childId = items.get(0).getChildId();
        String currentMbti = items.get(0).getCurrentMbti();

        // 새로운 MBTI 계산
        String newMbti = MbtiCalculator.calculateNewMbti(items);

        // MBTI가 변경된 경우에만 MbtiDto 생성
        if (!newMbti.equals(currentMbti)) {
            MbtiDto mbtiDto = new MbtiDto();
            mbtiDto.setChildId(childId);
            mbtiDto.setCurrentMbti(newMbti);
            mbtiDto.setChangeReason("020"); // 변경 원인 코드
            mbtiDto.setChangeReasonId(items.stream()
                    .max(Comparator.comparing(MbtiWithTraitScoreDto::getCreatedAt))
                    .get().getHistoryId()); // 최신 history_id 사용
            mbtiDto.setIsDeleted(false);
            return mbtiDto;
        }

        return null; // MBTI가 변경되지 않은 경우 null 반환
    }

}
