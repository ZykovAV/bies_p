package ylab.bies.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import ylab.bies.userservice.dto.LoginRequest;
import ylab.bies.userservice.dto.RegisterRequest;
import ylab.bies.userservice.dto.UserResponse;
import ylab.bies.userservice.repository.UserRepository;
import ylab.bies.userservice.service.KeycloakService;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ylab.bies.userservice.controller.UserTestUtil.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class UserAuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UserRepository repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @MockBean
    private KeycloakService keycloakService;
    private static final List<String> invalidUsernamesAndPasswords = Arrays.asList(
            "",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            null
    );
    private static final List<String> invalidEmails = Arrays.asList(
            "",
            "invalidEmail",
            "invalidEmailmail.ru",
            null
    );
    private static final List<String> invalidNames = Arrays.asList(
            "",
            "asdasd1",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
            null
    );
    private static final List<String> invalidMiddleNames = Arrays.asList(
            "",
            "asdasd1",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    );

    @Nested
    class RollbackTests {
        @AfterEach
        void setUp() {
            jdbcTemplate.execute("TRUNCATE TABLE users CASCADE");
        }

        @Test
        void register_Successfully() throws Exception {
            UUID userId = UUID.randomUUID();
            RegisterRequest request = getValidRegisterRequest();

            when(keycloakService.register(any(UserRepresentation.class))).thenReturn(
                    Response.created(URI.create("/" + userId)).build()
            );
            doNothing().when(keycloakService).assignRoles(eq(String.valueOf(userId)), anySet());

            MvcResult result = mockMvc.perform(post("/api/v1/users/register")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(mapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andReturn();
            UserResponse response = mapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);

            assertThat(isUserResponseValid(request, response, userId)).isTrue();
            assertThat(repository.findById(userId).isPresent()).isTrue();

            verify(keycloakService, times(1)).register(any(UserRepresentation.class));
        }
    }

    @Test
    void register_EmptyBody() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("getInvalidUsernamesAndPasswordsAsArguments")
    void register_InvalidUsername(String username) throws Exception {
        RegisterRequest request = getValidRegisterRequest();
        request.setUsername(username);

        register_InvalidBody(request);
    }

    @ParameterizedTest
    @MethodSource("getInvalidUsernamesAndPasswordsAsArguments")
    void register_InvalidPassword(String password) throws Exception {
        RegisterRequest request = getValidRegisterRequest();
        request.setPassword(password);

        register_InvalidBody(request);
    }

    @ParameterizedTest
    @MethodSource("getInvalidEmailsAsArguments")
    void register_InvalidEmail(String email) throws Exception {
        RegisterRequest request = getValidRegisterRequest();
        request.setEmail(email);

        register_InvalidBody(request);
    }

    @ParameterizedTest
    @MethodSource("getInvalidNamesAsArguments")
    void register_InvalidFirstName(String firstName) throws Exception {
        RegisterRequest request = getValidRegisterRequest();
        request.setFirstName(firstName);

        register_InvalidBody(request);
    }

    @ParameterizedTest
    @MethodSource("getInvalidNamesAsArguments")
    void register_InvalidLastName(String lastName) throws Exception {
        RegisterRequest request = getValidRegisterRequest();
        request.setLastName(lastName);

        register_InvalidBody(request);
    }

    @ParameterizedTest
    @MethodSource("getInvalidMiddleNamesAsArguments")
    void register_InvalidMiddleName(String middleName) throws Exception {
        RegisterRequest request = getValidRegisterRequest();
        request.setMiddleName(middleName);

        register_InvalidBody(request);
    }

    @Test
    void register_ThrowException_UserAlreadyExist() throws Exception {
        RegisterRequest request = getValidRegisterRequest();

        when(keycloakService.register(any(UserRepresentation.class))).thenReturn(
                Response.status(Response.Status.CONFLICT).build()
        );

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void login_Successfully() throws Exception {
        LoginRequest request = getValidLoginRequest();
        AccessTokenResponse tokenResponse = getAccessTokenResponse();

        when(keycloakService.getToken(request.getUsername(), request.getPassword())).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.access_token", equalTo(tokenResponse.getToken())))
                .andExpect(jsonPath("$.refresh_token", equalTo(tokenResponse.getRefreshToken())))
                .andExpect(jsonPath("$.token_type", equalTo(tokenResponse.getTokenType())))
                .andExpect(jsonPath("$.session_state", equalTo(tokenResponse.getSessionState())))
                .andExpect(jsonPath("$.scope", equalTo(tokenResponse.getScope())));

        verify(keycloakService, times(1)).getToken(request.getUsername(), request.getPassword());
    }

    @Test
    void login_ThrowException_InvalidCredentials() throws Exception {
        LoginRequest request = getValidLoginRequest();

        when(keycloakService.getToken(request.getUsername(), request.getPassword()))
                .thenThrow(NotAuthorizedException.class);

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verify(keycloakService, times(1)).getToken(request.getUsername(), request.getPassword());
    }

    @Test
    void login_EmptyBody() throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_InvalidUsername() throws Exception {
        LoginRequest request = getValidLoginRequest();
        request.setUsername("");

        login_InvalidBody(request);
    }

    @Test
    void login_InvalidPassword() throws Exception {
        LoginRequest request = getValidLoginRequest();
        request.setPassword("");

        login_InvalidBody(request);
    }

    private void register_InvalidBody(RegisterRequest request) throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private void login_InvalidBody(LoginRequest request) throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> getInvalidUsernamesAndPasswordsAsArguments() {
        return invalidUsernamesAndPasswords.stream().map(Arguments::arguments);
    }

    private static Stream<Arguments> getInvalidEmailsAsArguments() {
        return invalidEmails.stream().map(Arguments::arguments);
    }

    private static Stream<Arguments> getInvalidNamesAsArguments() {
        return invalidNames.stream().map(Arguments::arguments);
    }

    private static Stream<Arguments> getInvalidMiddleNamesAsArguments() {
        return invalidMiddleNames.stream().map(Arguments::arguments);
    }
}
