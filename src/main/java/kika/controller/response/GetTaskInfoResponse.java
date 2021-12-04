package kika.controller.response;

import kika.domain.AccountRole;
import kika.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskInfoResponse {
    private long id;
    private String name;
    private String description;
    private boolean subscribed;
    private boolean assigned;
    private ParentTask parent;
    private SubTaskList list;
    private SubGroup group;
    private List<ChildTask> children;
    private kika.domain.Task.Status status;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentTask {
        private long id;
        private String name;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildTask {
        private long id;
        private String name;
        private Task.Status status;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTaskList {
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
