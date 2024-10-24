package com.triple.backend.batch.tasklet;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.triple.backend.batch.dto.BookChildTraitDto;
import com.triple.backend.batch.dto.TraitsChangeDto;
import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.repository.ChildTraitsRepository;
import com.triple.backend.common.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FeedbackProcessor implements ItemProcessor<BookChildTraitDto, TraitsChangeDto> {

    private final ChildTraitsRepository childTraitsRepository;

    @Override
    public TraitsChangeDto process(BookChildTraitDto bookChildTraitDto) throws Exception {
        List<int[]> likedBookTraitsList = bookChildTraitDto.getLikedBookTraitsList();
        List<int[]> hatedBookTraitsList = bookChildTraitDto.getHatedBookTraitsList();

        int[] bookTrait = calculateBookTraits(likedBookTraitsList, hatedBookTraitsList);
        int[] childTrait = bookChildTraitDto.getChildTraitScore();

        ChildTraits traits = childTraitsRepository.findChildTraitsByChildId(bookChildTraitDto.getChildId())
            .orElseThrow(() -> NotFoundException.entityNotFound("아이 성향"));

        double[] changeAmount = new double[4];
        boolean isBeyondFive = false;

        for (int i = 0; i < 4; i++) {
            double traitChangeAmount = calculateTraitChange(bookTrait[i], childTrait[i]);

            changeAmount[i] = traitChangeAmount;

            if (Math.abs(traitChangeAmount) >= 5) {
                isBeyondFive = true;
            }
        }

        return new TraitsChangeDto(
            bookChildTraitDto.getChildId(),
            traits.getTrait().getTraitId(),
            changeAmount,
            isBeyondFive
        );
    }

    private static int[] calculateBookTraits(List<int[]> likedBookTraitsList, List<int[]> hatedBookTraitsList) {
        int[] bookTraitsScore = new int[4];

        // 좋아요 책 성향
        for (int[] bookTrait : likedBookTraitsList) {
            for (int j = 0; j < 4; j++) {
                bookTraitsScore[j] += bookTrait[j];
            }
        }

        // 싫어요 책 성향
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
    private double calculateTraitChange(int bookTrait, int childTrait) {
        final double BASE_CHANGE_AMOUNT = 0.5;
        final double INCREASE_AMOUNT = 0.3;
        final double DECREASE_AMOUNT = 0.3;

        double changeAmount = BASE_CHANGE_AMOUNT;

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
}