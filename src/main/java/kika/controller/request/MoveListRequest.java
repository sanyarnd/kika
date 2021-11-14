package kika.controller.request;

import org.jetbrains.annotations.Nullable;

public record MoveListRequest(
    @Nullable
    Long parentId
) {
}
