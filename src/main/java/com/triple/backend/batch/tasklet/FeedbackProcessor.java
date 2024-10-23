package com.triple.backend.batch.tasklet;

import com.triple.backend.batch.dto.FeedbackDto;
import com.triple.backend.batch.dto.TraitsChangeDto;
import com.triple.backend.book.entity.BookTraits;
import com.triple.backend.book.repository.BookTraitsRepository;
import com.triple.backend.child.entity.ChildTraits;
import com.triple.backend.child.repository.ChildTraitsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FeedbackProcessor implements ItemProcessor<List<FeedbackDto>, List<TraitsChangeDto>> {

    private final ChildTraitsRepository childTraitsRepository;
    private final BookTraitsRepository bookTraitsRepository;

    @Override
    public List<TraitsChangeDto> process(List<FeedbackDto> feedbackDtos) throws Exception {
        List<TraitsChangeDto> traitsChangeDtoList = new ArrayList<>();

        /*
        문제1 : feedbackDto를 돌면서 계속 똑같은 child의 trait를 찾아 비효율적이다.
        문제2 : 한 멤버가 '좋아요'한 책들을 한꺼번에 가져오지 않고 각각 쿼리를 날려 비효율적이다.
        문제3 : childTrait <-> bookTrait 1대1로 비교를 하는데, 책들끼리 성향 비교 후 멤버와 최종 비교해서 변화량을 측정하는 게 더 효율적이다.
        문제4 : redis에 책들의 성향이 모두 저장되어 있다면(캐시되어 있다면), 훨씬 빠른 연산이 가능하다. mysql 접근을 줄일 수 없을까?
        문제5 : ChildTraits, BookTraits의 성향 점수들은 Integer 타입이다. 하지만 성향 가중치는 소수점 단위로 올라간다. 현재는 Math.floor로 내림한 뒤, int 변환하는데, 더 좋은 방법이 있을까?
         */
        for (FeedbackDto feedbackDto : feedbackDtos) {
            List<ChildTraits> childTraits = childTraitsRepository.findAllByChildId(feedbackDto.getChildId());
            List<BookTraits> bookTraits = bookTraitsRepository.findAllByBookId(feedbackDto.getBookId());

            double totalChangeAmount = 0.0;
            for (BookTraits bookTrait : bookTraits) {
                ChildTraits correspondingChildTrait = findChildTraitByTraitId(childTraits, bookTrait.getTrait().getTraitId());
                double changeAmount = calculateTraitChange(bookTrait, correspondingChildTrait);
                totalChangeAmount += changeAmount;

                boolean isBeyondFive = Math.abs(totalChangeAmount) >= 5.0;
                traitsChangeDtoList.add(new TraitsChangeDto(feedbackDto.getChildId(), bookTrait.getTrait().getTraitId(), changeAmount, isBeyondFive));
            }

        }

        return traitsChangeDtoList;
    }

    private ChildTraits findChildTraitByTraitId(List<ChildTraits> childTraits, Long traitId) {
        return childTraits.stream()
                .filter(childTrait -> childTrait.getTrait().getTraitId().equals(traitId))
                .findFirst().
                orElseThrow(() -> new IllegalArgumentException("대응되는 성향이 없습니다"));
    }

    /*
    간단하게 구현해 본 계산 로직. 이후 가중치를 심화할 수 있도록 구현
     */
    private double calculateTraitChange(BookTraits bookTrait, ChildTraits childTrait) {
        double changeAmount = 0.0;

        boolean isBookTraitLow = bookTrait.getTraitScore() < 50;
        boolean isChildTraitLow = childTrait.getTraitScore() < 50;

        if (isBookTraitLow && isChildTraitLow) {
            // 둘다 50 미만인 경우, 아이의 성향이 강화되어 더 작은 점수가 된다
            changeAmount -= 0.5;
        } else if (isBookTraitLow && !isChildTraitLow) {
            // 책은 50 미만인데, 아이는 50 이상인 경우, 성향이 약화되어 작은 점수가 된다
            changeAmount -= 0.3;
        } else if (!isBookTraitLow && isChildTraitLow) {
            // 책은 50 이상인데, 아이는 50 미만인 경우, 성향이 강화되어 큰 점수가 된다
            changeAmount += 0.3;
        } else if (!isBookTraitLow && !isChildTraitLow) {
            // 둘다 50 이상인 경우, 성향이 강화되어 더 큰 점수가 된다
            changeAmount += 0.5;
        }

        return changeAmount;
    }
}
