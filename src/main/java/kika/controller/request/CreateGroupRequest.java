package kika.controller.request;

import java.util.List;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.Nullable;

public record CreateGroupRequest(
    @Length(min = 1, max = 128)
    String name,

    @Nullable
    List<AddGroupMemberRequest> members
) {
}
