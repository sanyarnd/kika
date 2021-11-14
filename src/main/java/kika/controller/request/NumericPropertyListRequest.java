package kika.controller.request;

import java.util.Set;
import javax.validation.constraints.NotNull;

public record NumericPropertyListRequest(
    @NotNull
    Set<Long> values
) {
}
