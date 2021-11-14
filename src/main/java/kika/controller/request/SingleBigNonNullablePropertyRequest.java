package kika.controller.request;

import org.hibernate.validator.constraints.Length;

public record SingleBigNonNullablePropertyRequest(
    @Length(min = 1, max = 512)
    String value
) {
}
