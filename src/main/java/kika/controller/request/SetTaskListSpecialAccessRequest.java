package kika.controller.request;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SetTaskListSpecialAccessRequest {
    private Set<Long> accountsWithAccess;

    public SetTaskListSpecialAccessRequest() {
        accountsWithAccess = new HashSet<>();
    }
}
