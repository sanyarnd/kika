package kika.controller.request;

import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.Nullable;

public record EditTaskRequest(
    @Length(min = 1, max = 32)
    String name,

    @Nullable
    @Length(min = 1, max = 512)
    String description,

    long listId,

    @Nullable
    Long parentId
) {
}
