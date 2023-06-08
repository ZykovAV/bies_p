package ylab.bies.ideaservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ylab.bies.ideaservice.dto.request.IdeaDraftRequestDto;
import ylab.bies.ideaservice.dto.request.IdeaRequestDto;
import ylab.bies.ideaservice.dto.response.IdeaDraftResponseDto;
import ylab.bies.ideaservice.dto.response.IdeaResponseDto;
import ylab.bies.ideaservice.service.IdeaService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@RestController
@RequestMapping(value = "/api/v1/ideas")
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Idea Controller", description = "REST operations with ideas")
@RequiredArgsConstructor
@Slf4j
public class IdeaController {

    private final IdeaService ideaService;

    @Operation(summary = "Create a new draft idea", responses = {
            @ApiResponse(responseCode = "201", description = "Draft idea created",
                    content = @Content(schema = @Schema(implementation = IdeaDraftResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/draft")
    public ResponseEntity<IdeaDraftResponseDto> createDraftIdea(@Valid @RequestBody IdeaDraftRequestDto request) {
        IdeaDraftResponseDto response = ideaService.createDraftIdea(request);
        log.info("Response with created idea: {}", response);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @Operation(summary = "Get idea by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Found idea",
                    content = @Content(schema = @Schema(implementation = IdeaResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Idea not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/{id}")
    public ResponseEntity<IdeaResponseDto> getById(@PathVariable Long id) {
        return new ResponseEntity<>(ideaService.findById(id), HttpStatus.OK);
    }


    @Operation(summary = "Change idea status", responses = {
            @ApiResponse(responseCode = "200", description = "Status changed"),
            @ApiResponse(responseCode = "304", description = "Status not changed"),
            @ApiResponse(responseCode = "404", description = "Idea not found")
    })
    @PreAuthorize("hasRole('EXPERT')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<HttpStatus> changeStatus(@PathVariable Long id, @RequestParam Integer statusId) {
        ideaService.changeStatus(id, statusId);
        return ResponseEntity.ok(HttpStatus.OK);
    }


    @Operation(summary = "Like an idea", responses = {
            @ApiResponse(responseCode = "200", description = "Idea liked"),
            @ApiResponse(responseCode = "304", description = "Like has been rejected"),
            @ApiResponse(responseCode = "404", description = "Idea not found")
    })
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/like")
    public ResponseEntity<HttpStatus> like(@PathVariable Long id) {
        ideaService.rate(id, true);
        return ResponseEntity.ok(HttpStatus.OK);
    }


    @Operation(summary = "Dislike an idea", responses = {
            @ApiResponse(responseCode = "200", description = "Idea disliked"),
            @ApiResponse(responseCode = "304", description = "Dislike has been rejected"),
            @ApiResponse(responseCode = "404", description = "Idea not found")
    })
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/dislike")
    public ResponseEntity<HttpStatus> dislike(@PathVariable Long id) {
        ideaService.rate(id, false);
        return ResponseEntity.ok(HttpStatus.OK);
    }


    @Operation(summary = "Checking if current user is an author of idea", responses = {
            @ApiResponse(responseCode = "200", description = "'true' if current user is an author, 'false' - if not",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "Idea not found")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/is-author")
    public ResponseEntity<Boolean> isCurrentUserAuthor(@PathVariable Long id) {
        return ResponseEntity.ok(ideaService.isCurrentUserAuthor(id));
    }


    @Operation(summary = "Get all ideas", responses = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Page<IdeaResponseDto>> getAllIdeas(@NotNull final Pageable pageable) {
        Page<IdeaResponseDto> ideas = ideaService.getAllIdeas(pageable);
        log.info(String.format("Ideas %s received successfully", ideas));
        return new ResponseEntity<>(ideas, HttpStatus.OK);
    }


    @Operation(summary = "Update an idea", responses = {
            @ApiResponse(responseCode = "200", description = "Idea updated",
                    content = @Content(schema = @Schema(implementation = IdeaResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Idea not found")
    })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<IdeaResponseDto> updateIdea(@Valid @RequestBody IdeaRequestDto editRequest, @PathVariable Long id) {
        IdeaResponseDto updatedIdea = ideaService.updateIdea(id, editRequest);
        log.info("Idea updated successfully");
        return new ResponseEntity<>(updatedIdea, HttpStatus.OK);
    }



    @Operation(summary = "Get all user's ideas", responses = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/my")
    public ResponseEntity<Page<IdeaResponseDto>> getAllUsersIdeas(@NotNull final Pageable pageable) {
        Page<IdeaResponseDto> ideas = ideaService.getAllUsersIdeas(pageable);
        log.info(String.format(" User's ideas %s received successfully", ideas));
        return new ResponseEntity<>(ideas, HttpStatus.OK);
    }
}
