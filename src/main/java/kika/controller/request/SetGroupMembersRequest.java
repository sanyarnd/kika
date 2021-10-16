package kika.controller.request;

import kika.domain.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SetGroupMembersRequest {
    @Nullable
    private Map<Long, AccountRole.Role> members;
}
