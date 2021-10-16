package kika.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public class SetTaskListSpecialAccessRequest {
    private Set<Long> accountsWithAccess;

    public SetTaskListSpecialAccessRequest() {
        accountsWithAccess = new HashSet<>();
    }
}
