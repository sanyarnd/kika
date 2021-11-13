package kika;

import java.util.Map;
import java.util.Random;
import kika.configuration.security.SecurityConfiguration;
import kika.domain.Account;
import kika.repository.AccountRepository;
import kika.security.jwt.encode.JwtToken;
import kika.security.jwt.encode.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static kika.JsonUtils.writeJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RequiredArgsConstructor
public class TestUtils {
    private final MockMvc mockMvc;
    private final AccountRepository accountRepository;
    private final JwtTokenService jwtTokenService;
    private final Random random = new Random();

    public String createAccount() {
        return String.valueOf(
            accountRepository.save(new Account("kate", Account.Provider.GITHUB, String.valueOf(random.nextLong())))
                .safeId());
    }

    public JwtToken getToken(String id) {
        return jwtTokenService.generate(Long.parseLong(id));
    }

    public String createGroup(String ownerId, JwtToken ownerToken) throws Exception {
        return mockMvc.perform(post("/api/group/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("value", "group")))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, ownerToken.accessToken()))
            .andReturn().getResponse().getContentAsString();
    }

    public String createList(String groupId, JwtToken creatorToken) throws Exception {
        return mockMvc.perform(post("/api/list/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "list", "groupId", groupId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, creatorToken.accessToken()))
            .andReturn().getResponse().getContentAsString();
    }

    public String createTask(String listId, JwtToken creatorToken) throws Exception {
        return mockMvc.perform(post("/api/task/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeJson(Map.of("name", "task", "listId", listId)))
                .header(SecurityConfiguration.ACCESS_TOKEN_HEADER_NAME, creatorToken.accessToken()))
            .andReturn().getResponse().getContentAsString();
    }
}
