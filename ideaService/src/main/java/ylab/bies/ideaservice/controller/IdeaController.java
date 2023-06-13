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

/**
 * Idea request processing controller
 */
@RestController
@RequestMapping(value = "/api/v1/ideas")
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Idea Controller", description = "REST operations with ideas")
@RequiredArgsConstructor
@Slf4j
public class IdeaController {

    private final IdeaService ideaService;

    /**
     * Create a draft
     * @param request contains required parameters 'name' and 'text'
     * @return created draft (idea with status DRAFT)
     */
    @Operation(summary = "Create a new draft idea", responses = {
            @ApiResponse(responseCode = "201", description = "Draft idea created",
                    content = @Content(schema = @Schema(implementation = IdeaDraftResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/draft")
    public ResponseEntity<IdeaDraftResponseDto> createDraftIdea(@Valid @RequestBody IdeaDraftRequestDto request) {
        IdeaDraftResponseDto response = ideaService.createDraftIdea(request);
        log.info("Response with created idea: {}", response);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get idea by id. Returns only idea with statuses UNDER_CONSIDERATION, ACCEPTER, REJECTED and
     * current user's DRAFT.
     * @param id of idea
     * @return an idea with required id
     */
    @Operation(summary = "Get idea by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Found idea",
                    content = @Content(schema = @Schema(implementation = IdeaResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Idea not found", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/{id}")
    public ResponseEntity<IdeaResponseDto> getById(@PathVariable Long id) {
        return new ResponseEntity<>(ideaService.findById(id), HttpStatus.OK);
    }

    /**
     * Change status of idea. Only available to experts.
     * It is possible to change the status only for ideas with a status UNDER_CONSIDERATION
     * @param id of idea
     * @param statusId (3 - ACCEPTED or 4 - REJECTED)
     */
    @Operation(summary = "Change idea status", responses = {
            @ApiResponse(responseCode = "200", description = "Status changed", content = @Content),
            @ApiResponse(responseCode = "403", description = "Status not changed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Idea not found", content = @Content)
    })
    @PreAuthorize("hasRole('EXPERT')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam Integer statusId) {
        ideaService.changeStatus(id, statusId);
        return ResponseEntity.ok().build();
    }

    /**
     * Like an idea. It is possible to like only other user's idea with status UNDER_CONSIDERATION
     * @param id of idea
     */
    @Operation(summary = "Like an idea", responses = {
            @ApiResponse(responseCode = "200", description = "Idea liked", content = @Content),
            @ApiResponse(responseCode = "403", description = "Like has been rejected", content = @Content),
            @ApiResponse(responseCode = "404", description = "Idea not found", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/like")
    public ResponseEntity<Void> like(@PathVariable Long id) {
        ideaService.rate(id, true);
        return ResponseEntity.ok().build();
    }

    /**
     * Dislike an idea. It is possible to dislike only other user's idea with status UNDER_CONSIDERATION
     * @param id of idea
     */
    @Operation(summary = "Dislike an idea", responses = {
            @ApiResponse(responseCode = "200", description = "Idea disliked", content = @Content),
            @ApiResponse(responseCode = "403", description = "Dislike has been rejected", content = @Content),
            @ApiResponse(responseCode = "404", description = "Idea not found", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/dislike")
    public ResponseEntity<Void> dislike(@PathVariable Long id) {
        ideaService.rate(id, false);
        return ResponseEntity.ok().build();
    }

    /**
     * Check if current user is an author of idea
     * @param id of idea
     * @return 'true' if current user is an author of idea, or 'false' - if not
     */
    @Operation(summary = "Checking if current user is an author of idea", responses = {
            @ApiResponse(responseCode = "200", description = "'true' if current user is an author, 'false' - if not",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "Idea not found", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/is-author")
    public ResponseEntity<Boolean> isCurrentUserAuthor(@PathVariable Long id) {
        return ResponseEntity.ok(ideaService.isCurrentUserAuthor(id));
    }

    /**
     * Get a list of ideas. Returns only ideas with statuses UNDER_CONSIDERATION, ACCEPTER, REJECTED.
     * @param pageable contains page number, how many results on page, sort options.
     * @return page with ideas
     */
    @Operation(summary = "Get all ideas", responses = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid pageable",
                    content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Page<IdeaResponseDto>> getAllIdeas(@NotNull final Pageable pageable) {
        Page<IdeaResponseDto> ideas = ideaService.getAllIdeas(pageable);
        log.info(String.format("Ideas %s received successfully", ideas));
        return new ResponseEntity<>(ideas, HttpStatus.OK);
    }

    /**
     * Edit and publish the idea. It is possible only edit user's own idea with DRAFT or UNDER_CONSIDERATION status.
     * If a draft is being edited, it will be automatically published (status changed to UNDER_CONSIDERATION)
     * @param editRequest contains required parameters 'name' and 'text'
     * @param id of idea
     * @return updated idea
     */
    @Operation(summary = "Update an idea",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Idea updated",
                            content = @Content(schema = @Schema(implementation = IdeaResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request data",
                            content = @Content),
                    @ApiResponse(responseCode = "404", description = "Idea not found",
                            content = @Content),
                    @ApiResponse(responseCode = "403", description = "Access to someone else's idea is prohibited",
                            content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<IdeaResponseDto> updateIdea(@Valid @RequestBody IdeaRequestDto editRequest, @PathVariable Long id) {
        IdeaResponseDto updatedIdea = ideaService.updateIdea(id, editRequest);
        log.info("Idea updated successfully");
        return new ResponseEntity<>(updatedIdea, HttpStatus.OK);
    }

    /**
     * Get a list of current user's ideas.
     * @param pageable contains page number, how many results on page, sort options.
     * @return page with current user's ideas
     */
    @Operation(summary = "Get all user's ideas", responses = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid pageable",
                    content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/my")
    public ResponseEntity<Page<IdeaResponseDto>> getAllUsersIdeas(@NotNull final Pageable pageable) {
        Page<IdeaResponseDto> ideas = ideaService.getAllUsersIdeas(pageable);
        log.info(String.format(" User's ideas %s received successfully", ideas));
        return new ResponseEntity<>(ideas, HttpStatus.OK);
    }
}
