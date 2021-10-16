package kika.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoveTaskRequest {
    @Nullable
    private Long listId;

    @Nullable
    private Long parentId;
}
