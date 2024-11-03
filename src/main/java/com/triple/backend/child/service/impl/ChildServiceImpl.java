package com.triple.backend.child.service.impl;

import com.triple.backend.child.dto.*;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.entity.TraitsChange;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.child.repository.TraitsChangeRepository;
import com.triple.backend.child.service.ChildService;
import com.triple.backend.common.config.JWTUtil;
import com.triple.backend.common.exception.NotFoundException;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import com.triple.backend.test.dto.TraitDataResponseDto;
import com.triple.backend.test.entity.Mbti;
import com.triple.backend.test.entity.MbtiType;
import com.triple.backend.test.entity.TestParticipation;
import com.triple.backend.test.entity.Trait;
import com.triple.backend.test.repository.MbtiRepository;
import com.triple.backend.test.repository.TestParticipationRepository;
import com.triple.backend.test.repository.TraitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final MbtiRepository mbtiRepository;
    private final TestParticipationRepository testParticipationRepository;
    private final TraitRepository traitRepository;
    private final TraitsChangeRepository traitsChangeRepository;
    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;

    @Override
    @Transactional
    public void registerChild(ChildRegisterRequestDto request, String accessToken) {
        // 사용자 검증 및 자녀 정보 저장
        Member member = findMemberByToken(accessToken);
        Child savedChild = saveChild(member, request);

        // 초기 MBTI 히스토리 생성
        MbtiHistory savedHistory = saveMbtiHistory(savedChild);

        // 특성 및 변화량 초기화
        initializeTraitsAndChanges(savedChild, savedHistory);
    }

    private Member findMemberByToken(String accessToken) {
        Long memberId = extractMemberIdFromToken(accessToken);
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private Child saveChild(Member member, ChildRegisterRequestDto request) {
        Child child = new Child(
                member,
                request.getName(),
                request.getBirthDate(),
                request.getGender().equals("여") ? "여자" : "남자",
                request.getProfileImage(),
                request.getAge()
        );
        return childRepository.save(child);
    }

    private MbtiHistory saveMbtiHistory(Child child) {
        MbtiHistory initialHistory = MbtiHistory.builder()
                .child(child)
                .currentMbti("ISFP")       // 초기 등록
                .reason("기본")            // 성향 변경 이유
                .reasonId(1L)              // 초기 reasonId 값 설정
                .isDeleted(false)
                .build();
        return mbtiHistoryRepository.save(initialHistory);
    }

    private void initializeTraitsAndChanges(Child child, MbtiHistory history) {
        // 모든 특성 조회
        List<Trait> traits = traitRepository.findAll();
        if (traits.isEmpty()) {
            throw new EntityNotFoundException("특성 정보가 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now();

        List<ChildTraits> childTraitsList = new ArrayList<>();
        List<TraitsChange> traitsChangeList = new ArrayList<>();

        for (Trait trait : traits) {
            // ChildTraits 생성
            ChildTraits childTraits = new ChildTraits(
                    history,
                    trait,
                    50,    // 초기값 50
                    now
            );
            childTraitsList.add(childTraits);

            // TraitsChange 생성
            TraitsChange traitsChange = new TraitsChange(
                    child,
                    trait,
                    0.0    // 초기 변화량 0
            );
            traitsChangeList.add(traitsChange);
        }

        // Batch insert 수행
        childTraitsRepository.saveAll(childTraitsList);
        traitsChangeRepository.saveAll(traitsChangeList);
    }

    // 사용자를 찾아서 자녀삭제
    @Override
    @Transactional
    public boolean deleteChildById(Long childId, String accessToken) {
        Long memberId = extractMemberIdFromToken(accessToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        if (childRepository.existsByChildIdAndMember(childId, member)) {
            childRepository.deleteByChildIdAndMember(childId, member);
            return true;
        }
        return false;
    }

    // JWT 토큰에서 memberId 추출
    private Long extractMemberIdFromToken(String accessToken) {
        long memberId = jwtUtil.getMemberIdFromToken(accessToken);
        return memberId;
    }
    @Override
    public ChildInfoResponseDto getChildInfo(Long childId) {

        // 자녀 정보 조회
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> NotFoundException.entityNotFound("자녀"));

        // 최근 MBTI 히스토리 조회
        MbtiHistory latestHistory = mbtiHistoryRepository.findTopByChild_ChildIdOrderByCreatedAtDesc(childId)
                .orElseThrow(() -> NotFoundException.entityNotFound("최신 히스토리"));

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
                .filter(history -> !history.getReason().equals("010"))
                .map(history -> history.getCreatedAt().toString())
                .collect(Collectors.toList());

        return ChildInfoResponseDto.toDto(child, historyMbti, latestHistory.getReason(), latestHistory.getCurrentMbti(), historyDateList);
    }

    // 자녀 히스토리 조회
    @Override
    public ChildHistoryResponseDto getChildHistory(Long childId, LocalDateTime date) {

        // 자녀 정보 조회
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> NotFoundException.entityNotFound("자녀"));

        // date MBTI 히스토리 조회
        MbtiHistory history = mbtiHistoryRepository.findByChildAndCreatedAt(child, date)
                .orElseThrow(() -> NotFoundException.entityNotFound("히스토리"));

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

    // 자녀 성향 진단 결과 모음 조회
    @Override
    public ChildTestHistoryResponseDto getChildTestHistory(Long childId) {

        // 자녀 정보 모음 전체 조회
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> NotFoundException.entityNotFound("자녀"));

        // 최근 MBTI 히스토리 조회
        MbtiHistory latestHistory = mbtiHistoryRepository.findTopByChildAndReasonAndIsDeletedFalseOrderByCreatedAtDesc(child, "020")
                .orElseThrow(() -> NotFoundException.entityNotFound("최근 히스토리"));


        TestParticipation testParticipation = testParticipationRepository.findTopByChild_ChildIdOrderByCreatedAtDesc(childId);
        Long testId = testParticipation.getTest().getTestId();

        // 최신 히스토리의 성향 리스트에서 최신 성향만 조회
        List<TraitDataResponseDto> traitDataDtoList = childTraitsRepository.findTraitsByChildAndTest(childId, latestHistory.getHistoryId(), testId);

        // MBTI 설명 조회
        Mbti mbti = mbtiRepository.findByName(MbtiType.valueOf(latestHistory.getCurrentMbti()))
                .orElseThrow(() -> NotFoundException.entityNotFound("MBTI"));


        //  날짜 집어넣기 ( 날짜 출력 형식 고민 )
        List<MbtiHistory> historyList = mbtiHistoryRepository.findByChildAndReasonOrderByCreatedAtDesc(child, "020");
        List<String> historyDateList = historyList.stream()
                .filter(history -> !history.isDeleted()) // 논리적 삭제인 경우 제외
                .filter(history -> !history.getReason().equals("010"))
                .map(history -> history.getCreatedAt().toString())
                .collect(Collectors.toList());

        return ChildTestHistoryResponseDto.builder()
                .historyId(latestHistory.getHistoryId())
                .historyMbti(traitDataDtoList)
                .currentMbti(latestHistory.getCurrentMbti())
                .historyDate(historyDateList)
                .mbtiPhrase(mbti.getPhrase())
                .mbtiDescription(mbti.getDescription())
                .mbtiImage(mbti.getImage())
                .build();

    }

    // 자녀 성향 진단 결과 모음 날짜 조회
    @Override
    public ChildTestHistoryDateResponseDto getChildTestHistoryDate(Long childId, LocalDateTime date) {
        // 자녀 정보 모음 전체 조회
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> NotFoundException.entityNotFound("자녀"));

        // date MBTI 히스토리 조회
        MbtiHistory history = mbtiHistoryRepository.findByChildAndCreatedAt(child, date)
                .orElseThrow(() -> NotFoundException.entityNotFound("히스토리"));

        // 최신 히스토리의 성향 리스트에서 최신 성향만 조회
        List<ChildTraits> traitList = childTraitsRepository.findByMbtiHistory_HistoryIdWithTraits(history.getHistoryId());

        Map<String, Integer> historyMbti = new LinkedHashMap<>();
        for (ChildTraits trait : traitList) {
            String traitName = trait.getTrait().getTraitName();
            int traitScore = trait.getTraitScore();
            historyMbti.put(traitName, traitScore);
        }

        // MBTI 설명 조회
        Mbti mbti = mbtiRepository.findByName(MbtiType.valueOf(history.getCurrentMbti()))
                .orElseThrow(() -> NotFoundException.entityNotFound("MBTI"));

        return ChildTestHistoryDateResponseDto.builder()
                .historyId(history.getHistoryId())
                .historyMbti(historyMbti)
                .currentMbti(history.getCurrentMbti())
                .mbtiPhrase(mbti.getPhrase())
                .mbtiDescription(mbti.getDescription())
                .mbtiImage(mbti.getImage())
                .build();
    }



}