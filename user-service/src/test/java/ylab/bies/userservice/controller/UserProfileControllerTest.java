package ylab.bies.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import ylab.bies.userservice.dto.ChangeFullNameRequest;
import ylab.bies.userservice.dto.ChangeFullNameResponse;
import ylab.bies.userservice.dto.RegisterRequest;
import ylab.bies.userservice.dto.UserResponse;
import ylab.bies.userservice.service.KeycloakService;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ylab.bies.userservice.controller.UserTestUtil.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class UserProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private KeycloakService keycloakService;
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

    @Test
    @Transactional
    void getProfile_Successfully() throws Exception {
        UUID userId = UUID.randomUUID();
        RegisterRequest request = getValidRegisterRequest();

        registerUser(userId, request);

        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(request.getUsername());
        when(keycloakService.getUserById(String.valueOf(userId))).thenReturn(keycloakUser);


        MvcResult result = mockMvc.perform(get("/api/v1/users/profile")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(userId))))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();
        UserResponse response = mapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);

        assertThat(isUserResponseValid(request, response, userId)).isTrue();

        verify(keycloakService, times(1)).getUserById(String.valueOf(userId));
    }

    @Test
    void getProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void changeFullName_Successfully() throws Exception {
        UUID userId = UUID.randomUUID();
        RegisterRequest registerRequest = getValidRegisterRequest();
        ChangeFullNameRequest request = getValidChangeFullNameRequest();
        registerUser(userId, registerRequest);

        doNothing().when(keycloakService).changeFullName(
                String.valueOf(userId),
                request.getFirstName(),
                request.getLastName()
        );

        MvcResult result = mockMvc.perform(put("/api/v1/users/profile/fullName")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(userId))))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();
        ChangeFullNameResponse response = mapper.readValue(
                result.getResponse().getContentAsString(),
                ChangeFullNameResponse.class
        );

        assertThat(isChangeFullNameResponseValid(request, response)).isTrue();

        verify(keycloakService, times(1)).changeFullName(
                String.valueOf(userId),
                request.getFirstName(),
                request.getLastName()
        );
    }

    @Test
    void changeFullName_EmptyBody() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/users/profile/fullName")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(userId))))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("getInvalidNamesAsArguments")
    void changeFullName_InvalidFirstName(String name) throws Exception {
        ChangeFullNameRequest request = getValidChangeFullNameRequest();
        request.setFirstName(name);

        changeFullName_InvalidBody(request);
    }

    @ParameterizedTest
    @MethodSource("getInvalidNamesAsArguments")
    void changeFullName_InvalidLastName(String name) throws Exception {
        ChangeFullNameRequest request = getValidChangeFullNameRequest();
        request.setLastName(name);

        changeFullName_InvalidBody(request);
    }

    @ParameterizedTest
    @MethodSource("getInvalidMiddleNamesAsArguments")
    void changeFullName_InvalidMiddleName(String name) throws Exception {
        ChangeFullNameRequest request = getValidChangeFullNameRequest();
        request.setMiddleName(name);

        changeFullName_InvalidBody(request);
    }

    @Test
    void changeFullName_Unauthorized() throws Exception {
        ChangeFullNameRequest request = getValidChangeFullNameRequest();
        mockMvc.perform(put("/api/v1/users/profile/fullName")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
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

    private void changeFullName_InvalidBody(ChangeFullNameRequest request) throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/users/profile/fullName")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(userId))))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> getInvalidNamesAsArguments() {
        return invalidNames.stream().map(Arguments::arguments);
    }

    private static Stream<Arguments> getInvalidMiddleNamesAsArguments() {
        return invalidMiddleNames.stream().map(Arguments::arguments);
    }
}
