package kika.controller.response;

import kika.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskResponse {
    private long id;
    private String name;
    private String description;
    private Task.Status status;
    private Long parentId;
    private long listId;
    private Set<Long> childrenIds;
}
