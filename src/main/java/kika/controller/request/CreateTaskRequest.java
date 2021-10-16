package kika.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.annotation.Nullable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    @Length(min = 1, max = 32)
    private String name;

    @Nullable
    @Length(min = 1, max = 512)
    private String description;

    private long listId;

    @Nullable
    private Long parentId;
}
