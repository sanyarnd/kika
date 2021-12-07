package kika.controller.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupEditInfoResponse {
    private long id;
    private String name;
    private List<GroupMemberResponse> members;
    private long messageCount;
}
