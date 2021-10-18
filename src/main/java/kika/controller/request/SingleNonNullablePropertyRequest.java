package kika.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SingleNonNullablePropertyRequest {
    @Length(min = 1, max = 128)
    private String value;
}
