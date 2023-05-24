package ylab.bies.fileStorageService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ylab.bies.fileStorageService.dto.FileListByIdeaDto;
import ylab.bies.fileStorageService.exception.ErrorResponse;
import ylab.bies.fileStorageService.service.FileService;

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
                          description = "Internal server error",
                          content = @Content(
                                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                                  schema = @Schema(implementation = ErrorResponse.class)))
          }
  )
  @GetMapping("/by-idea/{idea_id}")
  public FileListByIdeaDto getFileListByIdea(@PathVariable("idea_id") Long ideaId) {
    //TODO: security logic to be updated
    return fileService.getFileListByIdeaId(ideaId);
  }

  @Operation(
          summary = "Save file attachment for idea to S3",
          responses = {
                  @ApiResponse(
                          responseCode = "201",
                          description = "File successfully saved"),
                  @ApiResponse(
                          responseCode = "400",
                          description = "Data format is incorrect. Idea id and file name must not be empty.",
                          content = @Content(
                                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                                  schema = @Schema(implementation = ErrorResponse.class))),
                  @ApiResponse(
                          responseCode = "403",
                          description = "User is not authorized for editing idea with id specified",
                          content = @Content(
                                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                                  schema = @Schema(implementation = ErrorResponse.class))),
                  @ApiResponse(
                          responseCode = "500",
                          description = "Internal server error",
                          content = @Content(
                                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                                  schema = @Schema(implementation = ErrorResponse.class))),
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

}
