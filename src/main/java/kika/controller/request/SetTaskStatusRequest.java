package kika.controller.request;

import javax.validation.constraints.NotNull;
import kika.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SetTaskStatusRequest {
    @NotNull
    private Task.Status status;
}
