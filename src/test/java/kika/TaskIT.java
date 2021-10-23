package kika;

import java.util.Map;
import kika.configuration.security.SecurityConfiguration;
import kika.domain.Task;
import kika.repository.AccountRepository;
import kika.security.jwt.encode.JwtToken;
import kika.security.jwt.encode.JwtTokenService;
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

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    @Order(1)
    public void createTask() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);
        String listId = utils.createList(groupId, ownerToken);

        mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    public void renameTaskAndChangeDescription() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);
        String listId = utils.createList(groupId, ownerToken);

        String taskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post(String.format("/task/%s/rename", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": null}")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(post(String.format("/task/%s/rename", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "kate's task")))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.name").value("kate's task"),
                jsonPath("$.description").isEmpty(),
                jsonPath("$.parentId").isEmpty(),
                jsonPath("$.childrenIds").isEmpty());

        mockMvc.perform(post(String.format("/task/%s/description", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "an awesome task")))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.name").value("kate's task"),
                jsonPath("$.description").value("an awesome task"),
                jsonPath("$.parentId").isEmpty(),
                jsonPath("$.childrenIds").isEmpty());

        mockMvc.perform(post(String.format("/task/%s/description", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": null}")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.name").value("kate's task"),
                jsonPath("$.description").isEmpty(),
                jsonPath("$.parentId").isEmpty(),
                jsonPath("$.childrenIds").isEmpty());
    }

    @Test
    @Order(3)
    public void deleteTask() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);
        String listId = utils.createList(groupId, ownerToken);

        String taskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(delete(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/list/%s", listId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s", ownerId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    public void deleteChildDeleteParentTask() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);
        String listId = utils.createList(groupId, ownerToken);

        String parentTaskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        String childTask1Id = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId, "parentId", parentTaskId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        String childTask2Id = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId, "parentId", parentTaskId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(delete(String.format("/task/%s", childTask1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/task/%s", childTask1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/task/%s", parentTaskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/task/%s", childTask2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(delete(String.format("/task/%s", parentTaskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", parentTaskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());
        mockMvc.perform(get(String.format("/task/%s", childTask2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/list/%s/tasks", listId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.tasks").isEmpty(),
                jsonPath("$.count").value(0));
    }

    @Test
    @Order(5)
    public void changeTaskParent() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);
        String list1Id = utils.createList(groupId, ownerToken);
        String list2Id = utils.createList(groupId, ownerToken);

        // Task 1 @ list 1
        String task1Id = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        // Task 2 @ list 2
        String task2Id = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        // Task 3 @ list 2, parent: task 1
        String task3Id = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id, "parentId", task1Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        // Set task 2 as a parent of task 3 => error, they are in different lists
        mockMvc.perform(post(String.format("/task/%s/move", task3Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("parentId", task2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        // Make parent of task 3 null => success, now list 1 has two tasks with null parents: task 1 and task 3
        mockMvc.perform(post(String.format("/task/%s/move", task3Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentId\": null}")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/list/%s/tasks", list1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath(String.format("$.tasks[?(@.id == %s)].parentId", task1Id), nullValue(), Long.class),
                jsonPath(String.format("$.tasks[?(@.id == %s)].parentId", task3Id), nullValue(), Long.class),
                jsonPath("$.count").value(2));

        // Set task 3 as a parent of task 1 => success, now list 1 has only one task with null parent: task 3,
        // which has 1 child task: task 1
        mockMvc.perform(post(String.format("/task/%s/move", task1Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("parentId", task3Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/list/%s/tasks", list1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(
                jsonPath(String.format("$.tasks[?(@.id == %s)].parentId", task1Id)).value(Integer.parseInt(task3Id)),
                jsonPath(String.format("$.tasks[?(@.id == %s)].parentId", task3Id), nullValue(), Long.class),
                jsonPath("$.count").value(2));
        mockMvc.perform(get(String.format("/task/%s", task1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("$.parentId").value(task3Id));
        mockMvc.perform(get(String.format("/task/%s", task3Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.childrenIds").value(hasItem(parseInt(task1Id))),
                jsonPath("$.childrenIds").value(hasSize(1)));
    }

    @Test
    @Order(6)
    public void changeTaskList() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);
        String list1Id = utils.createList(groupId, ownerToken);
        String list2Id = utils.createList(groupId, ownerToken);

        String parentTaskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();
        String childTaskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id, "parentId", parentTaskId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();
        String childChildTaskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id, "parentId", childTaskId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();
        String childChildChildTaskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", list1Id, "parentId", childChildTaskId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        // Move childTask to list 2 (without specifying its parent) => success
        mockMvc.perform(post(String.format("/task/%s/move", childTaskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("listId", list2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", childTaskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.parentId", nullValue(), Long.class),
                jsonPath("$.listId").value(list2Id),
                jsonPath("$.childrenIds").value(hasSize(1)));

        mockMvc.perform(get(String.format("/task/%s", childChildTaskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.parentId").value(childTaskId),
                jsonPath("$.listId").value(list2Id));

        mockMvc.perform(get(String.format("/task/%s", childChildChildTaskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.parentId").value(childChildTaskId),
                jsonPath("$.listId").value(list2Id));

        mockMvc.perform(get(String.format("/task/%s", parentTaskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("$.childrenIds").isEmpty());

        // Move childChildTask back to list 1 and set parentTask as its parent => success
        mockMvc.perform(post(String.format("/task/%s/move", childChildTaskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("listId", list1Id, "parentId", parentTaskId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", parentTaskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.childrenIds").value(hasSize(1)),
                jsonPath("$.childrenIds").value(hasItem(Integer.parseInt(childChildTaskId))));

        mockMvc.perform(get(String.format("/task/%s", childTaskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("$.childrenIds").isEmpty());

        mockMvc.perform(get(String.format("/task/%s", childChildTaskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.parentId").value(parentTaskId),
                jsonPath("$.listId").value(list1Id));

        mockMvc.perform(get(String.format("/task/%s", childChildChildTaskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.parentId").value(childChildTaskId),
                jsonPath("$.listId").value(list1Id));
    }

    @Test
    @Order(8)
    public void taskAssignees() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);
        String listId = utils.createList(groupId, ownerToken);

        String account1Id = utils.createAccount();
        JwtToken account1Token = utils.getToken(account1Id);
        String account2Id = utils.createAccount();
        JwtToken account2Token = utils.getToken(account2Id);

        String taskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        // Trying to assign to an account that's not in group => error
        mockMvc.perform(post(String.format("/task/%s/assignee", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        // Restricting list access to account2
        mockMvc.perform(post(String.format("/list/%s/accounts", listId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        // Trying to assign to an account that's got no access to the list => error
        mockMvc.perform(post(String.format("/task/%s/assignee", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isUnauthorized());

        // Removing special access from the list and trying to assign to account1 and account2
        mockMvc.perform(post(String.format("/list/%s/accounts", listId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"values\": []}")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isOk());

        // Assigning account1 and account2 (both automatically get subscribed)
        mockMvc.perform(post(String.format("/task/%s/assignee", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(post(String.format("/task/%s/assignee", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s/assignees", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.values[?(@.id == %s)]", account1Id)).exists(),
                jsonPath(String.format("$.values[?(@.id == %s)]", account2Id)).exists());
        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.values[?(@.id == %s)]", account1Id)).exists(),
                jsonPath(String.format("$.values[?(@.id == %s)]", account2Id)).exists());

        // Un-assigning account1, checking that subscription remains
        mockMvc.perform(delete(String.format("/task/%s/assignee", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/task/%s/assignees", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.values[?(@.id == %s)]", account1Id)).doesNotExist(),
                jsonPath(String.format("$.values[?(@.id == %s)]", account2Id)).exists());
        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.values[?(@.id == %s)]", account1Id)).exists(),
                jsonPath(String.format("$.values[?(@.id == %s)]", account2Id)).exists());

        // Assigning account1 and account2 again
        mockMvc.perform(post(String.format("/task/%s/assignee", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(post(String.format("/task/%s/assignee", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isOk());

        // Deleting account1 and checking that it's not assigned or subscribed anymore
        mockMvc.perform(delete(String.format("/account/%s", account1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s", account1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/task/%s/assignees", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account1Id)).doesNotExist(),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account2Id)).exists());

        mockMvc.perform(get(String.format("/account/%s/tasks/assigned", account2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpectAll(jsonPath(String.format("$.tasks[?(@.id == %s)]", taskId)).exists(),
                jsonPath("$.count").value(1));

        // Deleting task and checking that all assignments and subscriptions also get deleted
        mockMvc.perform(delete(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/account/%s/tasks/assigned", account2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(jsonPath("$.tasks").isEmpty());
        mockMvc.perform(get(String.format("/account/%s/tasks/subscribed", account2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(jsonPath("$.tasks").isEmpty());
    }

    @Test
    @Order(7)
    public void taskSubscribers() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);
        String listId = utils.createList(groupId, ownerToken);

        String account1Id = utils.createAccount();
        JwtToken account1Token = utils.getToken(account1Id);
        String account2Id = utils.createAccount();
        JwtToken account2Token = utils.getToken(account2Id);

        String taskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        // Trying to subscribe from an account that's not in group => error
        mockMvc.perform(post(String.format("/task/%s/subscriber", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        // Restricting list access to account2
        mockMvc.perform(post(String.format("/list/%s/accounts", listId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isOk());

        // Trying to subscribe from an account that's got no access to the list => error
        mockMvc.perform(post(String.format("/task/%s/subscriber", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isUnauthorized());

        // Removing special access from the list
        mockMvc.perform(post(String.format("/list/%s/accounts", listId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"values\": []}")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isOk());

        // Subscribing from account1 and account2, checking
        mockMvc.perform(post(String.format("/task/%s/subscriber", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(post(String.format("/task/%s/subscriber", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account1Id)).exists(),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account2Id)).exists());

        // Unsubscribing from account1, checking
        mockMvc.perform(delete(String.format("/task/%s/subscriber", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account1Id)).doesNotExist(),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account2Id)).exists());

        // Subscribing from account1 and account2 again
        mockMvc.perform(post(String.format("/task/%s/subscriber", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(post(String.format("/task/%s/subscriber", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account1Id)).exists(),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account2Id)).exists());


        // Deleting account1, checking that it has been removed from the subscribers list
        mockMvc.perform(delete(String.format("/account/%s", account1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s", account1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/task/%s/subscribers", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account1Id)).doesNotExist(),
                jsonPath(String.format("$.values[?(@.id == %s)].name", account2Id)).exists());

        mockMvc.perform(get(String.format("/account/%s/tasks/subscribed", account2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpectAll(jsonPath(String.format("$.tasks[?(@.id == %s)]", taskId)).exists(),
                jsonPath("$.count").value(1));

        // Deleting task and checking that all subscriptions have been removed as well
        mockMvc.perform(delete(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/account/%s", account2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s/tasks/subscribed", account2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(jsonPath("$.tasks").isEmpty());
    }

    @Test
    @Order(9)
    public void taskStatus() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);
        String listId = utils.createList(groupId, ownerToken);

        String taskId = mockMvc.perform(post("/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("$.status").value(Task.Status.NOT_COMPLETED.name()));

        mockMvc.perform(post(String.format("/task/%s/status", taskId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("status", Task.Status.COMPLETED.name())))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("$.status").value(Task.Status.COMPLETED.name()));
    }
}
