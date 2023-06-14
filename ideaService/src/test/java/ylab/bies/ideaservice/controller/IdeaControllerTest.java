package ylab.bies.ideaservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
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
import ylab.bies.ideaservice.service.KafkaProducerService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ylab.bies.ideaservice.util.enums.Status.*;

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
    @MockBean
    private KafkaProducerService kafkaProducerService;

    public static final UUID USER_ID = UUID.fromString("a81bc81b-dead-4e5d-abff-90865d1e13b2");
    public static final UUID USER_2_ID = UUID.fromString("4676f39a-6c23-48b9-986c-869ef0f5dc31");
    public static final UUID EXPERT_ID = UUID.fromString("bc3d19e5-90d9-45d8-b742-b8d4e3a3fcdd");


    /**
     * CreateDraftIdea tests
     */

    @Test
    @DisplayName("Create a draft. Should be successful.")
    public void testCreateDraftIdea_successAndReturns201() throws Exception {
        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName("Draft Idea");
        request.setText("Draft idea text");
        mockMvc.perform(post("/api/v1/ideas/draft")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
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
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
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
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Create a draft. Returns 401 Unauthorized.")
    public void testCreateDraftIdea_returnsUnauthorized401() throws Exception {

        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName("Draft Idea");
        request.setText("Draft idea text");

        mockMvc.perform(post("/api/v1/ideas/draft")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }


    /**
     * GetAllIdeas tests
     */

    @Test
    @DisplayName("Get All Ideas. Should be successful.")
    public void testGetAllIdeas_successAndReturns200() throws Exception {

        dataGenerationForGetAllIdeas();

        List<IdeaResponseDto> ideas = Arrays.asList(
                new IdeaResponseDto(1L, "Idea", "Idea test text", 0, USER_ID, 2, null),
                new IdeaResponseDto(2L, "Idea 2", "Idea test text2", 0, USER_ID, 2, null)
        );
        Page<IdeaResponseDto> testPageIdeas = new PageImpl<>(ideas);

        mockMvc.perform(get("/api/v1/ideas")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(testPageIdeas))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Idea"))
                .andExpect(jsonPath("$.content[0].text").value("Idea test text"))
                .andExpect(jsonPath("$.content[0].rating").value(0))
                .andExpect(jsonPath("$.content[0].userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.content[0].status").value(2));

    }

    @Test
    @DisplayName("Get All Ideas. Returning a list without ideas.")
    public void testGetAllIdeas_returnsListWithoutIdeas() throws Exception {
        List<IdeaResponseDto> testList = new ArrayList<>();
        Page<IdeaResponseDto> testPage = new PageImpl<>(testList); //emptyPage

        mockMvc.perform(get("/api/v1/ideas")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(testPage))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("Get All Ideas. Test Pagination.")
    public void testGetAllIdeas_testPagination() throws Exception {

        mockMvc.perform(get("/api/v1/ideas")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .param("page", "0")
                        .param("limit", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get All Ideas test. Returns 401 Unauthorized.")
    public void testGetAllIdeas_returnsUnauthorized401() throws Exception {

        mockMvc.perform(get("/api/v1/ideas")
                        .param("page", "0")
                        .param("limit", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }


    /**
     * UpdateIdea tests
     */

    @Test
    @DisplayName("Update a idea. Should be successful.")
    public void testUpdateIdea_successAndReturns200() throws Exception {
        dataGenerationDraftOne();

        IdeaRequestDto requestForUpdate = new IdeaRequestDto();
        requestForUpdate.setName("Idea for Update");
        requestForUpdate.setText("Idea for Update text");

        mockMvc.perform(put("/api/v1/ideas/1")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
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
        dataGenerationDraftOne();

        IdeaRequestDto requestForUpdate = new IdeaRequestDto();
        requestForUpdate.setName("");  // Invalid data input
        requestForUpdate.setText("Idea for Update text");

        mockMvc.perform(put("/api/v1/ideas/1")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("Update a idea. Returns Not Found Exception 404.")
    public void testUpdateIdea_ReturnsNotFound404() throws Exception {
        dataGenerationDraftOne();

        IdeaRequestDto requestForUpdate = new IdeaRequestDto();
        requestForUpdate.setName("Idea");
        requestForUpdate.setText("Idea for Update text");


        mockMvc.perform(put("/api/v1/ideas/5")  //  Invalid id
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Update a idea. Returns 401 Unauthorized.")
    public void testUpdateIdea_ReturnsUnauthorized401() throws Exception {
        dataGenerationDraftOne();
        IdeaRequestDto requestForUpdate = new IdeaRequestDto();
        requestForUpdate.setName("Idea");
        requestForUpdate.setText("Idea for Update text");
        mockMvc.perform(put("/api/v1/ideas/1") //invalid request
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }



    /**
     *  GetAllUsersIdeas tests
     */

    @Test
    @DisplayName("Get All user's ideas. Should be successful.")
    public void testGetAllUsersIdeas_successAndReturns200() throws Exception {

        dataGenerationForGetAllIdeas();

        List<IdeaResponseDto> ideas = Arrays.asList(
                new IdeaResponseDto(1L, "Idea", "Idea test text", 0, USER_ID, 2, null),
                new IdeaResponseDto(2L, "Idea 2", "Idea test text2", 0, USER_ID, 3, null)
        );
        Page<IdeaResponseDto> testPageIdeas = new PageImpl<>(ideas);
        mockMvc.perform(get("/api/v1/ideas/my")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(testPageIdeas))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Idea"))
                .andExpect(jsonPath("$.content[0].text").value("Idea test text"))
                .andExpect(jsonPath("$.content[0].rating").value(0))
                .andExpect(jsonPath("$.content[0].userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.content[0].status").value(2));
    }

    @Test
    @DisplayName("Get All user's ideas. Returning a list without ideas.")
    public void testGetAllUsersIdeas_returnsListWithoutIdeas() throws Exception {
        List<IdeaResponseDto> testList = new ArrayList<>();
        Page<IdeaResponseDto> testPage = new PageImpl<>(testList); //emptyPage
        mockMvc.perform(get("/api/v1/ideas/my")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(testPage))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("Get All user's ideas. Invalid pagination. Returning Ok - 200.")
    public void testGetAllUsersIdeas_successWithInvalidPaginationAndReturns200() throws Exception {

        mockMvc.perform(get("/api/v1/ideas/my")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .param("page", "-3")
                        .param("size", "-10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("Get All user's ideas. Returns 401 Unauthorized.")
    public void testGetAllUsersIdeas_returnsUnauthorized401() throws Exception {
        mockMvc.perform(get("/api/v1/ideas/my")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }


    /**
     *  ChangeStatus tests
     */

    @Test
    @DisplayName("ChangeStatus. Change DRAFT(1) to IMPOSSIBLE STATUS. Should return 403")
    public void changeDraftToImpossibleStatus() throws Exception {
        initTable();  // add test draft and then try to change status
        for (int i : new int[]{-1, 0, 5}) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/1/status?statusId=" + i)
                            .with(jwt()
                                    .authorities(AuthorityUtils.createAuthorityList("ROLE_EXPERT"))
                                    .jwt(jwt -> jwt.claim("sub", String.valueOf(EXPERT_ID)))))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("ChangeStatus. Change DRAFT(1) to ANY STATUS. Should return 403")
    public void changeDraftToAnyStatus() throws Exception {
        initTable();  // add test draft and then try to change status
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/1/status?statusId=" + i)
                            .with(jwt()
                                    .authorities(AuthorityUtils.createAuthorityList("ROLE_EXPERT"))
                                    .jwt(jwt -> jwt.claim("sub", String.valueOf(EXPERT_ID)))))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("ChangeStatus. Change UNDER_CONSIDERATION(2) to DRAFT(1) or SAME. Should return 403")
    public void changeUnderConsiderationToAnyStatus() throws Exception {
        initTable();
        for (int i = 1; i <= 2; i++) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/status?statusId=" + i)
                            .with(jwt()
                                    .authorities(AuthorityUtils.createAuthorityList("ROLE_EXPERT"))
                                    .jwt(jwt -> jwt.claim("sub", String.valueOf(EXPERT_ID)))))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("ChangeStatus. Change UNDER_CONSIDERATION(2) to ACCEPTED(3). Should be ok")
    public void changeUnderConsiderationToAccepted() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/status?statusId=3")
                        .with(jwt()
                                .authorities(AuthorityUtils.createAuthorityList("ROLE_EXPERT"))
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(EXPERT_ID)))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ChangeStatus. Change UNDER_CONSIDERATION(2) to REJECTED(4). Should be ok")
    public void changeUnderConsiderationToRejected() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/status?statusId=4")
                        .with(jwt()
                                .authorities(AuthorityUtils.createAuthorityList("ROLE_EXPERT"))
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(EXPERT_ID)))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ChangeStatus. Change ACCEPTED(3) to ANY STATUS. Should return 403")
    public void changeAcceptedToAnyStatus() throws Exception {
        initTable();
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/3/status?statusId=" + i)
                            .with(jwt()
                                    .authorities(AuthorityUtils.createAuthorityList("ROLE_EXPERT"))
                                    .jwt(jwt -> jwt.claim("sub", String.valueOf(EXPERT_ID)))))
                    .andExpect(status().isForbidden());
        }
    }


    @Test
    @DisplayName("ChangeStatus. Change REJECTED(4) to ANY STATUS. Should return 403")
    public void changeRejectedToAnyStatus() throws Exception {
        initTable();
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/4/status?statusId=" + i)
                            .with(jwt()
                                    .authorities(AuthorityUtils.createAuthorityList("ROLE_EXPERT"))
                                    .jwt(jwt -> jwt.claim("sub", String.valueOf(EXPERT_ID)))))
                    .andExpect(status().isForbidden());
        }
    }


    /**
     * Like tests
     */

    @Test
    @DisplayName("Like. Like an non-existent idea. Should return 404")
    public void likeAnNonExistentIdea() throws Exception {
        for (int i : new int[]{-1, 0, 100}) {
            mockMvc.perform(MockMvcRequestBuilders.patch(String.format("/api/v1/ideas/%d/like", i))
                            .with(jwt()
                                    .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID)))))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("Like. Like a DRAFT. Should return 403")
    public void likeDraft() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/1/like")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Like. Like an ACCEPTED idea. Should return 403")
    public void likeAccepted() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/3/like")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Like. Like a REJECTED idea. Should return 403")
    public void likeRejected() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/4/like")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Like. Like own UNDER_CONSIDERATION idea. Should return 403")
    public void likeOwnUnderConsideration() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/like")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Like. Like not own UNDER_CONSIDERATION idea. Should be ok")
    public void likeNotOwn() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/like")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ideas/2")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(1)));
    }

    @Test
    @DisplayName("Like. Second like to same idea. Should be 403")
    public void secondLikeToSameIdea() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/like")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ideas/2")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(1)));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/like")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/ideas/2")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(1)));
    }

    @Test
    @DisplayName("Like. Like after dislike. Should be ok")
    public void likeAfterDislike() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/dislike")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ideas/2")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(-1)));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/like")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ideas/2")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(1)));
    }


    /**
     * Dislike tests
     */

    @Test
    @DisplayName("Dislike. Dislike an non-existent idea. Should return 404")
    public void dislikeAnNonExistentIdea() throws Exception {
        for (int i : new int[]{-1, 0, 100}) {
            mockMvc.perform(MockMvcRequestBuilders.patch(String.format("/api/v1/ideas/%d/dislike", i))
                            .with(jwt()
                                    .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID)))))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("Dislike. Dislike a DRAFT. Should return 403")
    public void dislikeDraft() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/1/dislike")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Dislike. Dislike an ACCEPTED idea. Should return 403")
    public void dislikeAccepted() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/3/dislike")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Dislike. Dislike a REJECTED idea. Should return 403")
    public void dislikeRejected() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/4/dislike")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Dislike. Dislike own UNDER_CONSIDERATION idea. Should return 403")
    public void dislikeOwnUnderConsideration() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/dislike")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Dislike. Dislike not own UNDER_CONSIDERATION idea. Should be ok")
    public void dislikeNotOwn() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/dislike")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ideas/2")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(-1)));
    }

    @Test
    @DisplayName("Dislike. Second dislike to same idea. Should be 403")
    public void secondDislikeToSameIdea() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/dislike")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ideas/2")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(-1)));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/dislike")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/ideas/2")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(-1)));
    }

    @Test
    @DisplayName("Dislike. Dislike after like. Should be ok")
    public void dislikeAfterLike() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/like")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ideas/2")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(1)));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/2/dislike")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/ideas/2")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(-1)));
    }


    /**
     *  GetById tests
     */

    @Test
    @DisplayName("Get by id. Get an existing idea. Should be successful.")
    public void getAnExistingIdeaById() throws Exception {
        initTable();  // add test draft and then get
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/1")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Draft Idea")))
                .andExpect(jsonPath("$.text", is("Draft idea text")))
                .andExpect(jsonPath("$.rating", is(0)))
                .andExpect(jsonPath("$.userId", is(String.valueOf(USER_ID))))
                .andExpect(jsonPath("$.status", is(DRAFT.getValue())))
                .andExpect(jsonPath("$.userLike", is(nullValue())));
    }

    @Test
    @DisplayName("GetById. Get an non-existent idea. Should return 404.")
    public void getAnNotExistingIdeaById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/100500")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID)))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GetById. Get idea with not valid id. Should return 400.")
    public void getIdeaByBadId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/BAD_ID")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID)))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GetById. Get someone's else draft. Should return 403.")
    public void getSomeonesElseDraft() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/1")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID)))))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("Is author. Check if user is an author of non-existent idea. Should return 404.")
    public void isAuthor404() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/-1/is-author")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID)))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Is author. Check if user is an author of his idea. Should return true.")
    public void isAuthorTrue() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/2/is-author")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID)))))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Is author. Check if user is an author of not own idea. Should return false.")
    public void isAuthorFalse() throws Exception {
        initTable();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ideas/6/is-author")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID)))))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    /**
     * Data generation methods
     *
     * Add ideas with those statuses (using API):
     * (id=1) owner: User, status: Draft
     * (id=2) owner: User, status: Under Consideration
     * (id=3) owner: User, status: Accepted
     * (id=4) owner: User, status: Rejected
     * (id=5) owner: User_2, status: Draft
     * (id=6) owner: User_2, status: Under Consideration
     * (id=7) owner: User_2, status: Accepted
     * (id=8) owner: User_2, status: Rejected
     */
    @Test
    @DisplayName("init table")
    public void initTable() throws Exception {
        // add Draft with id=1
        int id = 1;
        IdeaDraftRequestDto draftRequest = new IdeaDraftRequestDto();
        IdeaRequestDto requestForUpdate = new IdeaRequestDto();
        draftRequest.setName("Draft Idea");
        draftRequest.setText("Draft idea text");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ideas/draft")
                .with(jwt()
                        .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                .content(objectMapper.writeValueAsString(draftRequest))
                .contentType(MediaType.APPLICATION_JSON));

        // add Under consideration idea with id=2
        id = 2;
        requestForUpdate.setName("Under consideration idea");
        requestForUpdate.setText("Under consideration text");
        mockMvc.perform(post("/api/v1/ideas/draft")
                .with(jwt()
                        .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                .content(objectMapper.writeValueAsString(draftRequest))
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(put("/api/v1/ideas/" + id)
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("Under consideration idea")))
                .andExpect(jsonPath("$.text", is("Under consideration text")))
                .andExpect(jsonPath("$.status", is(UNDER_CONSIDERATION.getValue())));

        // add Accepted idea with id=3
        id = 3;
        requestForUpdate.setName("Accepted idea");
        requestForUpdate.setText("Accepted text");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ideas/draft")
                .with(jwt()
                        .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                .content(objectMapper.writeValueAsString(draftRequest))
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(put("/api/v1/ideas/" + id)
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("Accepted idea")))
                .andExpect(jsonPath("$.text", is("Accepted text")))
                .andExpect(jsonPath("$.status", is(UNDER_CONSIDERATION.getValue())));
        mockMvc.perform(patch("/api/v1/ideas/" + id + "/status?statusId=" + ACCEPTED.getValue())
                        .with(jwt()
                                .authorities(AuthorityUtils.createAuthorityList("ROLE_EXPERT"))
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(EXPERT_ID)))))
                .andExpect(status().isOk());

        // add Rejected idea with id=4
        id = 4;
        requestForUpdate.setName("Rejected idea");
        requestForUpdate.setText("Rejected text");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ideas/draft")
                .with(jwt()
                        .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                .content(objectMapper.writeValueAsString(draftRequest))
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(put("/api/v1/ideas/" + id)
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("Rejected idea")))
                .andExpect(jsonPath("$.text", is("Rejected text")))
                .andExpect(jsonPath("$.status", is(UNDER_CONSIDERATION.getValue())));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/" + id + "/status?statusId=" + REJECTED.getValue())
                        .with(jwt()
                                .authorities(AuthorityUtils.createAuthorityList("ROLE_EXPERT"))
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(EXPERT_ID)))))
                .andExpect(status().isOk());

        // add Draft with id=5
        id = 5;
        draftRequest = new IdeaDraftRequestDto();
        requestForUpdate = new IdeaRequestDto();
        draftRequest.setName("Draft Idea");
        draftRequest.setText("Draft idea text");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ideas/draft")
                .with(jwt()
                        .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID))))
                .content(objectMapper.writeValueAsString(draftRequest))
                .contentType(MediaType.APPLICATION_JSON));

        // add Under consideration idea with id=6
        id = 6;
        requestForUpdate.setName("Under consideration idea");
        requestForUpdate.setText("Under consideration text");
        mockMvc.perform(post("/api/v1/ideas/draft")
                .with(jwt()
                        .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID))))
                .content(objectMapper.writeValueAsString(draftRequest))
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(put("/api/v1/ideas/" + id)
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID))))
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("Under consideration idea")))
                .andExpect(jsonPath("$.text", is("Under consideration text")))
                .andExpect(jsonPath("$.status", is(UNDER_CONSIDERATION.getValue())));

        // add Accepted idea with id=7
        id = 7;
        requestForUpdate.setName("Accepted idea");
        requestForUpdate.setText("Accepted text");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ideas/draft")
                .with(jwt()
                        .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID))))
                .content(objectMapper.writeValueAsString(draftRequest))
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(put("/api/v1/ideas/" + id)
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID))))
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("Accepted idea")))
                .andExpect(jsonPath("$.text", is("Accepted text")))
                .andExpect(jsonPath("$.status", is(UNDER_CONSIDERATION.getValue())));
        mockMvc.perform(patch("/api/v1/ideas/" + id + "/status?statusId=" + ACCEPTED.getValue())
                        .with(jwt()
                                .authorities(AuthorityUtils.createAuthorityList("ROLE_EXPERT"))
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(EXPERT_ID)))))
                .andExpect(status().isOk());

        // add Rejected idea with id=8
        id = 8;
        requestForUpdate.setName("Rejected idea");
        requestForUpdate.setText("Rejected text");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ideas/draft")
                .with(jwt()
                        .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID))))
                .content(objectMapper.writeValueAsString(draftRequest))
                .contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(put("/api/v1/ideas/" + id)
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_2_ID))))
                        .content(objectMapper.writeValueAsString(requestForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.name", is("Rejected idea")))
                .andExpect(jsonPath("$.text", is("Rejected text")))
                .andExpect(jsonPath("$.status", is(UNDER_CONSIDERATION.getValue())));
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/ideas/" + id + "/status?statusId=" + REJECTED.getValue())
                        .with(jwt()
                                .authorities(AuthorityUtils.createAuthorityList("ROLE_EXPERT"))
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(EXPERT_ID)))))
                .andExpect(status().isOk());
    }


    /**
     * This private methods adds data to the test container for further testing.
     */

    private void dataGenerationDraftOne() throws Exception {

        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName("Draft Idea");
        request.setText("Draft Idea text");


        mockMvc.perform(post("/api/v1/ideas/draft")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Draft Idea")))
                .andExpect(jsonPath("$.text", is("Draft Idea text")));
    }

    private void dataGenerationDraftTwo() throws Exception {

        IdeaDraftRequestDto request = new IdeaDraftRequestDto();
        request.setName("Draft Idea2");
        request.setText("Draft Idea text2");


        mockMvc.perform(post("/api/v1/ideas/draft")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Draft Idea2")))
                .andExpect(jsonPath("$.text", is("Draft Idea text2")));
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
        requestForUpdate.setName("Idea");
        requestForUpdate.setText("Idea test text");

        //создаем черновик
        mockMvc.perform(post("/api/v1/ideas/draft")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Draft Idea")))
                .andExpect(jsonPath("$.text", is("Draft idea text")));

        //публикуем реальную идею(редактируем черновик)
        mockMvc.perform(put("/api/v1/ideas/1")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
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
        requestForUpdate2.setName("Idea2");
        requestForUpdate2.setText("Idea test text2");

        //создаем черновик
        mockMvc.perform(post("/api/v1/ideas/draft")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(request2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Draft Idea2")))
                .andExpect(jsonPath("$.text", is("Draft idea text2")));

        //публикуем реальную идею(редактируем черновик)
        mockMvc.perform(put("/api/v1/ideas/2")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("sub", String.valueOf(USER_ID))))
                        .content(objectMapper.writeValueAsString(requestForUpdate2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is("Idea2")))
                .andExpect(jsonPath("$.text", is("Idea test text2")))
                .andExpect(jsonPath("$.status", is(UNDER_CONSIDERATION.getValue())));

    }
}


