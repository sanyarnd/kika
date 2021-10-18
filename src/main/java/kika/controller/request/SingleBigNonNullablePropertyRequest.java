package kika.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SingleBigNonNullablePropertyRequest {
    @Length(min = 1, max = 512)
    private String value;
}
