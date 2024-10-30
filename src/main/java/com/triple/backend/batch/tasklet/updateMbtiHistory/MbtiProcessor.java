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
            return null;
        }

        Long childId = items.get(0).getChildId();
        String currentMbti = items.get(0).getCurrentMbti();

        // 새로운 MBTI 계산
        String newMbti = MbtiCalculator.calculateNewMbti(items);

        // MBTI가 변경된 경우에만 MbtiDto 생성
        if (!newMbti.equalsIgnoreCase(currentMbti)) {
            MbtiDto mbtiDto = new MbtiDto();
            mbtiDto.setChildId(childId);
            mbtiDto.setCurrentMbti(newMbti);
            mbtiDto.setChangeReason("030");
            mbtiDto.setChangeReasonId(items.stream()
                    .max(Comparator.comparing(MbtiWithTraitScoreDto::getCreatedAt))
                    .get().getHistoryId()); // 변경원인에는 바뀌기 직전 history_id를 넣는다
            mbtiDto.setIsDeleted(false);
            return mbtiDto;
        }

        // MBTI가 변경되지 않은 경우 null을 반환한다
        return null;
    }

}
