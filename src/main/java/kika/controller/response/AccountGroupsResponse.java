package kika.controller.response;

import java.util.Collection;
import kika.service.dto.GroupDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountGroupsResponse {
    private Collection<GroupDto> groups;
    private long count;
}
