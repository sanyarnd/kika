package kika.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.annotation.Nullable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskListRequest {
    @Length(min = 1, max = 128)
    private String name;

    private long groupId;

    @Nullable
    private Long parentId;
}
