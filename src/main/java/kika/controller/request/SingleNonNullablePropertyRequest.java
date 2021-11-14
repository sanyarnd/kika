package kika.controller.request;

import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;

public record SingleNonNullablePropertyRequest(
    @Length(min = 1, max = 128)
    @NotNull
    String value
) {
}
