package kika.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskAssigneesResponse {
    private Collection<GetTaskAssigneeResponse> values;
    private long count;
}
