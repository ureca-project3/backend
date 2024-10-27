package com.triple.backend.common.utils;

import com.triple.backend.batch.dto.MbtiWithTraitScoreDto;

import java.util.List;

public class MbtiCalculator {
    private static final int BASE_CHANGE_AMOUNT = 2;
    private static final int INCREASE_AMOUNT = 1;
    private static final int DECREASE_AMOUNT = 1;

    public static String calculateNewMbti(List<MbtiWithTraitScoreDto> childTraits) {
        int[] traitScores = new int[4]; // 0: E/I, 1: S/N, 2: T/F, 3: J/P

        // 각 traitId에 해당하는 traitScore를 traitScores 배열에 삽입
        for (MbtiWithTraitScoreDto data : childTraits) {
            int traitIndex = getTraitIndex(data.getTraitId());
            if (traitIndex != -1) {
                traitScores[traitIndex] = data.getTraitScore();
            }
        }

        StringBuilder mbti = new StringBuilder();
        mbti.append(traitScores[0] >= 50 ? "E" : "I"); // E vs I
        mbti.append(traitScores[1] >= 50 ? "S" : "N"); // S vs N
        mbti.append(traitScores[2] >= 50 ? "T" : "F"); // T vs F
        mbti.append(traitScores[3] >= 50 ? "J" : "P"); // J vs P

        return mbti.toString();
    }

    // traitId를 배열의 인덱스로 변환하는 메서드
    private static int getTraitIndex(Long traitId) {
        if (traitId == 1) return 0; // E/I
        if (traitId == 2) return 1; // S/N
        if (traitId == 3) return 2; // T/F
        if (traitId == 4) return 3; // J/P
        return -1; // 잘못된 traitId
    }

    public static Integer calculateTraitChange(int bookTrait, int childTrait) {

        int changeAmount = BASE_CHANGE_AMOUNT;

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
