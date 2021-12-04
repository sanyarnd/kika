package kika.controller.response;

import kika.domain.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupResponse {
    private long id;
    private String name;
    private long ownerId;
    private String ownerName;
    private AccountRole.Role role;
    private long messageCount;
}
