package kika.controller.request;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SingleNonNullableNumericPropertyRequest {
    @NotNull
    private long value;
}
