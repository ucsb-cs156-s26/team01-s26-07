package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

  @MockitoBean MenuItemReviewRepository menuItemReviewRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/menuitemreview/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/menuitemreview/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/menuitemreview").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  // Authorization tests for /api/menuitemreview/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreview/post")
                .param("itemId", "1")
                .param("reviewerEmail", "a@a.com")
                .param("stars", "2")
                .param("dateReviewed", "2022-01-03T00:00:00")
                .param("comments", "asdf")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/menuitemreview/post")
                .param("itemId", "1")
                .param("reviewerEmail", "a@a.com")
                .param("stars", "2")
                .param("dateReviewed", "2022-01-03T00:00:00")
                .param("comments", "asdf")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  // // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime ldt = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview mir1 =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("a@a.com")
            .stars(2)
            .dateReviewed(ldt)
            .comments("adsf")
            .build();

    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.of(mir1));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/menuitemreview").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(menuItemReviewRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(mir1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(menuItemReviewRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/menuitemreview").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(menuItemReviewRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("MenuItemReview with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menuitemreviews() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview mir1 =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("a@a.com")
            .stars(2)
            .dateReviewed(ldt1)
            .comments("adsf")
            .build();

    LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

    MenuItemReview mir2 =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("a@a.com")
            .stars(2)
            .dateReviewed(ldt2)
            .comments("adsf")
            .build();

    ArrayList<MenuItemReview> expectedMirs = new ArrayList<>();
    expectedMirs.addAll(Arrays.asList(mir1, mir2));

    when(menuItemReviewRepository.findAll()).thenReturn(expectedMirs);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/menuitemreview/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(menuItemReviewRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedMirs);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_menuitemreview() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview mir1 =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("a@a.com")
            .stars(2)
            .dateReviewed(ldt1)
            .comments("adsf")
            .build();

    when(menuItemReviewRepository.save(eq(mir1))).thenReturn(mir1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/menuitemreview/post")
                    .param("itemId", "1")
                    .param("reviewerEmail", "a@a.com")
                    .param("stars", "2")
                    .param("dateReviewed", "2022-01-03T00:00:00")
                    .param("comments", "adsf")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).save(mir1);
    String expectedJson = mapper.writeValueAsString(mir1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_menuitemreview() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

    MenuItemReview mirOrig =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("a@a.com")
            .stars(1)
            .dateReviewed(ldt1)
            .comments("adsf")
            .build();

    MenuItemReview mirEdited =
        MenuItemReview.builder()
            .itemId(2)
            .reviewerEmail("b@a.com")
            .stars(5)
            .dateReviewed(ldt2)
            .comments("greatest update ever")
            .build();

    String requestBody = mapper.writeValueAsString(mirEdited);

    when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.of(mirOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/menuitemreview")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).findById(67L);
    verify(menuItemReviewRepository, times(1)).save(mirEdited); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_menuitemreview_that_does_not_exist() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    MenuItemReview mirEdited =
        MenuItemReview.builder()
            .itemId(1)
            .reviewerEmail("a@a.com")
            .stars(5)
            .dateReviewed(ldt1)
            .comments("greatest update ever II")
            .build();

    String requestBody = mapper.writeValueAsString(mirEdited);

    when(menuItemReviewRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/menuitemreview")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(menuItemReviewRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("MenuItemReview with id 67 not found", json.get("message"));
  }
}
