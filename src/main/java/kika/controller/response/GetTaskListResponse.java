package kika.controller.response;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskListResponse {
    private long id;
    private String name;
    private Long parentId;
    private Set<Long> childrenIds;
}
