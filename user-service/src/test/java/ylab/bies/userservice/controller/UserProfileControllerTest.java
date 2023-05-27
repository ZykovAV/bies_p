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
import org.testcontainers.junit.jupiter.Testcontainers;
import ylab.bies.userservice.dto.RegisterRequest;
import ylab.bies.userservice.dto.UserResponse;
import ylab.bies.userservice.service.KeycloakService;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ylab.bies.userservice.controller.UserTestUtil.getValidRegisterRequest;
import static ylab.bies.userservice.controller.UserTestUtil.isUserResponseValid;

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

    @Test
    @Transactional
    void getProfile_Successfully() throws Exception {
        UUID userId = UUID.randomUUID();
        RegisterRequest request = getValidRegisterRequest();

        //Register part start
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
        //Register part end

        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(request.getUsername());
        when(keycloakService.getUserById(String.valueOf(userId))).thenReturn(keycloakUser);


        MvcResult result = mockMvc.perform(get("/api/v1/users/profile")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(userId)))
                                .authorities(createAuthorityList("ROLE_USER"))
                        ))
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
}
