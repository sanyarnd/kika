package kika.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupResponse {
    private String name;
    private long ownerId;
    private String ownerName;
}
