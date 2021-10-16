package kika.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SetNumericPropertyListRequest {
    @NotNull
    private Collection<Long> values;
}
