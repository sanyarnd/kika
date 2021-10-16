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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AccountIT extends AbstractIT {
    @Autowired
    private MockMvc mockMvc;

    public String createGroup(String ownerId) throws Exception {
        return mockMvc.perform(post("/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "group", "ownerId", ownerId))))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @Order(1)
    public void createAccount() throws Exception {
        String id = mockMvc.perform(post("/account/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "kate"))))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get(String.format("/account/%s", id)))
                .andExpectAll(status().isOk(), jsonPath("$.name").value("kate"));
    }

    @Test
    @Order(2)
    public void renameAccount() throws Exception {
        String id = mockMvc.perform(post("/account/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "kate"))))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post(String.format("/account/%s/rename", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("value", "kate kate"))))
                .andExpectAll(status().isOk(), content().string(""));

        mockMvc.perform(get(String.format("/account/%s", id)))
                .andExpectAll(status().isOk(), jsonPath("$.name").value("kate kate"));
    }

    @Test
    @Order(3)
    public void deleteAccount() throws Exception {
        String ownerId = mockMvc.perform(post("/account/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "kate"))))
                .andReturn().getResponse().getContentAsString();

        String accountId = mockMvc.perform(post("/account/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "kate"))))
                .andReturn().getResponse().getContentAsString();

        String groupId = createGroup(ownerId);

        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("id", accountId))))
                .andExpect(status().isOk());

        mockMvc.perform(delete(String.format("/account/%s", accountId)))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/group/%s", groupId)))
                .andExpect(status().isOk());

        mockMvc.perform(delete(String.format("/account/%s", ownerId)))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s", ownerId)))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/group/%s", ownerId)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @Order(4)
    public void accountGroups() throws Exception {
        String ownerId = mockMvc.perform(post("/account/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "kate"))))
                .andReturn().getResponse().getContentAsString();

        String accountId = mockMvc.perform(post("/account/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "kate"))))
                .andReturn().getResponse().getContentAsString();

        String groupId = createGroup(ownerId);

        mockMvc.perform(get(String.format("/account/%s/groups", accountId)))
                .andExpectAll(jsonPath("$.count").value(0),
                        jsonPath("$.groups").isEmpty());

        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("id", accountId))))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/account/%s/groups", accountId)))
                .andExpectAll(jsonPath("$.count").value(1),
                        jsonPath(String.format("$.groups[?(@.id == %s)].role", groupId)).value(AccountRole.Role.MEMBER.name()));

        mockMvc.perform(get(String.format("/account/%s/groups", ownerId)))
                .andExpectAll(jsonPath("$.count").value(1),
                        jsonPath(String.format("$.groups[?(@.id == %s)].role", groupId)).value(AccountRole.Role.OWNER.name()));
    }
}
