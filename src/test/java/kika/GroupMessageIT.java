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
import static kika.JsonUtils.writeJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GroupMessageIT extends AbstractIT {
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

    @Test
    @Order(1)
    public void createMessage() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);
        // Also in the group
        String account1Id = createAccount();
        // Not in the group
        String account2Id = createAccount();

        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id))))
            .andExpect(status().isOk());

        // Sending a message from an account that's not in the group => error
        mockMvc.perform(post(String.format("/group/%s/member/%s/message", groupId, account2Id))
            .contentType(MediaType.APPLICATION_JSON)
            .content(writeJson(Map.of("body", "New group message"))))
            .andExpect(status().is5xxServerError());

        // Sending a message from a member account => success
        String messageId = mockMvc.perform(post(String.format("/group/%s/member/%s/message", groupId, account1Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "New group message"))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get(String.format("/message/%s", messageId)))
            .andExpectAll(jsonPath("$.id").value(messageId),
                jsonPath("$.groupId").value(groupId),
                jsonPath("$.body").value("New group message"));

        mockMvc.perform(get(String.format("/group/%s/messages", groupId)))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.messages[?(@.id == %s)].groupId", messageId)).value(Integer.parseInt(groupId)),
                jsonPath(String.format("$.messages[?(@.id == %s)].body", messageId)).value("New group message"));
    }

    @Test
    @Order(2)
    public void deleteMessage() throws Exception {
        String ownerId = createAccount();
        String groupId = createGroup(ownerId);
        // Also in the group
        String account1Id = createAccount();
        // Not in the group
        String account2Id = createAccount();

        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("id", account1Id))))
            .andExpect(status().isOk());

        String message1Id = mockMvc.perform(post(String.format("/group/%s/member/%s/message", groupId, account1Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "New group message1"))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String message2Id = mockMvc.perform(post(String.format("/group/%s/member/%s/message", groupId, account1Id))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "New group message2"))))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        mockMvc.perform(get(String.format("/group/%s/messages", groupId)))
            .andExpectAll(jsonPath("$.count").value(2),
                jsonPath(String.format("$.messages[?(@.id == %s)]", message1Id)).exists(),
                jsonPath(String.format("$.messages[?(@.id == %s)]", message2Id)).exists());

        // Trying to delete a message from account that's not in the group => error
        mockMvc.perform(delete(String.format("/message/%s/account/%s", message1Id, account2Id)))
            .andExpect(status().is5xxServerError());

        // Deleting a message from a different account than the on it was created from => success
        mockMvc.perform(delete(String.format("/message/%s/account/%s", message1Id, ownerId)))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/group/%s/messages", groupId)))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.messages[?(@.id == %s)]", message1Id)).doesNotExist(),
                jsonPath(String.format("$.messages[?(@.id == %s)]", message2Id)).exists());

        // Deleting account does not affect messages that were created from it
        mockMvc.perform(delete(String.format("/account/%s", account1Id)))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/group/%s/messages", groupId)))
            .andExpectAll(jsonPath("$.count").value(1),
                jsonPath(String.format("$.messages[?(@.id == %s)]", message2Id)).exists());

        // Deleting a group erases all of its messages
        mockMvc.perform(delete(String.format("/group/%s", groupId)))
            .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/group/%s", groupId)))
            .andExpect(status().is5xxServerError());
        mockMvc.perform(get(String.format("/message/%s", message2Id)))
            .andExpect(status().is5xxServerError());
    }
}
