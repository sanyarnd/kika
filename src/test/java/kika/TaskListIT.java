package kika;

import kika.domain.AccountRole;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static kika.JsonUtils.numericList;
import static kika.JsonUtils.writeJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TaskListIT extends AbstractIT {
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

    public String createTask(String listId) throws Exception {
        return mockMvc.perform(post("/task/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "task", "listId", listId))))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @Order(1)
    public void createList() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);

        mockMvc.perform(post("/list/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "list", "groupId", groupId))))
                .andExpectAll(status().isOk());
    }

    @Test
    @Order(2)
    public void renameList() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);

        String listId = mockMvc.perform(post("/list/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "list", "groupId", groupId))))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post(String.format("/list/%s/rename", listId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("value", "kate's list"))))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/list/%s", listId)))
                .andExpectAll(jsonPath("$.childrenIds").isEmpty(),
                        jsonPath("$.parentId").isEmpty(),
                        jsonPath("$.name").value("kate's list"));
    }

    @Test
    @Order(3)
    public void deleteList() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);

        String parentListId = mockMvc.perform(post("/list/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "list", "groupId", groupId))))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(delete(String.format("/list/%s", parentListId)))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/list/%s", parentListId)))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/group/%s", groupId)))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s", ownerId)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    public void deleteChildDeleteParentList() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);

        String parentListId = mockMvc.perform(post("/list/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "list", "groupId", groupId))))
                .andReturn().getResponse().getContentAsString();

        String childList1Id = mockMvc.perform(post("/list/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "list", "groupId", groupId, "parentId", parentListId))))
                .andReturn().getResponse().getContentAsString();

        String childList2Id = mockMvc.perform(post("/list/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "list", "groupId", groupId, "parentId", parentListId))))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(delete(String.format("/list/%s", childList1Id)))
                .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/list/%s", childList1Id)))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/list/%s", parentListId)))
                .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/list/%s", childList2Id)))
                .andExpect(status().isOk());

        mockMvc.perform(delete(String.format("/list/%s", parentListId)))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/list/%s", parentListId)))
                .andExpect(status().is5xxServerError());
        mockMvc.perform(get(String.format("/list/%s", childList2Id)))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/group/%s/lists", groupId)))
                .andExpectAll(jsonPath("$.lists").isEmpty(),
                        jsonPath("$.count").value(0));
    }

    @Test
    @Order(5)
    public void specialAccess() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);
        String account1Id = createAccount();
        String account2Id = createAccount();

        // Add the other two accounts to the group
        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("id", account1Id))))
                .andExpect(status().isOk());
        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("id", account2Id))))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/group/%s/members", groupId)))
                .andExpectAll(jsonPath("$.count").value(3),
                        jsonPath(String.format("$.members[?(@.id == %s)].role", ownerId)).value(AccountRole.Role.OWNER.name()),
                        jsonPath(String.format("$.members[?(@.id == %s)].role", account1Id)).value(AccountRole.Role.MEMBER.name()),
                        jsonPath(String.format("$.members[?(@.id == %s)].role", account2Id)).value(AccountRole.Role.MEMBER.name()));

        // Create a list with a child, check that both have full access
        String parentListId = mockMvc.perform(post("/list/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "list", "groupId", groupId))))
                .andReturn().getResponse().getContentAsString();
        String childListId = mockMvc.perform(post("/list/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "list", "groupId", groupId, "parentId", parentListId))))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get(String.format("/list/%s/accounts", parentListId)))
                .andExpectAll(jsonPath("$.count").value(3),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", ownerId)).exists(),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", account1Id)).exists(),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());
        mockMvc.perform(get(String.format("/list/%s/accounts", childListId)))
                .andExpectAll(jsonPath("$.count").value(3),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", ownerId)).exists(),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", account1Id)).exists(),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());

        // Add a task to each list, check
        String task1Id = createTask(parentListId);
        String task2Id = createTask(childListId);

        mockMvc.perform(get(String.format("/task/%s", task1Id)))
                .andExpect(jsonPath("$.listId").value(parentListId));
        mockMvc.perform(get(String.format("/task/%s", task2Id)))
                .andExpect(jsonPath("$.listId").value(childListId));

        // Restrict parent list's access to account1 and account2, check
        mockMvc.perform(post(String.format("/list/%s/accounts", parentListId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"values\": %s}", numericList(account1Id, account2Id))))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/list/%s/accounts", parentListId)))
                .andExpectAll(jsonPath("$.count").value(2),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", account1Id)).exists(),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());

        // Check that child has inherited access rights
        mockMvc.perform(get(String.format("/list/%s/accounts", childListId)))
                .andExpectAll(jsonPath("$.count").value(2),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", account1Id)).exists(),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());

        // Restrict child list's access to account2, check
        mockMvc.perform(post(String.format("/list/%s/accounts", childListId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"values\": %s}", numericList(account2Id))))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/list/%s/accounts", childListId)))
                .andExpectAll(jsonPath("$.count").value(1),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());

        // Check that account1 still has access to parent list
        mockMvc.perform(get(String.format("/list/%s/accounts", parentListId)))
                .andExpectAll(jsonPath("$.count").value(2),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", account1Id)).exists(),
                        jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());

        // Delete account2 and check that child list also gets deleted along with its task
        // (because only account2 has access to it)
        mockMvc.perform(delete(String.format("/account/%s", account2Id)))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s", account2Id)))
                .andExpect(status().is5xxServerError());
        mockMvc.perform(get(String.format("/list/%s", childListId)))
                .andExpect(status().is5xxServerError());
        mockMvc.perform(get(String.format("/task/%s", task2Id)))
                .andExpect(status().is5xxServerError());

        // Delete group owner account and check that group, parent list and its task also get deleted
        mockMvc.perform(delete(String.format("/account/%s", ownerId)))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s", ownerId)))
                .andExpect(status().is5xxServerError());
        mockMvc.perform(get(String.format("/group/%s", groupId)))
                .andExpect(status().is5xxServerError());
        mockMvc.perform(get(String.format("/account/%s", parentListId)))
                .andExpect(status().is5xxServerError());
        mockMvc.perform(get(String.format("/task/%s", task1Id)))
                .andExpect(status().is5xxServerError());

        // Account1 has no associated groups
        mockMvc.perform(get(String.format("/account/%s/groups", account1Id)))
                .andExpectAll(jsonPath("$.count").value(0),
                        jsonPath("$.groups").isEmpty());
    }
}
