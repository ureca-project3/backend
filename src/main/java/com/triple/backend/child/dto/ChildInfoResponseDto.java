package com.triple.backend.child.dto;

import com.triple.backend.child.entity.Child;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class ChildInfoResponseDto {
    private String name;
    private int age;
    private String birthDate;
    private String gender;
    private String profileImage;
    private Map<String, Integer> historyMbti;
    private String reason;
    private String currentMbti;
    private List<String> historyDate;

    public ChildInfoResponseDto(String name, int age, String birthDate, String gender, String profileImage,
                                Map<String, Integer> historyMbti, String reason, String currentMbti, List<String> historyDate) {
        this.name = name;
        this.age = age;
        this.birthDate = birthDate;
        this.gender = gender;
        this.profileImage = profileImage;
        this.historyMbti = historyMbti;
        this.reason = reason;
        this.currentMbti = currentMbti;
        this.historyDate = historyDate;
    }

    public static ChildInfoResponseDto toDto(Child child, Map<String, Integer> historyMbti, String reason, String currentMbti, List<String> historyDateList) {
        return new ChildInfoResponseDto(
                child.getName(),
                child.getAge(),
                child.getBirthdate(),
                child.getGender(),
                child.getImageUrl(),
                historyMbti,
                reason,
                currentMbti,
                historyDateList
        );
    }
}
