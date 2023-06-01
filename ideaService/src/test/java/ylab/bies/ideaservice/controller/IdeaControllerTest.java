package ylab.bies.ideaservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.request.IdeaRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;
import ylab.bies.ideaservice.service.IdeaService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ylab.bies.ideaservice.util.enums.Status.UNDER_CONSIDERATION;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class IdeaControllerTest {

    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.2");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdeaService ideaService;


    // CreateDraftIdea tests

    @Test
    @DisplayName("Create a draft. Should be successful.")
    public void testCreateDraftIdea_successAndReturns201() throws Exception {

        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName("Draft Idea");
        request.setText("Draft idea text");

        mockMvc.perform(post("/api/v1/ideas/draft")
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

        mockMvc.perform(post("/api/v1/ideas/draft")
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

        mockMvc.perform(post("/api/v1/ideas/draft")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    // GetAllIdeas tests

    @Test
    @DisplayName("Get All Ideas. Should be successful.")
    public void testGetAllIdeas_successAndReturns200() throws Exception {

        dataGenerationForGetAllIdeas();
        UUID testingUserId = UUID.fromString("a81bc81b-dead-4e5d-abff-90865d1e13b1");

        List<IdeaResponseDto> ideas = Arrays.asList(
                new IdeaResponseDto(1L, "Idea", "Idea test text", 0, testingUserId, 2, null),
                new IdeaResponseDto(2L, "Idea 2", "Idea test text2", 0, testingUserId, 2, null)
        );
        Page<IdeaResponseDto> testPageIdeas = new PageImpl<>(ideas);

        mockMvc.perform(get("/api/v1/ideas")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(testPageIdeas))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Idea"))
                .andExpect(jsonPath("$.content[0].text").value("Idea test text"))
                .andExpect(jsonPath("$.content[0].rating").value(0))
                .andExpect(jsonPath("$.content[0].userId").value(testingUserId.toString()))
                .andExpect(jsonPath("$.content[0].status").value(2));

    }

    @Test
    @DisplayName("Get All Ideas. Returning a list without ideas.")
    public void testGetAllIdeas_returnsListWithoutIdeas() throws Exception {
        List<IdeaResponseDto> testList = new ArrayList<>();
        Page<IdeaResponseDto> testPage = new PageImpl<>(testList); //emptyPage

        mockMvc.perform(get("/api/v1/ideas")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(testPage))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("Get All Ideas. Test Pagination.")
    public void testGetAllIdeas_testPagination() throws Exception {

        mockMvc.perform(get("/api/v1/ideas")
                        .header("Authorization", "test-token")
                        .param("page", "0")
                        .param("limit", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // Update tests

    @Test
    @DisplayName("Update a idea. Should be successful.")
    public void testUpdateIdea_successAndReturns200() throws Exception {
        dataGeneration();

        IdeaRequestDto requestForUpdate = new IdeaRequestDto();
        requestForUpdate.setId(1L);
        requestForUpdate.setName("Idea for Update");
        requestForUpdate.setText("Idea for Update text");

        mockMvc.perform(put("/api/v1/ideas/1")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Idea for Update")))
                .andExpect(jsonPath("$.text", is("Idea for Update text")))
                .andExpect(jsonPath("$.status", is(UNDER_CONSIDERATION.getValue())));
    }


    @Test
    @DisplayName("Update a idea. Returns a bad request - not valid field.")
    public void testUpdateIdea_InvalidInput_badRequestAndReturns400() throws Exception {
        dataGeneration();

        IdeaRequestDto requestForUpdate = new IdeaRequestDto();
        requestForUpdate.setId(1L);
        requestForUpdate.setName("");  // Invalid data input
        requestForUpdate.setText("Idea for Update text");

        mockMvc.perform(put("/api/v1/ideas/1")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("Update a idea. Returns Not Found Exception 404.")
    public void testUpdateIdea_ReturnsNotFound404() throws Exception {
        dataGeneration();

        IdeaRequestDto requestForUpdate = new IdeaRequestDto();
        requestForUpdate.setId(5L); //   Invalid id
        requestForUpdate.setName("Idea");
        requestForUpdate.setText("Idea for Update text");


        mockMvc.perform(put("/api/v1/ideas/5")  //  Invalid id
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Update a idea. Returns method not allowed.")
    public void testUpdateIdea_ReturnsMethodNotAllowed405() throws Exception {
        dataGeneration();

        IdeaRequestDto requestForUpdate = new IdeaRequestDto();
        requestForUpdate.setId(null);
        requestForUpdate.setName("Idea");
        requestForUpdate.setText("Idea for Update text");


        mockMvc.perform(put("/api/v1/ideas") //invalid request
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andDo(print());
    }






    @Test
    @DisplayName("ChangeStatus. Change DRAFT(1) to IMPOSSIBLE STATUS. Should return 304")
    public void changeDraftToImpossibleStatus() throws Exception {
        initTable();  // add test draft and then try to change status
        for (int i : new int[]{-1,0,5}) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/1/status?statusId=" + i)
                            .header("Authorization", "test-token"))
                    .andExpect(status().isNotModified());
        }
    }

    @Test
    @DisplayName("ChangeStatus. Change DRAFT(1) to ANY STATUS. Should return 304")
    public void changeDraftToAnyStatus() throws Exception {
        initTable();  // add test draft and then try to change status
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/1/status?statusId=" + i)
                            .header("Authorization", "test-token"))
                    .andExpect(status().isNotModified());
        }
    }

    @Test
    @DisplayName("ChangeStatus. Change UNDER_CONSIDERATION(2) to DRAFT(1) or SAME. Should return 304")
    public void changeUnderConsiderationToAnyStatus() throws Exception {
        initTable();  // add test draft and then try to change status
        // todo изменить id идеи когда будет возможность публиковать черновики
        for (int i = 1; i <= 2; i++) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/1/status?statusId=" + i)
                            .header("Authorization", "test-token"))
                    .andExpect(status().isNotModified());
        }
    }

    @Test
    @DisplayName("ChangeStatus. Change UNDER_CONSIDERATION(2) to ACCEPTED(3). Should be ok")
    public void changeUnderConsiderationToAccepted() throws Exception {
        initTable();  // add test draft and then try to change status
        // todo изменить id идеи когда будет возможность публиковать черновики
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/1/status?statusId=3")
                        .header("Authorization", "test-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ChangeStatus. Change UNDER_CONSIDERATION(2) to REJECTED(4). Should be ok")
    public void changeUnderConsiderationToRejected() throws Exception {
        initTable();  // add test draft and then try to change status
        // todo изменить id идеи когда будет возможность публиковать черновики
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/1/status?statusId=4")
                        .header("Authorization", "test-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ChangeStatus. Change ACCEPTED(3) to ANY STATUS. Should return 304")
    public void changeAcceptedToAnyStatus() throws Exception {
        initTable();  // add test draft and then try to change status
        // todo изменить id идеи когда будет возможность присваивать статус ACCEPTED(3)
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/1/status?statusId=" + i)
                            .header("Authorization", "test-token"))
                    .andExpect(status().isNotModified());
        }
    }

    @Test
    @DisplayName("ChangeStatus. Change REJECTED(4) to ANY STATUS. Should return 304")
    public void changeRejectedToAnyStatus() throws Exception {
        initTable();  // add test draft and then try to change status
        // todo изменить id идеи когда будет возможность присваивать статус REJECTED(4)
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/1/status?statusId=" + i)
                            .header("Authorization", "test-token"))
                    .andExpect(status().isNotModified());
        }
    }

    @Test
    @DisplayName("Get by id. Get an existing idea. Should be successful.")
    public void getAnExistingIdeaById() throws Exception {
        initTable();  // add test draft and then get
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/1")
                        .header("Authorization", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Draft Idea")))
                .andExpect(jsonPath("$.text", is("Draft idea text")))
                .andExpect(jsonPath("$.rating", is(nullValue())))
                .andExpect(jsonPath("$.userId", is("a81bc81b-dead-4e5d-abff-90865d1e13b1")))
                .andExpect(jsonPath("$.status", is(1)))
                .andExpect(jsonPath("$.userLike", is(nullValue())));
    }

    @Test
    @DisplayName("GetById. Get an non-existent idea. Should return 404.")
    public void getAnNotExistingIdeaById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/100500")
                        .header("Authorization", "test-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GetById. Get idea with not valid id. Should return 400.")
    public void getIdeaByBadId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/BAD_ID")
                        .header("Authorization", "test-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GetById. Get someone's else draft. Should return 403.")
    public void getSomeonesElseDraft() throws Exception {
        initTable();
        // todo добавить чужие черновики как отключим получение одного и того же uuid из декодера
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/1")
                        .header("Authorization", "test-token"))
                .andExpect(status().isForbidden());
    }


    private void initTable() throws Exception {
        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName("Draft Idea");
        request.setText("Draft idea text");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ideas/draft")
                .header("Authorization", "test-token")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON));
    }






    /**
     * This private method adds data to the test container for further testing.
     */

    private void dataGeneration() throws Exception {

        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName("Draft Idea");
        request.setText("Draft idea text");


        mockMvc.perform(post("/api/v1/ideas/draft")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Draft Idea")))
                .andExpect(jsonPath("$.text", is("Draft idea text")));
    }


    /**
     * This private method adds data to the test container for further testing.
     */

    private void dataGenerationForGetAllIdeas() throws Exception {

        //1 идея
        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName("Draft Idea");
        request.setText("Draft idea text");

        IdeaRequestDto requestForUpdate = new IdeaRequestDto();
        requestForUpdate.setId(1L);
        requestForUpdate.setName("Idea");
        requestForUpdate.setText("Idea test text");

        //создаем черновик
        mockMvc.perform(post("/api/v1/ideas/draft")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Draft Idea")))
                .andExpect(jsonPath("$.text", is("Draft idea text")));

        //публикуем реальную идею(редактируем черновик)
        mockMvc.perform(put("/api/v1/ideas/1")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Idea")))
                .andExpect(jsonPath("$.text", is("Idea test text")))
                .andExpect(jsonPath("$.status", is(UNDER_CONSIDERATION.getValue())));



        //2 идея
        IdeaDraftRequestDto request2 = new IdeaDraftRequestDto();
        request2.setName("Draft Idea2");
        request2.setText("Draft idea text2");

        IdeaRequestDto requestForUpdate2 = new IdeaRequestDto();
        requestForUpdate2.setId(2L);
        requestForUpdate2.setName("Idea2");
        requestForUpdate2.setText("Idea test text2");

        //создаем черновик
        mockMvc.perform(post("/api/v1/ideas/draft")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(request2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Draft Idea2")))
                .andExpect(jsonPath("$.text", is("Draft idea text2")));

        //публикуем реальную идею(редактируем черновик)
        mockMvc.perform(put("/api/v1/ideas/2")
                        .header("Authorization", "test-token")
                        .content(objectMapper.writeValueAsString(requestForUpdate2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Idea2")))
                .andExpect(jsonPath("$.text", is("Idea test text2")))
                .andExpect(jsonPath("$.status", is(UNDER_CONSIDERATION.getValue())));

    }
}


