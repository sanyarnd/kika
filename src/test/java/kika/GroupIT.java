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
public class GroupIT extends AbstractIT {
    @Autowired
    private MockMvc mockMvc;

    public String registerAccount() throws Exception {
        return mockMvc.perform(post("/account/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "kate"))))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @Order(1)
    public void createGroup() throws Exception {
        String ownerId = registerAccount();
        String id = mockMvc.perform(post("/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "group", "ownerId", ownerId))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get(String.format("/group/%s", id)))
                .andExpectAll(jsonPath("$.name").value("group"),
                        jsonPath("$.ownerId").value(ownerId));

        mockMvc.perform(get(String.format("/group/%s/members", id)))
                .andExpectAll(jsonPath("$.count").value(1),
                        jsonPath(String.format("$.members[?(@.id == %s)].role", ownerId)).value(AccountRole.Role.OWNER.name()));
    }

    @Test
    @Order(2)
    public void renameGroup() throws Exception {
        String ownerId = registerAccount();
        String id = mockMvc.perform(post("/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "group", "ownerId", ownerId))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post(String.format("/group/%s/rename", id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("value", "kate's group"))))
                .andExpectAll(status().isOk(), content().string(""));

        mockMvc.perform(get(String.format("/group/%s", id)))
                .andExpectAll(status().isOk(),
                        jsonPath("$.name").value("kate's group"),
                        jsonPath("$.ownerId").value(ownerId));
    }

    @Test
    @Order(3)
    public void deleteGroup() throws Exception {
        String ownerId = registerAccount();
        String accountId = registerAccount();
        String groupId = mockMvc.perform(post("/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "group", "ownerId", ownerId))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("id", accountId, "role", AccountRole.Role.MEMBER.name()))))
                .andExpect(status().isOk());

        mockMvc.perform(delete(String.format("/group/%s", groupId)))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/group/%s", groupId)))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(get(String.format("/account/%s", ownerId)))
                .andExpect(status().isOk());
        mockMvc.perform(get(String.format("/account/%s", accountId)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    public void changeAccountRoleTransferOwnership() throws Exception {
        String ownerId = registerAccount();
        // Also in the group
        String account1Id = registerAccount();
        // Not in the group
        String account2Id = registerAccount();

        String groupId = mockMvc.perform(post("/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("name", "group", "ownerId", ownerId))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Trying to add a member with "OWNER" role => error
        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("id", account1Id, "role", AccountRole.Role.OWNER.name()))))
                .andExpect(status().is5xxServerError());

        // Trying to change a non-member's role => error
        mockMvc.perform(post(String.format("/group/%s/member/%s/role", groupId, account2Id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("role", AccountRole.Role.MEMBER.name()))))
                .andExpectAll(status().is5xxServerError());

        // Adding a member with "RESTRICTED" (read-only) role => success
        mockMvc.perform(post(String.format("/group/%s/member", groupId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("id", account1Id, "role", AccountRole.Role.RESTRICTED.name()))))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/group/%s/members", groupId)))
                .andExpectAll(jsonPath("$.count").value(2),
                        jsonPath(String.format("$.members[?(@.id == %s)].role", ownerId)).value(AccountRole.Role.OWNER.name()),
                        jsonPath(String.format("$.members[?(@.id == %s)].role", account1Id)).value(AccountRole.Role.RESTRICTED.name()));

        // Making restricted member an owner => success, previous owner is now a regular member
        mockMvc.perform(post(String.format("/group/%s/owner", groupId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("value", account1Id))))
                .andExpectAll(status().isOk());

        mockMvc.perform(get(String.format("/group/%s", groupId)))
                .andExpect(jsonPath("ownerId").value(account1Id));

        mockMvc.perform(get(String.format("/group/%s/members", groupId)))
                .andExpectAll(jsonPath("$.count").value(2),
                        jsonPath(String.format("$.members[?(@.id == %s)].role", ownerId)).value(AccountRole.Role.MEMBER.name()),
                        jsonPath(String.format("$.members[?(@.id == %s)].role", account1Id)).value(AccountRole.Role.OWNER.name()));

        // Trying to change owner's role to regular member's one => error
        mockMvc.perform(post(String.format("/group/%s/member/%s/role", groupId, account1Id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writeJson(Map.of("role", AccountRole.Role.MEMBER.name()))))
                .andExpectAll(status().is5xxServerError());
    }
}
