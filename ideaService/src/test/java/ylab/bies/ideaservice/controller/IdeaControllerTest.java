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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.service.IdeaService;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
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


//    @Test
//    @DisplayName("Get an existing idea. Should be successful.")
//    @SqlGroup({
//            @Sql(value = "classpath:init/ideas-test-data.sql", executionPhase = BEFORE_TEST_METHOD)
//    })
//    public void getAnExistingIdeaById() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/1")
//                        .header("Authorization", "test-token"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", is(1)))
//                .andExpect(jsonPath("$.name", is("name-1")))
//                .andExpect(jsonPath("$.text", is("text-1")));
//    }

    @Test
    @DisplayName("Get an non-existent idea. Should return 404.")
    public void getAnNotExistingIdeaById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/100500")
                        .header("Authorization", "test-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get idea with not valid id. Should return 400.")
    public void getIdeaByBadId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/BAD_ID")
                        .header("Authorization", "test-token"))
                .andExpect(status().isBadRequest());
    }

}


