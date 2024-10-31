package com.triple.backend.child.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChildRegisterResponseDto {
    private String message;
    private ChildData data;
    private String timestamp;

    @Getter
    @Setter
    public static class ChildData {
        private String name;

        public ChildData(String name) {
            this.name = name;
        }
    }
}