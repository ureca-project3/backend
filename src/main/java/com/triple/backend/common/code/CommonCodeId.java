package com.triple.backend.common.code;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCodeId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(columnDefinition = "char(3)")
    private String codeId;
    private String groupId;

    public CommonCodeId(String codeId, String groupId) {
        this.codeId = codeId;
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommonCodeId codeId1 = (CommonCodeId) o;
        return Objects.equals(getCodeId(), codeId1.getCodeId()) && Objects.equals(getGroupId(), codeId1.getGroupId());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getCodeId());
        result = 31 * result + Objects.hashCode(getGroupId());
        return result;
    }
}
