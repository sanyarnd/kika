package kika.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskEditInfoResponse {
    private long id;
    private String name;
    private String description;
    private ParentTask parent;
    private SubList list;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubList {
        private long id;
        private String name;
        private long groupId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentTask {
        private long id;
        private String name;
    }
}
