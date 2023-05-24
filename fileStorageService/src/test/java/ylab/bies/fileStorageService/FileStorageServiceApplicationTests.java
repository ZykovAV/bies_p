package ylab.bies.fileStorageService;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
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

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
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
  private MinioClient minioClient;
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
    when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);
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

    ArgumentCaptor<PutObjectArgs> argumentCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
    Mockito.verify(minioClient, only()).putObject(argumentCaptor.capture());

    //check arguments sent to S3
    PutObjectArgs argumentsSentToS3 = argumentCaptor.getValue();
    UUID fileUUIDSentToS3 = UUID.fromString(argumentsSentToS3.object().split("/")[1]);
    Long ideaIdSentToS3 = Long.parseLong(argumentsSentToS3.object().split("/")[0]);
    assertEquals(ideaId, ideaIdSentToS3);
    assertEquals(mockMultipartFile.getContentType(), argumentsSentToS3.contentType());

    //check object saved to database
    FileModel fileModelSavedToDB = fileService.getByFileId(fileUUIDSentToS3).orElse(null);
    assertNotNull(fileModelSavedToDB);
    assertEquals(ideaId, fileModelSavedToDB.getIdeaId());
    assertEquals(mockMultipartFile.getOriginalFilename(), fileModelSavedToDB.getFileName());
    assertEquals(mockMultipartFile.getContentType(), fileModelSavedToDB.getContentType());
    assertEquals(mockMultipartFile.getSize(), fileModelSavedToDB.getFileSize());
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

    verify(minioClient, never()).putObject(any(PutObjectArgs.class));
  }

  @Test
  void shouldNotSaveToDbAndReturnStatus500WhenS3Throws() throws Exception {
    ideaId = 997L;
    when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(ServerException.class);

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

    verify(minioClient, only()).putObject(any(PutObjectArgs.class));
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

}
