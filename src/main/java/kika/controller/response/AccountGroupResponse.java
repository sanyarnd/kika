package kika.controller.response;

import kika.domain.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountGroupResponse {
    private long id;
    private String name;
    private AccountRole.Role role;
}
