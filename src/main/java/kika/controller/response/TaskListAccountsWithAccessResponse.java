package kika.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskListAccountsWithAccessResponse {
    private Collection<TaskListAccountWithAccessResponse> accounts;
    long count;
}
