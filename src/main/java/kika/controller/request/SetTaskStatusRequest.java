package kika.controller.request;

import kika.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SetTaskStatusRequest {
    @NotNull
    private Task.Status status;
}
