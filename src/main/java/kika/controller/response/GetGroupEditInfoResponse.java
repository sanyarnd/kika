package kika.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupEditInfoResponse {
    private long id;
    private String name;
    private List<GroupMemberResponse> members;
    private long messageCount;
}
