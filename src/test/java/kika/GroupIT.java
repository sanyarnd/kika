package kika;

import java.util.Map;
import kika.configuration.security.SecurityConfiguration;
import kika.domain.AccountRole;
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
import static kika.JsonUtils.numericList;
import static kika.JsonUtils.writeJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GroupIT extends AbstractIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    @Order(1)
    public void createGroup() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);

        String groupId = utils.createGroup(ownerId, ownerToken);

        mockMvc.perform(get(String.format("/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("$.ownerId").value(ownerId));

        mockMvc.perform(get(String.format("/group/%s/members", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.members[?(@.id == %s)].role", ownerId)).value(AccountRole.Role.OWNER.name()));
    }

    @Test
    @Order(2)
    public void renameGroup() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);

        String groupId = utils.createGroup(ownerId, ownerToken);

        mockMvc.perform(post(String.format("/group/%s/rename", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "kate's group")))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(status().isOk(),
                jsonPath("$.name").value("kate's group"),
                jsonPath("$.ownerId").value(ownerId));
    }

    @Test
    @Order(3)
    public void deleteGroup() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);

        String accountId = utils.createAccount();
        JwtToken accountToken = utils.getToken(accountId);

        String groupId = utils.createGroup(ownerId, ownerToken);

        String listId = utils.createList(groupId, ownerToken);
        String taskId = utils.createTask(listId, ownerToken);

        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", accountId, "role", AccountRole.Role.MEMBER.name())))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(delete(String.format("/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/account/%s", ownerId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/account/%s", accountId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, accountToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/list/%s", listId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());
        mockMvc.perform(get(String.format("/task/%s", taskId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Order(4)
    public void changeAccountRoleTransferOwnership() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        // Also in the group
        String account1Id = utils.createAccount();
        JwtToken account1Token = utils.getToken(account1Id);
        // Not in the group
        String account2Id = utils.createAccount();
        JwtToken account2Token = utils.getToken(account2Id);

        String groupId = utils.createGroup(ownerId, ownerToken);

        // Trying to add a member with "OWNER" role => error
        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id, "role", AccountRole.Role.OWNER.name())))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        // Trying to change a non-member's role => error
        mockMvc.perform(post(String.format("/group/%s/member/%s/role", groupId, account2Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("role", AccountRole.Role.MEMBER.name())))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(status().isInternalServerError());

        // Adding a member with "RESTRICTED" (read-only) role => success
        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id, "role", AccountRole.Role.RESTRICTED.name())))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/group/%s/members", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.members[?(@.id == %s)].role", ownerId)).value(AccountRole.Role.OWNER.name()),
                jsonPath(String.format("$.members[?(@.id == %s)].role", account1Id)).value(
                    AccountRole.Role.RESTRICTED.name()));

        // Trying to add a member through a non-owner account => error
        mockMvc.perform(post(String.format("/group/%s/member/%s/role", groupId, account2Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("role", AccountRole.Role.MEMBER.name())))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(status().isUnauthorized());

        // Making restricted member an owner => success, previous owner is now a regular member
        mockMvc.perform(post(String.format("/group/%s/owner", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", account1Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(status().isOk());

        mockMvc.perform(get(String.format("/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(jsonPath("ownerId").value(account1Id));

        mockMvc.perform(get(String.format("/group/%s/members", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.members[?(@.id == %s)].role", ownerId)).value(AccountRole.Role.MEMBER.name()),
                jsonPath(String.format("$.members[?(@.id == %s)].role", account1Id)).value(
                    AccountRole.Role.OWNER.name()));

        // Trying to change owner's role to regular member's one => error
        mockMvc.perform(post(String.format("/group/%s/member/%s/role", groupId, account1Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("role", AccountRole.Role.MEMBER.name())))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(status().isInternalServerError());

        // Changing former owner's role to restricted => success
        mockMvc.perform(post(String.format("/group/%s/member/%s/role", groupId, ownerId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("role", AccountRole.Role.RESTRICTED.name())))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(status().isOk());

        mockMvc.perform(get(String.format("/account/%s/groups", ownerId))
            .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.groups[?(@.id == %s)].role", groupId)).value(
                AccountRole.Role.RESTRICTED.name()));
    }

    @Test
    @Order(5)
    public void excludeMember() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        // Also in the group
        String account1Id = utils.createAccount();
        JwtToken account1Token = utils.getToken(account1Id);
        // Not in the group
        String account2Id = utils.createAccount();
        JwtToken account2Token = utils.getToken(account2Id);

        String groupId = utils.createGroup(ownerId, ownerToken);
        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        // List1, owner and account1 have access to it
        String list1Id = utils.createList(groupId, ownerToken);
        mockMvc.perform(post(String.format("/list/%s/accounts", list1Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id, ownerId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        // List2, only account1 has access to it
        String list2Id = utils.createList(groupId, ownerToken);
        mockMvc.perform(post(String.format("/list/%s/accounts", list2Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"values\": %s}", numericList(account1Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());

        // Trying to exclude from group an account that is not its member => error
        mockMvc.perform(delete(String.format("/group/%s/member/%s", groupId, account2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        // Trying to exclude from group through a non-member account => error
        mockMvc.perform(delete(String.format("/group/%s/member/%s", groupId, account1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isUnauthorized());

        // Trying to exclude owner from group => error
        mockMvc.perform(delete(String.format("/group/%s/member/%s", groupId, ownerId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        // Trying to exclude a regular member => success
        mockMvc.perform(delete(String.format("/group/%s/member/%s", groupId, account1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        // Owner still has access to list1, but account1 does not anymore
        mockMvc.perform(get(String.format("/list/%s/accounts", list1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", ownerId)).exists(),
                jsonPath(String.format("$.accounts[?(@.id == %s)]", account1Id)).doesNotExist());

        // List2 gets deleted when account1 is excluded from the group
        mockMvc.perform(get(String.format("/list/%s", list2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());
    }
}
