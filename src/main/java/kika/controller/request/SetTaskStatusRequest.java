package kika.controller.request;

import javax.validation.constraints.NotNull;
import kika.domain.Task;

public record SetTaskStatusRequest(
    @NotNull
    Task.Status status
) {
}
