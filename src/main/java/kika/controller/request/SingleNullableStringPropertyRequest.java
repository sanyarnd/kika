package kika.controller.request;

import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.Nullable;

public record SingleNullableStringPropertyRequest(
    @Length(min = 1, max = 128)
    @Nullable
    String value
) {
}
