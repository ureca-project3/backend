package com.triple.backend.test.entity;

import lombok.Getter;

@Getter
public enum TraitType {
    에너지방향("I", "E"),
    인식기능("N", "S"),
    판단기능("F", "T"),
    생활양식("P", "J");

    private final String highScoreTrait;
    private final String lowScoreTrait;

    TraitType(String highScoreTrait, String lowScoreTrait) {
        this.highScoreTrait = highScoreTrait;
        this.lowScoreTrait = lowScoreTrait;
    }

    public String getTraitByScore(int score) {
        return score > 50 ? highScoreTrait : lowScoreTrait;
    }
}
