package kika.controller.response;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskListAccountsWithAccessResponse {
    private Collection<TaskListAccountWithAccessResponse> accounts;
    private long count;
}
