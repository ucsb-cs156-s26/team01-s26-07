package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for RecommendationRequests */
@Tag(name = "RecommendationRequests")
@RequestMapping("/api/recommendationrequests")
@RestController
@Slf4j
public class RecommendationRequestsController extends ApiController {

  @Autowired RecommendationRequestRepository recommendationRequestRepository;

  /**
   * List all recommendation requests
   *
   * @return an iterable of RecommendationRequest
   */
  @Operation(summary = "List all recommendation requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allRecommendationRequests() {
    Iterable<RecommendationRequest> recommendationRequests =
        recommendationRequestRepository.findAll();
    return recommendationRequests;
  }

  /**
   * Get a single recommendation request by id
   *
   * @param id the id of the recommendation request
   * @return a RecommendationRequest
   */
  @Operation(summary = "Get a single recommendation request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public RecommendationRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    RecommendationRequest recommendationRequest =
        recommendationRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    return recommendationRequest;
  }

  /**
   * Create a new recommendation request
   *
   * @param requesterEmail the requester's email address
   * @param professorEmail the professor's email address
   * @param explanation explanation for recommendation request
   * @param dateRequested date the recommendation request was made
   * @param dateNeeded date the recommendation is needed by
   * @param done whether the recommendation request has been completed
   * @return the saved recommendation request
   */
  @Operation(summary = "Create a new recommendation request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "professorEmail") @RequestParam String professorEmail,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(
              name = "dateRequested",
              description =
                  "date requested (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateRequested")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateRequested,
      @Parameter(
              name = "dateNeeded",
              description =
                  "date needed (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateNeeded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateNeeded,
      @Parameter(name = "done") @RequestParam Boolean done)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("dateRequested={}", dateRequested);
    log.info("dateNeeded={}", dateNeeded);

    RecommendationRequest recommendationRequest = new RecommendationRequest();
    recommendationRequest.setRequesterEmail(requesterEmail);
    recommendationRequest.setProfessorEmail(professorEmail);
    recommendationRequest.setExplanation(explanation);
    recommendationRequest.setDateRequested(dateRequested);
    recommendationRequest.setDateNeeded(dateNeeded);
    recommendationRequest.setDone(done);

    RecommendationRequest savedRecommendationRequest =
        recommendationRequestRepository.save(recommendationRequest);

    return savedRecommendationRequest;
  }

  // /**
  //  * Delete a RecommendationRequest
  //  *
  //  * @param id the id of the recommendation request to delete
  //  * @return a message indicating the recommendation request was deleted
  //  */
  // @Operation(summary = "Delete a RecommendationRequest")
  // @PreAuthorize("hasRole('ROLE_ADMIN')")
  // @DeleteMapping("")
  // public Object deleteRecommendationRequest(@Parameter(name = "id") @RequestParam Long id) {
  //   RecommendationRequest recommendationRequest =
  //       recommendationRequestRepository
  //           .findById(id)
  //           .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

  //   recommendationRequestRepository.delete(recommendationRequest);
  //   return genericMessage("RecommendationRequest with id %s deleted".formatted(id));
  // }

  // /**
  //  * Update a single recommendation request
  //  *
  //  * @param id id of the recommendation request to update
  //  * @param incoming the new recommendation request
  //  * @return the updated recommendation request object
  //  */
  // @Operation(summary = "Update a single recommendation request")
  // @PreAuthorize("hasRole('ROLE_ADMIN')")
  // @PutMapping("")
  // public RecommendationRequest updateRecommendationRequest(
  //     @Parameter(name = "id") @RequestParam Long id,
  //     @RequestBody @Valid RecommendationRequest incoming) {

  //   RecommendationRequest recommendationRequest =
  //       recommendationRequestRepository
  //           .findById(id)
  //           .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

  //   recommendationRequest.setRequesterEmail(incoming.getRequesterEmail());
  //   recommendationRequest.setProfessorEmail(incoming.getProfessorEmail());
  //   recommendationRequest.setExplanation(incoming.getExplanation());
  //   recommendationRequest.setDateRequested(incoming.getDateRequested());
  //   recommendationRequest.setDateNeeded(incoming.getDateNeeded());
  //   recommendationRequest.setDone(incoming.getDone());

  //   recommendationRequestRepository.save(recommendationRequest);

  //   return recommendationRequest;
  // }
}
