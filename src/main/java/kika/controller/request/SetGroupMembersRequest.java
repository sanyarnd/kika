package kika.controller.request;

import java.util.Map;
import kika.domain.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SetGroupMembersRequest {
    @Nullable
    private Map<Long, AccountRole.Role> members;
}
