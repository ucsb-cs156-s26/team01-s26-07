package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

// maybe?

@WebMvcTest(controllers = HelpRequestsController.class)
@Import(TestConfig.class)
public class HelpRequestsControllerTests extends ControllerTestCase {

  @MockitoBean HelpRequestRepository helpRequestRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/helprequests/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/helprequests/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/helprequests/post")
                .param("requesterEmail", "whamabe@ucsb.edu")
                .param("teamId", "team07")
                .param("tableOrBreakoutRoom", "table07")
                .param("requestTime", "2022-01-03T00:00:00Z")
                .param("explanation", "this is a test help request")
                .param("solved", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/helprequests/post")
                .param("requesterEmail", "whamabe@ucsb.edu")
                .param("teamId", "team07")
                .param("tableOrBreakoutRoom", "table07")
                .param("requestTime", "2022-01-03T00:00:00Z")
                .param("explanation", "this is a test help request")
                .param("solved", "false")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_help_requests() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    ZonedDateTime zdt1 = ZonedDateTime.parse("2022-01-03T00:00:00Z");

    HelpRequest helpRequest1 =
        HelpRequest.builder()
            .requesterEmail("whamabe@ucsb.edu")
            .teamId("team07")
            .tableOrBreakoutRoom("table07")
            .requestTime(zdt1)
            .explanation("this is a test help request")
            .solved(false)
            .build();

    ArrayList<HelpRequest> expectedHelpRequests = new ArrayList<>();
    expectedHelpRequests.add(helpRequest1);

    when(helpRequestRepository.findAll()).thenReturn(expectedHelpRequests);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/helprequests/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(helpRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedHelpRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_help_request() throws Exception {
    // arrange

    ZonedDateTime zdt1 = ZonedDateTime.parse("2022-01-03T00:00:00-08:00");

    HelpRequest helpRequest1 =
        HelpRequest.builder()
            .requesterEmail("whamabe@ucsb.edu")
            .teamId("team07")
            .tableOrBreakoutRoom("table07")
            .requestTime(zdt1)
            .explanation("this is a test help request")
            .solved(true)
            .build();
    when(helpRequestRepository.save(eq(helpRequest1))).thenReturn(helpRequest1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/helprequests/post")
                    .param("requesterEmail", "whamabe@ucsb.edu")
                    .param("teamId", "team07")
                    .param("tableOrBreakoutRoom", "table07")
                    .param("requestTime", "2022-01-03T00:00:00-08:00")
                    .param("explanation", "this is a test help request")
                    .param("solved", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).save(helpRequest1);
    String expectedJson = mapper.writeValueAsString(helpRequest1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);

    // verify(helpRequestRepository).save(argThat(saved -> saved.getSolved() == false));
    assertTrue(responseString.contains(":true"));
  }

  // ----------------------
  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/helprequests").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    ZonedDateTime zdt = ZonedDateTime.parse("2022-01-03T00:00:00Z");

    HelpRequest helpRequest =
        HelpRequest.builder()
            .requesterEmail("whamabe@ucsb.edu")
            .teamId("team07")
            .tableOrBreakoutRoom("table07")
            .requestTime(zdt)
            .explanation("this is a test help request")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(helpRequest));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/helprequests").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(helpRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(helpRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/helprequests").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(helpRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 7 not found", json.get("message"));
  }
}
