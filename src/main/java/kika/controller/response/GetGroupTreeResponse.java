package kika.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupTreeResponse {
    private long id;
    private String name;
    private List<SubTaskList> lists;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTaskList {
        private long id;
        private String name;
        private List<SubTaskList> children;
        private List<SubTask> tasks;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTask {
        private long id;
        private String name;
        private List<SubTask> children;
    }
}
