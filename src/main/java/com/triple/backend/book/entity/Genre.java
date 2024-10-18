package com.triple.backend.book.entity;

import lombok.Getter;

@Getter
public enum Genre {
    동화("010"),
    브랜드전집("011"),
    명작동화("012"),
    전래동화("013"),
    창작동화("014"),
    위인전("015"),
    공주왕자("016"),
    모험탐험("017"),
    유명작가("018"),
    자연("020"),
    계절날씨("021"),
    산바다("022"),
    하늘우주("023"),
    식물("024"),
    배우기("030"),
    한글("031"),
    수학("032"),
    과학("033"),
    역사문화("034"),
    무료워크지("035"),
    퀴즈("036"),
    생활안전("037"),
    성교육("038"),
    동물("040"),
    곤충("042"),
    파충류("043"),
    공룡("044"),
    새("045"),
    반려동물("046"),
    바다동물("047"),
    물고기("048"),
    일상생활("050"),
    가족("051"),
    친구("052"),
    똥방귀("053"),
    잠자기("054"),
    탈것("055"),
    음식("056"),
    놀이체험("057"),
    그림음악("058");

    private final String genreCode;

    Genre(String genreCode) {
        this.genreCode = genreCode;
    }

    public static String getGenreCode(String genreName) {
        for (Genre genre : Genre.values()) {
            if (genre.name().equals(genreName)) {
                return genre.getGenreCode();
            }
        }
        return null;
    }
}
