package kika.controller.response;

import java.util.List;
import kika.domain.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupInfoResponse {
    private long id;
    private String name;
    private AccountRole.Role role;
    private List<SubTaskList> lists;
    private MessageBulk messages;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTaskList {
        private long id;
        private String name;
    }
}
