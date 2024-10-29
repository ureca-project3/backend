package com.triple.backend.child.dto;

import com.triple.backend.child.entity.Child;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChildDto {
    private Long childId;
    private String name;
    private Integer age;
    private String birthdate;
    private String gender;
    private String imageUrl;

    public ChildDto(Child child) {
        this.childId = child.getChildId();
        this.name = child.getName();
        this.age = child.getAge();
        this.birthdate = child.getBirthdate();
        this.gender = child.getGender();
        this.imageUrl = child.getImageUrl();
    }

    public static ChildDto from(Child child) {
        return new ChildDto(child);
    }
}