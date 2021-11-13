package kika.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public class EditGroupRequest {
    @Nullable
    @Length(min = 1, max = 128)
    private String name;

    @Nullable
    private AddGroupMemberRequest[] members;
}
