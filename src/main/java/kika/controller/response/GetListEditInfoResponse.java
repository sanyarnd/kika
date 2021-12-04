package kika.controller.response;

import kika.domain.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetListEditInfoResponse {
    private long id;
    private String name;
    private SubGroup group;
    private ParentTaskList parent;
    private TaskListSpecialAccessResponse accessData;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentTaskList {
        private long id;
        private String name;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubGroup {
        private long id;
        private String name;
        private AccountRole.Role role;
    }
}
