package ylab.bies.fileStorageService;

import io.minio.errors.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ylab.bies.fileStorageService.entity.FileModel;
import ylab.bies.fileStorageService.service.FileService;
import ylab.bies.fileStorageService.service.impl.IdeaServiceClientImpl;
import ylab.bies.fileStorageService.service.impl.MinioServiceImpl;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileStorageServiceApplicationTests {

  private final String url = "/api/v1/files";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private FileService fileService;

  @MockBean
  private MinioServiceImpl minioService;
  @MockBean
  private IdeaServiceClientImpl ideaServiceClient;

  private MockMultipartFile mockMultipartFile;
  private Long ideaId;

  @BeforeEach
  void setup() throws Exception {
    mockMultipartFile = new MockMultipartFile(
            "file",
            "hello.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello world".getBytes()
    );
    doNothing().when(minioService).putObject(anyString(), anyString(), any(MockMultipartFile.class));
    doNothing().when(minioService).removeObject(anyString(), anyString());
    when(ideaServiceClient.validateIdeaOwner(anyLong(), anyString())).thenReturn(true);
  }

  @Test
  void shouldSaveFileAndReturnStatus201() throws Exception {
    ideaId = 999L;
    mockMvc.perform(MockMvcRequestBuilders
                    .multipart(HttpMethod.POST, url)
                    .file(mockMultipartFile)
                    .param("idea_id", ideaId.toString()))
            .andDo(print())
            .andExpect(status().isCreated());

    UUID fileUUIDSentToS3 = getUUIDWithArgumentCaptor();

    //check object saved to database
    FileModel fileModelSavedToDB = fileService.getByFileId(fileUUIDSentToS3).orElse(null);
    assertNotNull(fileModelSavedToDB);
    assertEquals(ideaId, fileModelSavedToDB.getIdeaId());
    assertEquals(mockMultipartFile.getOriginalFilename(), fileModelSavedToDB.getFileName());
    assertEquals(mockMultipartFile.getContentType(), fileModelSavedToDB.getContentType());
    assertEquals(mockMultipartFile.getSize(), fileModelSavedToDB.getFileSize());
  }

  private UUID getUUIDWithArgumentCaptor() throws Exception {
    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(minioService, only()).putObject(anyString(), argumentCaptor.capture(), any(MockMultipartFile.class));

    String objectKeySentToS3 = argumentCaptor.getValue();
    return UUID.fromString(objectKeySentToS3.split("/")[1]);
  }

  @Test
  void shouldNotSaveToDbAndReturnStatus403WhenWrongIdeaOwner() throws Exception {
    ideaId = 998L;
    when(ideaServiceClient.validateIdeaOwner(anyLong(), anyString())).thenReturn(false);

    mockMvc.perform(MockMvcRequestBuilders
                    .multipart(HttpMethod.POST, url)
                    .file(mockMultipartFile)
                    .param("idea_id", ideaId.toString()))
            .andDo(print())
            .andExpect(status().isForbidden());

    //make sure nothing was saved to db
    mockMvc.perform(MockMvcRequestBuilders
                    .get(url + "/by-idea/" + ideaId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files", hasSize(0)));

    verify(minioService, never()).putObject(anyString(), anyString(), any(MockMultipartFile.class));
  }

  @Test
  void shouldNotSaveToDbAndReturnStatus500WhenS3Throws() throws Exception {
    ideaId = 997L;
    doThrow(ServerException.class).when(minioService).putObject(anyString(), anyString(), any(MockMultipartFile.class));

    mockMvc.perform(MockMvcRequestBuilders
                    .multipart(HttpMethod.POST, url)
                    .file(mockMultipartFile)
                    .param("idea_id", ideaId.toString()))
            .andDo(print())
            .andExpect(status().isInternalServerError());

    //make sure db transaction is reverted
    mockMvc.perform(MockMvcRequestBuilders
                    .get(url + "/by-idea/" + ideaId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files", hasSize(0)));

    verify(minioService, only()).putObject(anyString(), anyString(), any(MockMultipartFile.class));
  }

  @Test
  void shouldReturnListOfFilesForIdeaId() throws Exception {
    ideaId = 999L;

    mockMvc.perform(MockMvcRequestBuilders
                    .get(url + "/by-idea/" + ideaId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files").isArray())
            .andExpect(jsonPath("$.files", hasSize(1)));
  }

  @Test
  void shouldReturnEmptyListOfFilesForIdeaWithoutFiles() throws Exception {
    ideaId = 111L;

    mockMvc.perform(MockMvcRequestBuilders
                    .get(url + "/by-idea/" + ideaId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files").isArray())
            .andExpect(jsonPath("$.files", hasSize(0)));
  }

  @Test
  void shouldRemoveFileAndReturnStatus200() throws Exception {
    //put file for removal
    ideaId = 555L;
    mockMvc.perform(MockMvcRequestBuilders
                    .multipart(HttpMethod.POST, url)
                    .file(mockMultipartFile)
                    .param("idea_id", ideaId.toString()))
            .andExpect(status().isCreated());

    //make sure file is there
    mockMvc.perform(MockMvcRequestBuilders
                    .get(url + "/by-idea/" + ideaId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files").isArray())
            .andExpect(jsonPath("$.files", hasSize(1)));

    //get uuid for the saved file
    UUID fileUUID = getUUIDWithArgumentCaptor();

    //remove file
    mockMvc.perform(MockMvcRequestBuilders
                    .delete(url + "/" + fileUUID))
            .andExpect(status().isOk());

    //make sure file is not there anymore
    mockMvc.perform(MockMvcRequestBuilders
                    .get(url + "/by-idea/" + ideaId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files", hasSize(0)));
  }

  @Test
  void shouldReturnStatus404WhenRemoveWithWrongUUID() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders
                    .delete(url + "/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldNotRemoveAndReturnStatus500WhenS3Throws() throws Exception {
    doThrow(ServerException.class).when(minioService).removeObject(anyString(), anyString());

    //put file for removal
    ideaId = 556L;
    mockMvc.perform(MockMvcRequestBuilders
                    .multipart(HttpMethod.POST, url)
                    .file(mockMultipartFile)
                    .param("idea_id", ideaId.toString()))
            .andExpect(status().isCreated());

    //make sure file is there
    mockMvc.perform(MockMvcRequestBuilders
                    .get(url + "/by-idea/" + ideaId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files").isArray())
            .andExpect(jsonPath("$.files", hasSize(1)));

    //get uuid for the saved file
    UUID fileUUID = getUUIDWithArgumentCaptor();

    //try to remove file
    mockMvc.perform(MockMvcRequestBuilders
                    .delete(url + "/" + fileUUID))
            .andExpect(status().isInternalServerError());

    //make sure transaction reverted and file is still there
    mockMvc.perform(MockMvcRequestBuilders
                    .get(url + "/by-idea/" + ideaId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files", hasSize(1)));
  }

}
