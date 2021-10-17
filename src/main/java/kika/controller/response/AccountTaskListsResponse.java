package kika.controller.response;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountTaskListsResponse {
    private Collection<AccountTaskListResponse> taskLists;
    private long count;
}
