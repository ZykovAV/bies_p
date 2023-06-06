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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.UriComponentsBuilder;
import ylab.bies.fileStorageService.entity.FileModel;
import ylab.bies.fileStorageService.exception.OperationFailedException;
import ylab.bies.fileStorageService.service.FileService;
import ylab.bies.fileStorageService.service.impl.IdeaServiceClientImpl;
import ylab.bies.fileStorageService.service.impl.MinioServiceImpl;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileStorageServiceApplicationTests {

  private final String url = "/api/v1/files";
  private final String filesByIdeaEndpoint = "/by-idea/{idea_id}";
  private final String downloadFileEndpoint = "/{file_id}";
  private final String deleteFileEndpoint = "/{file_id}";

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
  private byte[] data;
  private String fileName;

  @BeforeEach
  void setup() throws Exception {
    data = "Hello world".getBytes();
    fileName = "hello.txt";
    mockMultipartFile = new MockMultipartFile(
            "file",
            fileName,
            MediaType.TEXT_PLAIN_VALUE,
            data
    );
    doNothing().when(minioService).putObject(anyString(), anyString(), any(MockMultipartFile.class));
    doNothing().when(minioService).removeObject(anyString(), anyString());
    when(ideaServiceClient.validateIdeaOwner(anyLong())).thenReturn(true);
  }

  @Test
  void shouldSaveFileAndReturnStatus201() throws Exception {
    ideaId = 999L;
    mockMvcPerformPostToSaveFile()
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

  ResultActions mockMvcPerformPostToSaveFile() throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders
            .multipart(HttpMethod.POST, url)
            .file(mockMultipartFile)
            .param("idea_id", ideaId.toString())
            .with(jwt()));
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
    when(ideaServiceClient.validateIdeaOwner(anyLong())).thenReturn(false);

    mockMvcPerformPostToSaveFile()
            .andDo(print())
            .andExpect(status().isForbidden());

    //make sure nothing was saved to db
    mockMvcPerformGetFilesByIdeaId()
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files", hasSize(0)));

    verify(minioService, never()).putObject(anyString(), anyString(), any(MockMultipartFile.class));
  }

  ResultActions mockMvcPerformGetFilesByIdeaId() throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders
            .get(UriComponentsBuilder.newInstance()
                    .path(url)
                    .path(filesByIdeaEndpoint)
                    .build(ideaId))
            .with(jwt()));
  }

  @Test
  void shouldNotSaveToDbAndReturnStatus500WhenS3Throws() throws Exception {
    ideaId = 997L;
    doThrow(ServerException.class).when(minioService).putObject(anyString(), anyString(), any(MockMultipartFile.class));

    mockMvcPerformPostToSaveFile()
            .andDo(print())
            .andExpect(status().isInternalServerError());

    //make sure db transaction is reverted
    mockMvcPerformGetFilesByIdeaId()
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files", hasSize(0)));

    verify(minioService, only()).putObject(anyString(), anyString(), any(MockMultipartFile.class));
  }

  @Test
  void shouldReturnListOfFilesForIdeaId() throws Exception {
    ideaId = 999L;

    mockMvcPerformGetFilesByIdeaId()
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files").isArray())
            .andExpect(jsonPath("$.files", hasSize(1)));
  }

  @Test
  void shouldReturnEmptyListOfFilesForIdeaWithoutFiles() throws Exception {
    ideaId = 111L;

    mockMvcPerformGetFilesByIdeaId()
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
    mockMvcPerformPostToSaveFile()
            .andExpect(status().isCreated());

    //make sure file is there
    mockMvcPerformGetFilesByIdeaId()
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files").isArray())
            .andExpect(jsonPath("$.files", hasSize(1)));

    //get uuid for the saved file
    UUID fileUUID = getUUIDWithArgumentCaptor();

    //remove file
    mockMvcPerformDeleteByFileId(fileUUID)
            .andExpect(status().isNoContent());

    //make sure file is not there anymore
    mockMvcPerformGetFilesByIdeaId()
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files", hasSize(0)));
  }

  ResultActions mockMvcPerformDeleteByFileId(UUID fileUUID) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders
            .delete(UriComponentsBuilder.newInstance()
                    .path(url)
                    .path(deleteFileEndpoint)
                    .build(fileUUID))
            .with(jwt()));
  }

  @Test
  void shouldReturnStatus404WhenRemoveWithWrongUUID() throws Exception {
    mockMvcPerformDeleteByFileId(UUID.randomUUID())
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldNotRemoveAndReturnStatus500WhenS3Throws() throws Exception {
    doThrow(ServerException.class).when(minioService).removeObject(anyString(), anyString());

    //put file for removal
    ideaId = 556L;
    mockMvcPerformPostToSaveFile()
            .andExpect(status().isCreated());

    //make sure file is there
    mockMvcPerformGetFilesByIdeaId()
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files").isArray())
            .andExpect(jsonPath("$.files", hasSize(1)));

    //get uuid for the saved file
    UUID fileUUID = getUUIDWithArgumentCaptor();

    //try to remove file
    mockMvcPerformDeleteByFileId(fileUUID)
            .andExpect(status().isInternalServerError());

    //make sure transaction is reverted and file is still in db
    mockMvcPerformGetFilesByIdeaId()
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ideaId").value(ideaId))
            .andExpect(jsonPath("$.files", hasSize(1)));
  }

  @Test
  void shouldReturnFileAsByteArray() throws Exception {
    ideaId = 222L;
    when(minioService.getObject(anyString(), anyString(), anyLong())).thenReturn(data);

    //post file to be downloaded
    mockMvcPerformPostToSaveFile()
            .andExpect(status().isCreated());

    UUID fileUUID = getUUIDWithArgumentCaptor();
    //download file
    MvcResult result = mockMvcPerformDownloadFile(fileUUID)
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\""))
            .andReturn();
    assertArrayEquals(data, result.getResponse().getContentAsByteArray());
  }

  private ResultActions mockMvcPerformDownloadFile(UUID fileUUID) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders
            .get(UriComponentsBuilder.newInstance()
                    .path(url)
                    .path(downloadFileEndpoint)
                    .build(fileUUID))
            .with(jwt()));
  }


  @Test
  void shouldReturnStatus404WhenGetWithWrongFileId() throws Exception {
    ideaId = 221L;
    when(minioService.getObject(anyString(), anyString(), anyLong())).thenReturn(data);

    UUID fileUUID = UUID.randomUUID();
    mockMvcPerformDownloadFile(fileUUID)
            .andDo(print())
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn500StatusWhenS3Throws() throws Exception {
    ideaId = 223L;
    when(minioService.getObject(anyString(), anyString(), anyLong())).thenThrow(OperationFailedException.class);

    //post file to be downloaded
    mockMvcPerformPostToSaveFile()
            .andExpect(status().isCreated());

    UUID fileUUID = getUUIDWithArgumentCaptor();
    //download file
    mockMvcPerformDownloadFile(fileUUID)
            .andDo(print())
            .andExpect(status().isInternalServerError());
  }

}
