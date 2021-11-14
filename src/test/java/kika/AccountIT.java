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
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static kika.JsonUtils.writeJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AccountIT extends AbstractIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    @Order(1)
    public void createAccount() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String id = utils.createAccount();
        JwtToken token = utils.getToken(id);

        mockMvc.perform(get("/api/account"))
            .andExpectAll(status().isUnauthorized());

        mockMvc.perform(get("/api/account")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, token.accessToken()))
            .andExpectAll(status().isOk());
    }

    @Test
    @Order(2)
    public void renameAccount() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String id = utils.createAccount();
        JwtToken token = utils.getToken(id);

        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "kate kate")))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, token.accessToken()))
            .andExpectAll(status().isOk());

        mockMvc.perform(get("/api/account")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, token.accessToken()))
            .andExpectAll(status().isOk(), jsonPath("$.name").value("kate kate"));
    }

    @Test
    @Order(3)
    public void deleteAccount() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);

        String accountId = utils.createAccount();
        JwtToken accountToken = utils.getToken(accountId);

        String groupId = utils.createGroup(ownerId, ownerToken);

        mockMvc.perform(post(String.format("/api/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", accountId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/account")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, accountToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/api/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/account")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/account")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());

        mockMvc.perform(get(String.format("/api/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, accountToken.accessToken()))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Order(4)
    public void accountGroups() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);

        String accountId = utils.createAccount();
        JwtToken accountToken = utils.getToken(accountId);

        String groupId = utils.createGroup(ownerId, ownerToken);

        mockMvc.perform(get("/api/account/groups")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, accountToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(0),
                jsonPath("$.groups").isEmpty());

        mockMvc.perform(post(String.format("/api/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", accountId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/account/groups")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, accountToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.groups[?(@.id == %s)].role", groupId)).value(AccountRole.Role.MEMBER.name()));

        mockMvc.perform(get("/api/account/groups")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.groups[?(@.id == %s)].role", groupId)).value(AccountRole.Role.OWNER.name()));
    }
}
