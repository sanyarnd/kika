package kika.controller.response;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageBulk {
    private List<SubMessage> messages;
    private long count;
    private long offset;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubMessage {
        private long id;
        private Instant createdDate;
        private String body;
        private String sender;
    }
}
