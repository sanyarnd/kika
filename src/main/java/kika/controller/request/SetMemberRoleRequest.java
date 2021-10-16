package kika.controller.request;

import kika.domain.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SetMemberRoleRequest {
    AccountRole.Role role;
}
