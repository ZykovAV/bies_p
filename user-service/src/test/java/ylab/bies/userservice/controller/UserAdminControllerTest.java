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
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import ylab.bies.userservice.dto.RegisterRequest;
import ylab.bies.userservice.dto.RoleResponse;
import ylab.bies.userservice.dto.UserResponse;
import ylab.bies.userservice.service.KeycloakService;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ylab.bies.userservice.controller.UserTestUtil.getValidRegisterRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class UserAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @MockBean
    private KeycloakService keycloakService;
    private static final String USER_ID_CLAIM = "sub";
    private static final String EXPERT_ROLE = "EXPERT";
    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    @Nested
    class RollbackTests {
        @AfterEach
        void setUp() {
            jdbcTemplate.execute("TRUNCATE TABLE users CASCADE");
        }

        @Test
        void assignRole_Successfully() throws Exception {
            UUID userId = UUID.randomUUID();
            RegisterRequest request = getValidRegisterRequest();
            registerUser(userId, request);

            mockMvc.perform(patch("/api/v1/admin/users/{id}/roles/{roleName}", userId, EXPERT_ROLE)
                            .with(jwt().authorities(AuthorityUtils.createAuthorityList(ADMIN_AUTHORITY))))
                    .andDo(print())
                    .andExpect(status().isOk());

            MvcResult result = mockMvc.perform(get("/api/v1/users/profile")
                            .with(jwt()
                                    .jwt(jwt -> jwt.claim(USER_ID_CLAIM, String.valueOf(userId)))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();
            UserResponse response = mapper.readValue(
                    result.getResponse().getContentAsString(),
                    UserResponse.class
            );

            List<String> userRoles = response.getRoles()
                    .stream()
                    .map(RoleResponse::getName)
                    .collect(Collectors.toList());
            assertThat(userRoles).isEqualTo(Arrays.asList("USER", "EXPERT"));
        }

        @Test
        void assignRole_ThrowException_UserAlreadyHasRole() throws Exception {
            UUID userId = UUID.randomUUID();
            RegisterRequest request = getValidRegisterRequest();
            registerUser(userId, request);

            mockMvc.perform(patch("/api/v1/admin/users/{id}/roles/{roleName}", userId, EXPERT_ROLE)
                            .with(jwt().authorities(AuthorityUtils.createAuthorityList(ADMIN_AUTHORITY))))
                    .andDo(print())
                    .andExpect(status().isOk());

            mockMvc.perform(patch("/api/v1/admin/users/{id}/roles/{roleName}", userId, EXPERT_ROLE)
                            .with(jwt().authorities(AuthorityUtils.createAuthorityList(ADMIN_AUTHORITY))))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        void assignRole_ThrowException_RoleNotFound() throws Exception {
            UUID userId = UUID.randomUUID();
            RegisterRequest request = getValidRegisterRequest();
            registerUser(userId, request);

            mockMvc.perform(patch("/api/v1/admin/users/{id}/roles/{roleName}", userId, "INVALID")
                            .with(jwt().authorities(AuthorityUtils.createAuthorityList(ADMIN_AUTHORITY))))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void assignRole_ThrowException_UserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/users/{id}/roles/{roleName}", userId, EXPERT_ROLE)
                        .with(jwt().authorities(AuthorityUtils.createAuthorityList(ADMIN_AUTHORITY))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "1", "asd"})
    void assignRole_InvalidUserId(String userId) throws Exception {
        mockMvc.perform(patch("/api/v1/admin/users/{id}/roles/{roleName}", userId, EXPERT_ROLE)
                        .with(jwt().authorities(AuthorityUtils.createAuthorityList(ADMIN_AUTHORITY)))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void assignRole_Unauthorized() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/users/{id}/roles/{roleName}", userId, EXPERT_ROLE))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void assignRole_Forbidden_NotAdmin() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/users/{id}/roles/{roleName}", userId, EXPERT_ROLE)
                        .with(jwt()))
                .andDo(print())
                .andExpect(status().isForbidden());
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
