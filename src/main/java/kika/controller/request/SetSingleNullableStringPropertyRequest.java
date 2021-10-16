package kika.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.annotation.Nullable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SetSingleNullableStringPropertyRequest {
    @Length(min = 1, max = 128)
    @Nullable
    private String value;
}
