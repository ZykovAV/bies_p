package ylab.bies.ideaservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;
import ylab.bies.ideaservice.service.IdeaService;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class IdeaControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private IdeaService ideaService;
    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2");

    @Test
    @DisplayName("Create a draft. Should be successful.")
    public void testCreateDraftIdea_successAndReturns201() throws Exception {

        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName("Draft Idea");
        request.setText("Draft idea text");

        IdeaDraftResponseDto response = new IdeaDraftResponseDto();
        response.setId(123L);
        response.setName("Draft Idea");
        response.setText("Draft idea text");

        Mockito.when(ideaService.createDraftIdea(Mockito.anyString(), Mockito.any(IdeaDraftRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ideas/draft")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(123)))
                .andExpect(jsonPath("$.name", is("Draft Idea")))
                .andExpect(jsonPath("$.text", is("Draft idea text")));

        Mockito.verify(ideaService, Mockito.times(1)).createDraftIdea(Mockito.anyString(), Mockito.any(IdeaDraftRequestDto.class));
    }


    @Test
    @DisplayName("Create a draft. Returns a bad request.")
    public void testCreateDraftIdea_badRequestAndReturns400() throws Exception {

        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName(null);
        request.setText(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ideas/draft")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


}


