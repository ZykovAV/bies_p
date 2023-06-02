package ylab.bies.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import ylab.bies.userservice.dto.RegisterRequest;
import ylab.bies.userservice.service.KeycloakService;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ylab.bies.userservice.controller.UserTestUtil.getValidRegisterRequest;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class UserContactsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @MockBean
    private KeycloakService keycloakService;

    @Nested
    class RollbackTests {
        @AfterEach
        void setUp() {
            jdbcTemplate.execute("TRUNCATE TABLE users CASCADE");
        }

        @Test
        void getUserContactById_Successfully() throws Exception {
            UUID userId = UUID.randomUUID();
            RegisterRequest request = getValidRegisterRequest();
            registerUser(userId, request);

            mockMvc.perform(get("/api/v1/users/{id}/contacts", userId)
                            .with(jwt().authorities(AuthorityUtils.createAuthorityList("ROLE_SERVICE")))
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email", equalTo(request.getEmail())))
                    .andExpect(jsonPath("$.firstName", equalTo(request.getFirstName())));
        }
    }

    @Test
    void getUserContactById_Unauthorized() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/users/{id}/contacts", userId))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserContactById_Forbidden_NotAService() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/users/{id}/contacts", userId)
                        .with(jwt())
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserContactById_UserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/users/{id}/contacts", userId)
                        .with(jwt().authorities(AuthorityUtils.createAuthorityList("ROLE_SERVICE")))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "1", "asd"})
    void getUserContactById_InvalidUserId(String userId) throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}/contacts", userId)
                        .with(jwt().authorities(AuthorityUtils.createAuthorityList("ROLE_SERVICE")))
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private void registerUser(UUID userId, RegisterRequest request) throws Exception {
        when(keycloakService.register(any(UserRepresentation.class))).thenReturn(
                Response.created(URI.create("/" + userId)).build()
        );
        doNothing().when(keycloakService).assignRoles(eq(String.valueOf(userId)), anySet());

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
}