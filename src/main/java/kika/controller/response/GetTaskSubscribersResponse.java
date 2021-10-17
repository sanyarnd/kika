package kika.controller.response;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetTaskSubscribersResponse {
    private Collection<GetTaskSubscriberResponse> values;
    private long count;
}
