package kika.controller.request;

import javax.validation.constraints.NotNull;
import kika.domain.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddGroupMemberRequest {
    private long id;

    @NotNull
    private AccountRole.Role role = AccountRole.Role.MEMBER;
}
