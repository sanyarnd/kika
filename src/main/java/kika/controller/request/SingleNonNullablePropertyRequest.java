package kika.controller.request;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record SingleNonNullablePropertyRequest(
    @Length(min = 1, max = 128)
    @NotNull
    String value
) {
}
