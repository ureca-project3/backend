package com.triple.backend.batch.tasklet;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.triple.backend.batch.dto.BookChildTraitDto;
import com.triple.backend.batch.dto.UpdateTraitChangeDto;
import com.triple.backend.child.entity.Child;
import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.entity.MbtiHistory;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.child.repository.MbtiHistoryRepository;
import com.triple.backend.common.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedbackProcessor implements ItemProcessor<BookChildTraitDto, UpdateTraitChangeDto> {

    private final ChildRepository childRepository;
    private final ChildTraitsRepository childTraitsRepository;
    private final MbtiHistoryRepository mbtiHistoryRepository;
    private static final int BASE_CHANGE_AMOUNT = 2;
    private static final int INCREASE_AMOUNT = 1;
    private static final int DECREASE_AMOUNT = 1;

    @Override
    public UpdateTraitChangeDto process(BookChildTraitDto bookChildTraitDto) throws Exception {
        List<int[]> likedBookTraitsList = bookChildTraitDto.getLikedBookTraitsList();
        List<int[]> hatedBookTraitsList = bookChildTraitDto.getHatedBookTraitsList();

        int[] bookTrait = calculateBookTraits(likedBookTraitsList, hatedBookTraitsList);
        int[] childTrait = bookChildTraitDto.getChildTraitScore();

        ChildTraits traits = childTraitsRepository.findChildTraitsByChildId(bookChildTraitDto.getChildId())
            .orElseThrow(() -> NotFoundException.entityNotFound("아이 성향"));

        int[] traitAmount = bookChildTraitDto.getTraitsAmount();
        int[] changeTraitScore = bookChildTraitDto.getChildTraitScore();
        boolean isTraitChanged = false;

        for (int i = 0; i < 4; i++) {
            int changeAmount = calculateTraitChange(bookTrait[i], childTrait[i], traitAmount[i]);

            changeTraitScore[i] += changeAmount;

            if (Math.abs(changeAmount) >= 5) {
                if ((changeTraitScore[i] >= 50 && childTrait[i] < 50) || (changeTraitScore[i] < 50 && childTrait[i] >= 50)) {
                    isTraitChanged = true;
                }
            }
        }

        if (isTraitChanged) {
            // 히스토리에 기록 남기기
            Child child = childRepository.findById(bookChildTraitDto.getChildId())
                .orElseThrow(() -> NotFoundException.entityNotFound("아이"));

            String currentMbti = calculateNewMbti(changeTraitScore);

            MbtiHistory mbtiHistory = MbtiHistory.builder()
                .child(child)
                .currentMbti(currentMbti)
                .reason("020")     // 피드백으로 인한 코드?
                .isDeleted(false)
                .build();

            mbtiHistoryRepository.save(mbtiHistory);

            log.info("히스토리에 바뀐 {} MBTI 추가", currentMbti);

            // 자녀 바뀐 성향만 추가
            log.info("자녀 MBTI 추가");

            // 모든 성향 변화량을 0 으로 초기화 한 후 new UpdateTraitChangeDto 로 전달

        } else {
            // 성향 변화량이 하나씩 추가되어 new UpdateTraitChangeDto 로 전달

        }

    }

    // 좋아요, 싫어요 책의 성향별 평균 계산
    private static int[] calculateBookTraits(List<int[]> likedBookTraitsList, List<int[]> hatedBookTraitsList) {
        int[] bookTraitsScore = new int[4];

        for (int[] bookTrait : likedBookTraitsList) {
            for (int j = 0; j < 4; j++) {
                bookTraitsScore[j] += bookTrait[j];
            }
        }

        for (int[] bookTrait : hatedBookTraitsList) {
            for (int j = 0; j < 4; j++) {
                bookTraitsScore[j] += bookTrait[j];
            }
        }

        int totalBookCnt = likedBookTraitsList.size() + hatedBookTraitsList.size();

        if (totalBookCnt > 0) {
            for (int i = 0; i < 4; i++) {
                bookTraitsScore[i] = bookTraitsScore[i] / totalBookCnt;
            }
        }

        return bookTraitsScore;
    }

    // 책과 아이의 성향에 따른 가중치 적용
    private Integer calculateTraitChange(int bookTrait, int childTrait, int amount) {
        int changeAmount = amount;

        boolean isBookTraitLow = bookTrait < 50;
        boolean isChildTraitLow = childTrait < 50;

        if (isBookTraitLow && isChildTraitLow) {
            changeAmount -= BASE_CHANGE_AMOUNT;
        } else if (isBookTraitLow && !isChildTraitLow) {
            changeAmount -= DECREASE_AMOUNT;
        } else if (!isBookTraitLow && isChildTraitLow) {
            changeAmount += INCREASE_AMOUNT;
        } else {
            changeAmount += BASE_CHANGE_AMOUNT;
        }

        return changeAmount;
    }

    // MBTI 추출
    private String calculateNewMbti(int[] childTrait) {
        StringBuilder mbti = new StringBuilder();

        // E (외향성) vs I (내향성)
        mbti.append(childTrait[0] >= 50 ? "E" : "I");

        // S (감각) vs N (직관)
        mbti.append(childTrait[1] >= 50 ? "S" : "N");

        // T (사고) vs F (감정)
        mbti.append(childTrait[2] >= 50 ? "T" : "F");

        // J (판단) vs P (인식)
        mbti.append(childTrait[3] >= 50 ? "J" : "P");

        return mbti.toString();
    }
}