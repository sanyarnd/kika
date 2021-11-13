package kika.controller.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskListSpecialAccessResponse {
    private boolean set;
    private List<AccountWithAccess> accounts;
}
