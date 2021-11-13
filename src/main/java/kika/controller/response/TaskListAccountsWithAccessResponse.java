package kika.controller.response;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskListAccountsWithAccessResponse {
    private boolean set;
    private Collection<AccountWithAccess> accounts;
    private long count;
}
