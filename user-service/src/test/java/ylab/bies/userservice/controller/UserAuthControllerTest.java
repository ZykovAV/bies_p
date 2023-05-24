package ylab.bies.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ylab.bies.userservice.dto.RegisterRequest;
import ylab.bies.userservice.dto.UserResponse;
import ylab.bies.userservice.repository.UserRepository;
import ylab.bies.userservice.service.KeycloakService;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
    @MockBean
    private KeycloakService keycloakService;
    @Container
    PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2");


    @Test
    @Transactional
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

    @Test
    void register_EmptyBody() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isBadRequest());

        assertThat(repository.findAll()).isEmpty();

        verify(keycloakService, never()).register(any(UserRepresentation.class));
        verify(keycloakService, never()).assignRoles(anyString(), anySet());
    }

    @Test
    void register_InvalidBody() throws Exception {
        RegisterRequest request = getInvalidRegisterRequest();
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(mapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        assertThat(repository.findAll()).isEmpty();
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
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

        assertThat(repository.findAll()).isEmpty();
    }
}
