package kika.controller.response;

import java.util.List;
import kika.domain.AccountRole;
import kika.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetListInfoResponse {
    private long id;
    private String name;
    private SubGroup group;
    private ParentTaskList parent;
    private List<ChildTaskList> children;
    private List<SubTask> tasks;

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
    public static class SubTask {
        private long id;
        private String name;
        private Task.Status status;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildTaskList {
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
