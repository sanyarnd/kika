package kika.controller.request;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.Nullable;

public record CreateTaskListRequest(
    @Length(min = 1, max = 128)
    String name,

    long groupId,

    @Nullable
    Long parentId,

    @Nullable
    @SuppressFBWarnings("CNC_COLLECTION_NAMING_CONFUSION")
    Set<Long> accessList
) {
}
