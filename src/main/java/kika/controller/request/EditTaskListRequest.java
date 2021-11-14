package kika.controller.request;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record EditTaskListRequest(
    @Length(min = 1, max = 128)
    String name,

    @Nullable
    Long parentId,

    @NotNull
    @SuppressFBWarnings("CNC_COLLECTION_NAMING_CONFUSION")
    Set<Long> accessList
) {
}
