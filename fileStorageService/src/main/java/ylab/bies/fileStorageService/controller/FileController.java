package ylab.bies.fileStorageService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ylab.bies.fileStorageService.dto.FileListByIdeaDto;
import ylab.bies.fileStorageService.entity.FileModel;
import ylab.bies.fileStorageService.service.FileService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping(value = "api/v1/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

  private final FileService fileService;

  @Operation(
          summary = "Get list of files by idea id",
          responses = {
                  @ApiResponse(
                          responseCode = "200",
                          description = "OK"),
                  @ApiResponse(
                          responseCode = "500",
                          description = "Internal server error")
          }
  )
  @GetMapping("/by-idea/{idea_id}")
  public FileListByIdeaDto getFileListByIdea(@PathVariable("idea_id") Long ideaId) {
    //TODO: security logic to be updated
    return fileService.getFileListByIdeaId(ideaId);
  }

  @GetMapping("/{file_id}")
  public ResponseEntity<byte[]> getFileById(@PathVariable("file_id") UUID fileId) {
    FileModel fileModel = fileService.getFileWithBodyById(fileId);

    ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(fileModel.getContentType()));

    try {
      String fileName = URLEncoder.encode(fileModel.getFileName(), StandardCharsets.UTF_8.toString());
      responseBuilder.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
    } catch (UnsupportedEncodingException e) {
      log.info("Unable to encode file name {} for a file with id {} ", fileModel.getFileName(), fileModel.getId());
    }

    return responseBuilder.body(fileModel.getBody());
  }

  @Operation(
          summary = "Save file attachment for idea to S3",
          responses = {
                  @ApiResponse(
                          responseCode = "201",
                          description = "File successfully saved"),
                  @ApiResponse(
                          responseCode = "400",
                          description = "Data format is incorrect. Idea id and file name must not be empty."),
                  @ApiResponse(
                          responseCode = "403",
                          description = "User is not authorized for editing idea with id specified"),
                  @ApiResponse(
                          responseCode = "500",
                          description = "Internal server error")
          }
  )
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> saveFile(@RequestParam("idea_id") Long ideaId,
                                       @RequestParam MultipartFile file) {
    log.info("File received: " + file.getOriginalFilename());
    //TODO: token logic to be updated
    String dummyToken = "test";
    fileService.addFile(ideaId, file, dummyToken);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/{file_id}")
  public ResponseEntity<Void> removeFile(@PathVariable("file_id") UUID fileId) {
    fileService.removeFile(fileId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}
