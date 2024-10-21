package com.triple.backend.child.service.impl;

import com.triple.backend.child.dto.ChildInfoResponseDto;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.child.service.ChildService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChildServiceImpl implements ChildService {

    private final ChildRepository childRepository;
    private final MbtiHistoryRepository mbtiHistoryRepository;
    private final ChildTraitsRepository childTraitsRepository;

    @Override
    public ChildInfoResponseDto getChildInfo(Long childId) {

        // 자녀 정보 조회
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("자녀 정보를 찾을 수 없습니다."));

        // 최근 MBTI 히스토리 조회
        MbtiHistory latestHistory = mbtiHistoryRepository.findTopByChild_ChildIdOrderByCreatedAtDesc(childId);

        // 최신 히스토리로 성향 리스트 조회
        List<ChildTraits> traitList = childTraitsRepository.findByMbtiHistory_HistoryId(latestHistory.getHistoryId());

        Map<String, Integer> historyMbti = new LinkedHashMap<>();
        for (ChildTraits trait : traitList) {
            String traitName = trait.getTrait().getTraitName();
            int traitScore = trait.getTraitScore();
            historyMbti.put(traitName, traitScore);
        }

        //  날짜 집어넣기 ( 날짜 출력 형식 고민 )
        List<MbtiHistory> historyList = mbtiHistoryRepository.findByChild_ChildIdOrderByCreatedAtDesc(childId);
        List<String> historyDateList = historyList.stream()
                .map(history -> history.getCreatedAt().toString())
                .collect(Collectors.toList());

        return ChildInfoResponseDto.toDto(child, historyMbti, latestHistory.getReason(), latestHistory.getCurrentMbti(), historyDateList);
    }
}