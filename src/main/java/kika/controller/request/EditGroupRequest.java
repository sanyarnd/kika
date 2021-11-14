package kika.controller.request;

import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.Nullable;

public record EditGroupRequest(
    @Length(min = 1, max = 128)
    String name,

    @Nullable
    AddGroupMemberRequest[] members
) {
}
