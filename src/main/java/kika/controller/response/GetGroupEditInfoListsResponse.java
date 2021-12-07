package kika.controller.response;

import java.util.List;
import kika.service.dto.ConciseTaskListDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupEditInfoListsResponse {
    private long id;
    private String name;
    private List<ConciseTaskListDto> lists;
    private TaskListSpecialAccessResponse members;
}
