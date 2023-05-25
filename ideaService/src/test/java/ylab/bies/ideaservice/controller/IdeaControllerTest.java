package ylab.bies.ideaservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2");

    static {
        postgreSQLContainer.start();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdeaService ideaService;


    @Test
    @DisplayName("Create a draft. Should be successful.")
    public void testCreateDraftIdea_successAndReturns201() throws Exception {

        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName("Draft Idea");
        request.setText("Draft idea text");

        IdeaDraftResponseDto response = new IdeaDraftResponseDto();
        response.setId(1L);
        response.setName("Draft Idea");
        response.setText("Draft idea text");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ideas/draft")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Draft Idea")))
                .andExpect(jsonPath("$.text", is("Draft idea text")));
    }

    @Test
    @DisplayName("Create a draft. Returns a bad request - null field.")
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


    @Test
    @DisplayName("Create a draft. Returns a bad request - not valid field.")
    public void testCreateDraftIdea_badRequestNotValidField() throws Exception {

        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName("");
        request.setText("small text");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ideas/draft")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Get All Ideas. Should be successful.")
    public void testGetAllIdeas_successAndReturns200() throws Exception {
        UUID testingUserId = UUID.randomUUID();

        List<IdeaResponseDto> ideas = Arrays.asList(
                new IdeaResponseDto(1L, "Idea 1", "Some text 1", 5, testingUserId, 3, true),
                new IdeaResponseDto(2L, "Idea 2", "Some text 2", 3, testingUserId, 2, false),
                new IdeaResponseDto(3L, "Idea 3", "Some text 3", 8, testingUserId, 2, false)
        );

        //todo заполнить тестовыми данными, когда появится эндпойнт сохранения идеи (сохранить ideas через него)

        mockMvc.perform(get("/api/v1/ideas")
                        .header("Authorization", "test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Idea 1")))
                .andExpect(jsonPath("$[0].text", is("Some text 1")))
                .andExpect(jsonPath("$[0].rating", is(5)))
                .andExpect(jsonPath("$[0].userId", is(testingUserId)))
                .andExpect(jsonPath("$[0].status", is(3)))
                .andExpect(jsonPath("$[0].userLike", is(true)));

    }

    @Test
    @DisplayName("Get All Ideas. Returning a list without ideas.")
    public void testGetAllIdeas_returnsListWithoutIdeas() throws Exception {

        mockMvc.perform(get("/api/v1/ideas")
                        .header("Authorization", "test-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));


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


