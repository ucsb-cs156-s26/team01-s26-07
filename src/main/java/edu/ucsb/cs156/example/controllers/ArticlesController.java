package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for Articles */
@Tag(name = "Articles")
@RequestMapping("/api/articles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {

  @Autowired ArticlesRepository articlesRepository;

  /**
   * List all Articles
   *
   * @return an iterable of Articles
   */
  @Operation(summary = "List all articles")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<Articles> allUCSBDates() {
    Iterable<Articles> ret = articlesRepository.findAll();
    return ret;
  }

  /**
   * Get a single article by id
   *
   * @param id the id of the article
   * @return a article
   */
  @Operation(summary = "Get a single article by id")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public Articles getById(@Parameter(name = "id") @RequestParam Long id) {
    Articles ret =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    return ret;
  }

  /**
   * Create a new article
   *
   * @param title title of article
   * @param url url of artucle
   * @param explanation brief explanation of the article
   * @param email email of author
   * @param dateAdded date of the article added
   * @return the saved article
   */
  @Operation(summary = "Create a new article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public Articles postUCSBDate(
      @Parameter(name = "title") @RequestParam String title,
      @Parameter(name = "url") @RequestParam String url,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "email") @RequestParam String email,
      @Parameter(
              name = "dateAdded",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateAdded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateadded)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("localDateTime={}", dateadded);

    Articles article = new Articles();
    article.setTitle(title);
    article.setDateAdded(dateadded);
    article.setExplanation(explanation);
    article.setEmail(email);
    article.setUrl(url);

    Articles savedArticle = articlesRepository.save(article);

    return savedArticle;
  }

  /**
   * Delete a article
   *
   * @param id the id of the article to delete
   * @return a message indicating the article was deleted
   */
  @Operation(summary = "Delete a article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteUCSBDate(@Parameter(name = "id") @RequestParam Long id) {
    Articles article =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    articlesRepository.delete(article);
    return genericMessage("Article with id %s deleted".formatted(id));
  }

  /**
   * Update a single article
   *
   * @param id id of the article to update
   * @param incoming the new article
   * @return the updated article
   */
  @Operation(summary = "Update a single article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public Articles updateUCSBDate(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid Articles incoming) {

    Articles article =
        articlesRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    article.setDateAdded(incoming.getDateAdded());
    article.setEmail(incoming.getEmail());
    article.setUrl(incoming.getUrl());
    article.setExplanation(incoming.getExplanation());
    article.setTitle(incoming.getTitle());

    articlesRepository.save(article);

    return article;
  }
}
