package kika.controller.request;

import javax.validation.constraints.NotNull;

public record SingleNonNullableNumericPropertyRequest(
    @NotNull
    long value
) {
}
