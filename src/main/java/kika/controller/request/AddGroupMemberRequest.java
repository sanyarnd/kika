package kika.controller.request;

import kika.domain.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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
