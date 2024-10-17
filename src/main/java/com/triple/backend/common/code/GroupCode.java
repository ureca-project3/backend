package com.triple.backend.common.code;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupCode {
    @Id
    @Column(columnDefinition = "char(3)")
    private String groupId;

    private String groupName;

    public GroupCode(String groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }
}
