package com.triple.backend.child.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChildRegisterRequestDto {
    private String name;
    private Integer age;
    private String birthDate;
    private String gender;
    private String profileImage;
}
