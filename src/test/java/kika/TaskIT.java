package kika;

import java.util.Map;
import kika.domain.Task;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static java.lang.Integer.parseInt;
import static kika.JsonUtils.numericList;
import static kika.JsonUtils.writeJson;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TaskIT extends AbstractIT {
    @Autowired
    private MockMvc mockMvc;

    public String createAccount() throws Exception {
        return mockMvc.perform(post("/account/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "kate"))))
            .andReturn().getResponse().getContentAsString();
    }

    public String createGroup(String ownerId) throws Exception {
        return mockMvc.perform(post("/group/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "group", "ownerId", ownerId))))
            .andReturn().getResponse().getContentAsString();
    }

    public String createList(String groupId) throws Exception {
        return mockMvc.perform(post("/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId))))
            .andReturn().getResponse().getContentAsString();
    }

    @Test
    @Order(1)
    public void createTask() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);
        String listId = createList(groupId);

        mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId))))
            .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    public void renameTaskAndChangeDescription() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);
        String listId = createList(groupId);

        String taskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId))))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post(String.format("/task/%s/rename", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": null}"))
            .andExpect(status().is5xxServerError());

        mockMvc.perform(post(String.format("/task/%s/rename", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "kate's task"))))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId)))
            .andExpectAll(jsonPath("$.name").value("kate's task"),
                jsonPath("$.description").isEmpty(),
                jsonPath("$.parentId").isEmpty(),
                jsonPath("$.childrenIds").isEmpty());

        mockMvc.perform(post(String.format("/task/%s/description", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "an awesome task"))))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId)))
            .andExpectAll(jsonPath("$.name").value("kate's task"),
                jsonPath("$.description").value("an awesome task"),
                jsonPath("$.parentId").isEmpty(),
                jsonPath("$.childrenIds").isEmpty());

        mockMvc.perform(post(String.format("/task/%s/description", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": null}"))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId)))
            .andExpectAll(jsonPath("$.name").value("kate's task"),
                jsonPath("$.description").isEmpty(),
                jsonPath("$.parentId").isEmpty(),
                jsonPath("$.childrenIds").isEmpty());
    }

    @Test
    @Order(3)
    public void deleteTask() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);
        String listId = createList(groupId);

        String taskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId))))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(delete(String.format("/task/%s", taskId)))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId)))
            .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/list/%s", listId)))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/group/%s", groupId)))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s", ownerId)))
            .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    public void deleteChildDeleteParentTask() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);
        String listId = createList(groupId);

        String parentTaskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId))))
            .andReturn().getResponse().getContentAsString();

        String childTask1Id = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId, "parentId", parentTaskId))))
            .andReturn().getResponse().getContentAsString();

        String childTask2Id = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId, "parentId", parentTaskId))))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(delete(String.format("/task/%s", childTask1Id)))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/task/%s", childTask1Id)))
            .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/task/%s", parentTaskId)))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/task/%s", childTask2Id)))
            .andExpect(status().isOk());

        mockMvc.perform(delete(String.format("/task/%s", parentTaskId)))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", parentTaskId)))
            .andExpect(status().is5xxServerError());
        mockMvc.perform(get(String.format("/task/%s", childTask2Id)))
            .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/list/%s/tasks", listId)))
            .andExpectAll(jsonPath("$.tasks").isEmpty(),
                jsonPath("$.count").value(0));
    }

    @Test
    @Order(5)
    public void changeTaskParent() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);
        String list1Id = createList(groupId);
        String list2Id = createList(groupId);

        // Task 1 @ list 1
        String task1Id = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id))))
            .andReturn().getResponse().getContentAsString();

        // Task 2 @ list 2
        String task2Id = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list2Id))))
            .andReturn().getResponse().getContentAsString();

        // Task 3 @ list 2, parent: task 1
        String task3Id = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id, "parentId", task1Id))))
            .andReturn().getResponse().getContentAsString();

        // Set task 2 as a parent of task 3 => error, they are in different lists
        mockMvc.perform(post(String.format("/task/%s/move", task3Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("parentId", task2Id))))
            .andExpect(status().is5xxServerError());

        // Make parent of task 3 null => success, now list 1 has two tasks with null parents: task 1 and task 3
        mockMvc.perform(post(String.format("/task/%s/move", task3Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentId\": null}"))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/list/%s/tasks", list1Id)))
            .andExpectAll(jsonPath(String.format("$.tasks[?(@.id == %s)].parentId", task1Id), nullValue(), Long.class),
                jsonPath(String.format("$.tasks[?(@.id == %s)].parentId", task3Id), nullValue(), Long.class),
                jsonPath("$.count").value(2));

        // Set task 3 as a parent of task 1 => success, now list 1 has only one task with null parent: task 3,
        // which has 1 child task: task 1
        mockMvc.perform(post(String.format("/task/%s/move", task1Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("parentId", task3Id))))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/list/%s/tasks", list1Id)))
            .andExpectAll(
                jsonPath(String.format("$.tasks[?(@.id == %s)].parentId", task1Id)).value(Integer.parseInt(task3Id)),
                jsonPath(String.format("$.tasks[?(@.id == %s)].parentId", task3Id), nullValue(), Long.class),
                jsonPath("$.count").value(2));
        mockMvc.perform(get(String.format("/task/%s", task1Id)))
            .andExpect(jsonPath("$.parentId").value(task3Id));
        mockMvc.perform(get(String.format("/task/%s", task3Id)))
            .andExpectAll(jsonPath("$.childrenIds").value(hasItem(parseInt(task1Id))),
                jsonPath("$.childrenIds").value(hasSize(1)));
    }

    @Test
    @Order(6)
    public void changeTaskList() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);
        String list1Id = createList(groupId);
        String list2Id = createList(groupId);

        String parentTaskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id))))
            .andReturn().getResponse().getContentAsString();
        String childTaskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id, "parentId", parentTaskId))))
            .andReturn().getResponse().getContentAsString();
        String childChildTaskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id, "parentId", childTaskId))))
            .andReturn().getResponse().getContentAsString();
        String childChildChildTaskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id, "parentId", childChildTaskId))))
            .andReturn().getResponse().getContentAsString();

        // Move childTask to list 2 (without specifying its parent) => success
        mockMvc.perform(post(String.format("/task/%s/move", childTaskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("listId", list2Id))))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", childTaskId)))
            .andExpectAll(jsonPath("$.parentId", nullValue(), Long.class),
                jsonPath("$.listId").value(list2Id),
                jsonPath("$.childrenIds").value(hasSize(1)));

        mockMvc.perform(get(String.format("/task/%s", childChildTaskId)))
            .andExpectAll(jsonPath("$.parentId").value(childTaskId),
                jsonPath("$.listId").value(list2Id));

        mockMvc.perform(get(String.format("/task/%s", childChildChildTaskId)))
            .andExpectAll(jsonPath("$.parentId").value(childChildTaskId),
                jsonPath("$.listId").value(list2Id));

        mockMvc.perform(get(String.format("/task/%s", parentTaskId)))
            .andExpect(jsonPath("$.childrenIds").isEmpty());

        // Move childChildTask back to list 1 and set parentTask as its parent => success
        mockMvc.perform(post(String.format("/task/%s/move", childChildTaskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("listId", list1Id, "parentId", parentTaskId))))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", parentTaskId)))
            .andExpectAll(jsonPath("$.childrenIds").value(hasSize(1)),
                jsonPath("$.childrenIds").value(hasItem(Integer.parseInt(childChildTaskId))));

        mockMvc.perform(get(String.format("/task/%s", childTaskId)))
            .andExpect(jsonPath("$.childrenIds").isEmpty());

        mockMvc.perform(get(String.format("/task/%s", childChildTaskId)))
            .andExpectAll(jsonPath("$.parentId").value(parentTaskId),
                jsonPath("$.listId").value(list1Id));

        mockMvc.perform(get(String.format("/task/%s", childChildChildTaskId)))
            .andExpectAll(jsonPath("$.parentId").value(childChildTaskId),
                jsonPath("$.listId").value(list1Id));
    }

    @Test
    @Order(8)
    public void taskAssignees() throws Exception {
        String ownerId = createAccount();
        String account1Id = createAccount();
        String account2Id = createAccount();
        String groupId = createGroup(ownerId);
        String listId = createList(groupId);

        String taskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId))))
            .andReturn().getResponse().getContentAsString();

        // Trying to assign to an account that's not in group => error
        mockMvc.perform(post(String.format("/task/%s/assignees", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id))))
            .andExpect(status().is5xxServerError());

        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id))))
            .andExpect(status().isOk());
        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account2Id))))
            .andExpect(status().isOk());

        // Restricting list access to account2
        mockMvc.perform(post(String.format("/list/%s/accounts", listId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account2Id))))
            .andExpect(status().isOk());

        // Trying to assign to an account that's got no access to the list => error
        mockMvc.perform(post(String.format("/task/%s/assignees", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id))))
            .andExpect(status().is5xxServerError());

        // Removing special access from the list and trying to assign to account1 and account2
        mockMvc.perform(post(String.format("/list/%s/accounts", listId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"values\": []}"))
            .andExpect(status().isOk());

        // Assigning account1 and account2 (both automatically get subscribed)
        mockMvc.perform(post(String.format("/task/%s/assignees", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id, account2Id))))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s/assignees", taskId)))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.values[?(@.id == %s)]", account1Id)).exists(),
                jsonPath(String.format("$.values[?(@.id == %s)]", account2Id)).exists());
        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId)))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.values[?(@.id == %s)]", account1Id)).exists(),
                jsonPath(String.format("$.values[?(@.id == %s)]", account2Id)).exists());

        // Un-assigning account1, checking that subscription remains
        mockMvc.perform(post(String.format("/task/%s/assignees", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account2Id))))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/task/%s/assignees", taskId)))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.values[?(@.id == %s)]", account1Id)).doesNotExist(),
                jsonPath(String.format("$.values[?(@.id == %s)]", account2Id)).exists());
        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId)))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.values[?(@.id == %s)]", account1Id)).exists(),
                jsonPath(String.format("$.values[?(@.id == %s)]", account2Id)).exists());

        // Assigning account1 and account2 again
        mockMvc.perform(post(String.format("/task/%s/assignees", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id, account2Id))))
            .andExpect(status().isOk());

        // Deleting account1 and checking that it's not assigned or subscribed anymore
        mockMvc.perform(delete(String.format("/account/%s", account1Id)))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s", account1Id)))
            .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/task/%s/assignees", taskId)))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account1Id)).doesNotExist(),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account2Id)).exists());

        mockMvc.perform(get(String.format("/account/%s/tasks/assigned", account2Id)))
            .andExpectAll(jsonPath(String.format("$.tasks[?(@.id == %s)]", taskId)).exists(),
                jsonPath("$.count").value(1));

        // Deleting task and checking that all assignments and subscriptions also get deleted
        mockMvc.perform(delete(String.format("/task/%s", taskId)))
            .andExpectAll(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId)))
            .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/account/%s/tasks/assigned", account2Id)))
            .andExpect(jsonPath("$.tasks").isEmpty());
        mockMvc.perform(get(String.format("/account/%s/tasks/subscribed", account2Id)))
            .andExpect(jsonPath("$.tasks").isEmpty());
    }

    @Test
    @Order(7)
    public void taskSubscribers() throws Exception {
        String ownerId = createAccount();
        String account1Id = createAccount();
        String account2Id = createAccount();
        String groupId = createGroup(ownerId);
        String listId = createList(groupId);

        String taskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId))))
            .andReturn().getResponse().getContentAsString();

        // Trying to subscribe from an account that's not in group => error
        mockMvc.perform(post(String.format("/task/%s/subscribers", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id))))
            .andExpect(status().is5xxServerError());

        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id))))
            .andExpect(status().isOk());
        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account2Id))))
            .andExpect(status().isOk());

        // Restricting list access to account2
        mockMvc.perform(post(String.format("/list/%s/accounts", listId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account2Id))))
            .andExpect(status().isOk());

        // Trying to subscribe from an account that's got no access to the list => error
        mockMvc.perform(post(String.format("/task/%s/subscribers", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id))))
            .andExpect(status().is5xxServerError());

        // Removing special access from the list and trying to subscribe from account1 and account2
        mockMvc.perform(post(String.format("/list/%s/accounts", listId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"values\": []}"))
            .andExpect(status().isOk());

        // Subscribing from account1 and account2, checking
        mockMvc.perform(post(String.format("/task/%s/subscribers", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id, account2Id))))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId)))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account1Id)).exists(),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account2Id)).exists());

        // Unsubscribing from account1, checking
        mockMvc.perform(post(String.format("/task/%s/subscribers", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account2Id))))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId)))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account1Id)).doesNotExist(),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account2Id)).exists());

        // Subscribing from account1 and account2 again
        mockMvc.perform(post(String.format("/task/%s/subscribers", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id, account2Id))))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId)))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account1Id)).exists(),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account2Id)).exists());


        // Deleting account1, checking that it has been removed from the subscribers list
        mockMvc.perform(delete(String.format("/account/%s", account1Id)))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s", account1Id)))
            .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId)))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account1Id)).doesNotExist(),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account2Id)).exists());

        mockMvc.perform(get(String.format("/account/%s/tasks/subscribed", account2Id)))
            .andExpectAll(jsonPath(String.format("$.tasks[?(@.id == %s)]", taskId)).exists(),
                jsonPath("$.count").value(1));

        // Deleting task and checking that all subscriptions have been removed as well
        mockMvc.perform(delete(String.format("/task/%s", taskId)))
            .andExpectAll(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId)))
            .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/account/%s", account2Id)))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s/tasks/subscribed", account2Id)))
            .andExpect(jsonPath("$.tasks").isEmpty());
    }

    @Test
    @Order(9)
    public void taskStatus() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);
        String listId = createList(groupId);

        String taskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId))))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get(String.format("/task/%s", taskId)))
            .andExpect(jsonPath("$.status").value(Task.Status.NOT_COMPLETED.name()));

        mockMvc.perform(post(String.format("/task/%s/status", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("status", Task.Status.COMPLETED.name()))))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId)))
            .andExpect(jsonPath("$.status").value(Task.Status.COMPLETED.name()));
    }
}
