package kika.controller.request;

import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SetNumericPropertyListRequest {
    @NotNull
    private Collection<Long> values;
}
