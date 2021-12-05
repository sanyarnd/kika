package kika.controller.response;

import kika.service.dto.ConciseTaskListDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetGroupEditInfoListsResponse {
    private long id;
    private String name;
    private List<ConciseTaskListDto> lists;
    private TaskListSpecialAccessResponse members;
}
