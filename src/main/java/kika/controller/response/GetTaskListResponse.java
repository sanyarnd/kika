package kika.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskListResponse {
    private long id;
    private String name;
    private Long parentId;
    private Set<Long> childrenIds;
}
