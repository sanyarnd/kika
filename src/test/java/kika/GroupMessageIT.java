package kika;

import java.util.Map;
import kika.configuration.security.SecurityConfiguration;
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
import static kika.JsonUtils.writeJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GroupMessageIT extends AbstractIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    @Order(1)
    public void createMessage() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);

        // Also in the group
        String account1Id = utils.createAccount();
        JwtToken account1Token = utils.getToken(account1Id);
        // Not in the group
        String account2Id = utils.createAccount();
        JwtToken account2Token = utils.getToken(account2Id);

        mockMvc.perform(post(String.format("/api/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        // Sending a message from an account that's not in the group => error
        mockMvc.perform(post(String.format("/api/group/%s/message", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "New group message")))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isUnauthorized());

        // Sending a message from a member account => success
        String messageId = mockMvc.perform(post(String.format("/api/group/%s/message", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "New group message")))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get(String.format("/api/message/%s", messageId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.id").value(messageId),
                jsonPath("$.groupId").value(groupId),
                jsonPath("$.body").value("New group message"));

        mockMvc.perform(get(String.format("/api/group/%s/messages", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.messages[?(@.id == %s)].groupId", messageId)).value(
                    Integer.parseInt(groupId)),
                jsonPath(String.format("$.messages[?(@.id == %s)].body", messageId)).value("New group message"));
    }

    @Test
    @Order(2)
    public void deleteMessage() throws Exception {
        TestUtils utils = new TestUtils(mockMvc, accountRepository, jwtTokenService);
        String ownerId = utils.createAccount();
        JwtToken ownerToken = utils.getToken(ownerId);
        String groupId = utils.createGroup(ownerId, ownerToken);

        // Also in the group
        String account1Id = utils.createAccount();
        JwtToken account1Token = utils.getToken(account1Id);
        // Not in the group
        String account2Id = utils.createAccount();
        JwtToken account2Token = utils.getToken(account2Id);

        mockMvc.perform(post(String.format("/api/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());

        String message1Id = mockMvc.perform(post(String.format("/api/group/%s/message", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "New group message1")))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String message2Id = mockMvc.perform(post(String.format("/api/group/%s/message", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "New group message2")))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        mockMvc.perform(get(String.format("/api/group/%s/messages", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.messages[?(@.id == %s)]", message1Id)).exists(),
                jsonPath(String.format("$.messages[?(@.id == %s)]", message2Id)).exists());

        // Trying to delete a message from account that's not in the group => error
        mockMvc.perform(delete(String.format("/api/message/%s", message1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account2Token.accessToken()))
            .andExpect(status().isUnauthorized());

        // Deleting a message from a different account than the on it was created from => success
        mockMvc.perform(delete(String.format("/api/message/%s", message1Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/api/group/%s/messages", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.messages[?(@.id == %s)]", message1Id)).doesNotExist(),
                jsonPath(String.format("$.messages[?(@.id == %s)]", message2Id)).exists());

        // Deleting account does not affect messages that were created from it
        mockMvc.perform(delete("/api/account")
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, account1Token.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/api/group/%s/messages", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.messages[?(@.id == %s)]", message2Id)).exists());

        // Deleting a group erases all of its messages
        mockMvc.perform(delete(String.format("/api/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/api/group/%s", groupId))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());
        mockMvc.perform(get(String.format("/api/message/%s", message2Id))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andExpect(status().isInternalServerError());
    }
}
