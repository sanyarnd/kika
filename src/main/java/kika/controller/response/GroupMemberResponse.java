package kika.controller.response;

import kika.domain.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberResponse {
    private long id;
    private AccountRole.Role role;
    private String name;
}
