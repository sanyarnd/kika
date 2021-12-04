package kika.controller.request;

import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public record CreateGroupRequest(
    @Length(min = 1, max = 128)
    String name,

    @Nullable
    List<AddGroupMemberRequest> members
) {
}
