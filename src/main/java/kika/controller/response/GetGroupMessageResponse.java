package kika.controller.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupMessageResponse {
    private long id;
    private long groupId;
    private Instant createdDate;
    private String body;
    private String sender;
}
