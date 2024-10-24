package com.triple.backend.child.service.impl;

import com.triple.backend.child.dto.ChildHistoryResponseDto;
import com.triple.backend.child.dto.ChildInfoResponseDto;
import com.triple.backend.child.dto.ChildTestHistoryResponseDto;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.child.service.ChildService;
import com.triple.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
                .orElseThrow(() -> NotFoundException.entityNotFound("자녀"));

        // 최근 MBTI 히스토리 조회
        MbtiHistory latestHistory = mbtiHistoryRepository.findTopByChild_ChildIdOrderByCreatedAtDesc(childId);

        // 최신 히스토리의 성향 리스트에서 최신 성향만 조회
        List<ChildTraits> traitList = childTraitsRepository.findLatestByMbtiHistory_HistoryIdWithTraits(latestHistory.getHistoryId());

        Map<String, Integer> historyMbti = new LinkedHashMap<>();
        for (ChildTraits trait : traitList) {
            String traitName = trait.getTrait().getTraitName();
            int traitScore = trait.getTraitScore();
            historyMbti.put(traitName, traitScore);
        }

        //  날짜 집어넣기 ( 날짜 출력 형식 고민 )
        List<MbtiHistory> historyList = mbtiHistoryRepository.findByChild_ChildIdOrderByCreatedAtDesc(childId);
        List<String> historyDateList = historyList.stream()
                .filter(history -> !history.isDeleted()) // 논리적 삭제인 경우 제외
                .filter(history -> !history.getReason().equals("기본"))
                .map(history -> history.getCreatedAt().toString())
                .collect(Collectors.toList());

        return ChildInfoResponseDto.toDto(child, historyMbti, latestHistory.getReason(), latestHistory.getCurrentMbti(), historyDateList);
    }

    // 자녀 히스토리 조회
    @Override
    public ChildHistoryResponseDto getChildHistory(Long childId, String date) {

        // 자녀 정보 조회
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> NotFoundException.entityNotFound("자녀"));

        // date type을 String -> LocalDateTime 으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        LocalDateTime dateTime = LocalDateTime.parse(date, formatter);

        // date MBTI 히스토리 조회
        MbtiHistory history = mbtiHistoryRepository.findByChildAndCreatedAt(child, dateTime);

        // 최신 히스토리로 성향 리스트 조회
        List<ChildTraits> traitList = childTraitsRepository.findLatestByMbtiHistory_HistoryIdWithTraits(history.getHistoryId());

        Map<String, Integer> historyMbti = new LinkedHashMap<>();
        for (ChildTraits trait : traitList) {
            String traitName = trait.getTrait().getTraitName();
            int traitScore = trait.getTraitScore();
            historyMbti.put(traitName, traitScore);
        }

        return new ChildHistoryResponseDto(historyMbti, history.getReason(), history.getCurrentMbti());
    }

    // 자녀 성향 진단 결과 조회
    @Override
    public ChildTestHistoryResponseDto getChildTestHistory(Long childId) {

        List<MbtiHistory> historyList = mbtiHistoryRepository.findResultsByChildId(childId);

        List<String> currentMbti = new ArrayList<>();
        List<String> createdAt = new ArrayList<>();

        for (MbtiHistory history : historyList) {
            currentMbti.add(history.getCurrentMbti());
            createdAt.add(history.getCreatedAt().toString());
        }

        Map<String, String> traitData = new LinkedHashMap<>();
        for (MbtiHistory history : historyList) {
            List<ChildTraits> traits = childTraitsRepository.findLatestByMbtiHistory_HistoryIdWithTraits(history.getHistoryId());

            for (ChildTraits ct : traits) {
                traitData.put(ct.getTrait().getTraitName(), ct.getTrait().getTraitDescription());
            }
        }
        return new ChildTestHistoryResponseDto(currentMbti, createdAt, traitData);
    }
}