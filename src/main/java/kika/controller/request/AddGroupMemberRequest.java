package kika.controller.request;

import javax.validation.constraints.NotNull;
import kika.domain.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddGroupMemberRequest {
    @NotNull
    private long id;

    @NotNull
    private AccountRole.Role role;

    public AddGroupMemberRequest() {
        role = AccountRole.Role.MEMBER;
    }
}
