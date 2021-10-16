package kika.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountTaskListsResponse {
    private Collection<AccountTaskListResponse> taskLists;
    private long count;
}
