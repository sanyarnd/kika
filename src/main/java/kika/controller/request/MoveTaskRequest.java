package kika.controller.request;

import org.jetbrains.annotations.Nullable;

public record MoveTaskRequest(
    @Nullable
    Long listId,

    @Nullable
    Long parentId
) {
}
