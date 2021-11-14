package kika;

import java.util.Map;
import kika.configuration.security.SecurityConfiguration;
import kika.domain.AccountRole;
import kika.repository.AccountRepository;
import kika.security.jwt.encode.JwtToken;
import kika.security.jwt.encode.JwtTokenService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static kika.JsonUtils.numericList;
import static kika.JsonUtils.writeJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.hamcrest.Matchers.nullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TaskListIT extends AbstractIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    @Order(1)
    public void createList() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);

        mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(status().isOk());
    }

    @Test
    @Order(2)
    public void renameList() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);

        String listId = mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post(String.format("/api/list/%s/rename", listId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "kate's list")))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/api/list/%s", listId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.childrenIds").isEmpty(),
                jsonPath("$.parentId").isEmpty(),
                jsonPath("$.name").value("kate's list"));
    }

    @Test
    @Order(3)
    public void deleteList() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);

        String parentListId = mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(delete(String.format("/api/list/%s", parentListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/api/list/%s", parentListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/api/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/account")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    public void deleteChildDeleteParentList() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);

        String parentListId = mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        String childList1Id = mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId, "parentId", parentListId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        String childList2Id = mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId, "parentId", parentListId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(delete(String.format("/api/list/%s", childList1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/api/list/%s", childList1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/api/list/%s", parentListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/api/list/%s", childList2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(delete(String.format("/api/list/%s", parentListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/api/list/%s", parentListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());
        mockMvc.perform(get(String.format("/api/list/%s", childList2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/api/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    public void specialAccess() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);

        String account1Id = utils.createAccount();
        JwtToken account1Token = utils.getToken(account1Id);
        String account2Id = utils.createAccount();
        JwtToken account2Token = utils.getToken(account2Id);

        // Add the other two accounts to the group
        mockMvc.perform(post(String.format("/api/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(post(String.format("/api/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/api/group/%s/members", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(3),
                jsonPath(String.format("$.members[?(@.id == %s)].role", ownerId)).value(AccountRole.Role.OWNER.name()),
                jsonPath(String.format("$.members[?(@.id == %s)].role", account1Id)).value(
                    AccountRole.Role.MEMBER.name()),
                jsonPath(String.format("$.members[?(@.id == %s)].role", account2Id)).value(
                    AccountRole.Role.MEMBER.name()));

        // Create a list with a child, check that both have full access
        String parentListId = mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();
        String childListId = mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId, "parentId", parentListId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get(String.format("/api/list/%s/accounts", parentListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(3),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", ownerId)).exists(),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account1Id)).exists(),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());
        mockMvc.perform(get(String.format("/api/list/%s/accounts", childListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(3),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", ownerId)).exists(),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account1Id)).exists(),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());

        // Check for access from account side
        mockMvc.perform(get(String.format("/api/account/group/%s/lists", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.lists[?(@.id == %s)]", parentListId)).exists(),
                jsonPath(String.format("$.lists[?(@.id == %s)]", childListId)).exists());
        mockMvc.perform(get(String.format("/api/account/group/%s/lists", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.lists[?(@.id == %s)]", parentListId)).exists(),
                jsonPath(String.format("$.lists[?(@.id == %s)]", childListId)).exists());
        mockMvc.perform(get(String.format("/api/account/group/%s/lists", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.lists[?(@.id == %s)]", parentListId)).exists(),
                jsonPath(String.format("$.lists[?(@.id == %s)]", childListId)).exists());

        // Add a task to each list, check
        String task1Id = utils.createTask(parentListId, ownerToken);
        String task2Id = utils.createTask(childListId, ownerToken);

        mockMvc.perform(get(String.format("/api/task/%s", task1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("$.listId").value(parentListId));
        mockMvc.perform(get(String.format("/api/task/%s", task2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("$.listId").value(childListId));

        // Restrict parent list's access to account1 and account2, check
        // Also check that excluding yourself is possible
        mockMvc.perform(post(String.format("/api/list/%s/accounts", parentListId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id, account2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        // From account side
        mockMvc.perform(get(String.format("/api/account/group/%s/lists", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath(String.format("$.lists[?(@.id == %s)]", parentListId)).doesNotExist());
        mockMvc.perform(get(String.format("/api/account/group/%s/lists", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.lists[?(@.id == %s)]", parentListId)).exists(),
                jsonPath(String.format("$.lists[?(@.id == %s)]", childListId)).exists());
        mockMvc.perform(get(String.format("/api/account/group/%s/lists", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.lists[?(@.id == %s)]", parentListId)).exists(),
                jsonPath(String.format("$.lists[?(@.id == %s)]", childListId)).exists());

        // Check that adding the same accounts twice does not cause errors
        mockMvc.perform(post(String.format("/api/list/%s/accounts", parentListId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id, account2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/api/list/%s/accounts", parentListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account1Id)).exists(),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());

        // ...but is not possible from an account that has no access
        mockMvc.perform(post(String.format("/api/list/%s/accounts", parentListId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id, account2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isUnauthorized());

        // Check that child has inherited access rights
        mockMvc.perform(get(String.format("/api/list/%s/accounts", childListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account1Id)).exists(),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());
        mockMvc.perform(get(String.format("/api/list/%s/accounts", childListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isUnauthorized());

        // From account side
        mockMvc.perform(get(String.format("/api/account/group/%s/lists", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath(String.format("$.lists[?(@.id == %s)]", childListId)).doesNotExist(),
                jsonPath("$.count").value(0));

        // Restrict child list's access to account2, check
        mockMvc.perform(post(String.format("/api/list/%s/accounts", childListId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/api/list/%s/accounts", childListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());

        // From account side
        mockMvc.perform(get(String.format("/api/account/group/%s/lists", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(jsonPath(String.format("$.lists[?(@.id == %s)]", childListId)).doesNotExist());
        mockMvc.perform(get(String.format("/api/account/group/%s/lists", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.lists[?(@.id == %s)]", parentListId)).exists(),
                jsonPath(String.format("$.lists[?(@.id == %s)]", childListId)).exists());

        // Check that account1 still has access to parent list
        mockMvc.perform(get(String.format("/api/list/%s/accounts", parentListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account1Id)).exists(),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account2Id)).exists());

        // From account side
        mockMvc.perform(get(String.format("/api/account/group/%s/lists", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(jsonPath(String.format("$.lists[?(@.id == %s)]", parentListId)).exists(),
                jsonPath("$.count").value(1));

        // Delete account2 and check that child list also gets deleted along with its task
        // (because only account2 has access to it)
        mockMvc.perform(delete("/api/account")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/account")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isInternalServerError());
        mockMvc.perform(get(String.format("/api/list/%s", childListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isInternalServerError());
        mockMvc.perform(get(String.format("/api/task/%s", task2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isInternalServerError());

        // Delete group owner account and check that group, parent list and its task also get deleted
        mockMvc.perform(delete("/api/account")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/account")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());
        mockMvc.perform(get(String.format("/api/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isInternalServerError());
        mockMvc.perform(get(String.format("/api/list/%s", parentListId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isInternalServerError());
        mockMvc.perform(get(String.format("/api/task/%s", task1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isInternalServerError());

        // Account1 has no associated groups or lists
        mockMvc.perform(get("/api/account/groups")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(0),
                jsonPath("$.groups").isEmpty());
        mockMvc.perform(get(String.format("/api/account/group/%s/lists", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(jsonPath(String.format("$.lists[?(@.id == %s)]", parentListId)).doesNotExist(),
                jsonPath(String.format("$.lists[?(@.id == %s)]", childListId)).doesNotExist(),
                jsonPath("$.count").value(0));
    }

    @Test
    @Order(6)
    public void moveList() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);

        String parentListId = mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        String childList1Id = mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId, "parentId", parentListId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        String childList2Id = mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId, "parentId", parentListId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();

        // Make child list 2 a child of child list 1
        mockMvc.perform(post("/api/list/%s/move".formatted(childList2Id))
            .contentType(MediaType.APPLICATION_JSON)
            .content(writeJson(Map.of("parentId", childList1Id)))
            .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/list/%s".formatted(childList2Id))
            .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("$.parentId").value(childList1Id));
        mockMvc.perform(get("/api/list/%s".formatted(childList1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("$.children[?(@.id == %s)]".formatted(childList2Id)).exists());

        // Trying to move a list into a child list => error
        mockMvc.perform(post("/api/list/%s/move".formatted(parentListId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("parentId", childList2Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        // Move child list 1 to group (= no parent)
        mockMvc.perform(post("/api/list/%s/move".formatted(childList1Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentId\": null}")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/list/%s".formatted(childList1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("$.parentId", Matchers.nullValue(), Long.class));
        mockMvc.perform(get("/api/group/%s/lists".formatted(groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath("$.lists[?(@.id == %s)].parentId".formatted(childList1Id), Matchers.nullValue(), Long.class));
    }
}
