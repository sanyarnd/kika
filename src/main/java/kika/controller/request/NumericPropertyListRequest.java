package kika.controller.request;

import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NumericPropertyListRequest {
    @NotNull
    private Set<Long> values;
}
