package kika.controller.response;

import kika.domain.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetSubscribedTaskResponse {
    private long id;
    private String name;
    private Task.Status status;
}
