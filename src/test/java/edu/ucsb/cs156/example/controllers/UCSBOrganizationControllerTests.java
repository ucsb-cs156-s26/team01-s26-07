package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
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

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockitoBean UserRepository userRepository;

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_organization() throws Exception {
    UCSBOrganization zpr =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("Zeta Phi Rho")
            .orgTranslation("Zeta Phi Rho Fraternity")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("ZPR"))).thenReturn(Optional.of(zpr));

    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBOrganization").param("orgCode", "ZPR").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("ZPR");
    verify(ucsbOrganizationRepository, times(1)).delete(any());
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id ZPR deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existent_organization_and_gets_right_error_message()
      throws Exception {
    when(ucsbOrganizationRepository.findById(eq("DNE"))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBOrganization").param("orgCode", "DNE").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("DNE");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id DNE not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_organization() throws Exception {
    UCSBOrganization zprOrig =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("Zeta Phi Rho")
            .orgTranslation("Zeta Phi Rho Fraternity")
            .inactive(false)
            .build();

    UCSBOrganization zprEdited =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("ZPR")
            .orgTranslation("Zeta Phi Rho Updated")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(zprEdited);

    when(ucsbOrganizationRepository.findById(eq("ZPR"))).thenReturn(Optional.of(zprOrig));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBOrganization")
                    .param("orgCode", "ZPR")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("ZPR");
    verify(ucsbOrganizationRepository, times(1)).save(zprEdited);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
    UCSBOrganization editedOrg =
        UCSBOrganization.builder()
            .orgCode("DNE")
            .orgTranslationShort("Does Not Exist")
            .orgTranslation("Does Not Exist Organization")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(editedOrg);

    when(ucsbOrganizationRepository.findById(eq("DNE"))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBOrganization")
                    .param("orgCode", "DNE")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("DNE");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id DNE not found", json.get("message"));
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/UCSBOrganization").param("orgCode", "ZPR"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {
    UCSBOrganization zpr =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("Zeta Phi Rho")
            .orgTranslation("Zeta Phi Rho Fraternity")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("ZPR"))).thenReturn(Optional.of(zpr));

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBOrganization").param("orgCode", "ZPR"))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById(eq("ZPR"));
    String expectedJson = mapper.writeValueAsString(zpr);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
    when(ucsbOrganizationRepository.findById(eq("DNE"))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBOrganization").param("orgCode", "DNE"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById(eq("DNE"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id DNE not found", json.get("message"));
  }

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "ZPR")
                .param("orgTranslationShort", "Zeta Phi Rho")
                .param("orgTranslation", "Zeta Phi Rho Fraternity")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "ZPR")
                .param("orgTranslationShort", "Zeta Phi Rho")
                .param("orgTranslation", "Zeta Phi Rho Fraternity")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsborganizations() throws Exception {
    UCSBOrganization zpr =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("Zeta Phi Rho")
            .orgTranslation("Zeta Phi Rho Fraternity")
            .inactive(false)
            .build();

    UCSBOrganization sky =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("Skydiving Club")
            .orgTranslation("UCSB Skydiving Club")
            .inactive(false)
            .build();

    ArrayList<UCSBOrganization> expectedOrgs = new ArrayList<>();
    expectedOrgs.addAll(Arrays.asList(zpr, sky));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrgs);

    MvcResult response =
        mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().isOk()).andReturn();

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrgs);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {
    UCSBOrganization zpr =
        UCSBOrganization.builder()
            .orgCode("ZPR")
            .orgTranslationShort("Zeta Phi Rho")
            .orgTranslation("Zeta Phi Rho Fraternity")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.save(eq(zpr))).thenReturn(zpr);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBOrganization/post")
                    .param("orgCode", "ZPR")
                    .param("orgTranslationShort", "Zeta Phi Rho")
                    .param("orgTranslation", "Zeta Phi Rho Fraternity")
                    .param("inactive", "false")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).save(zpr);
    String expectedJson = mapper.writeValueAsString(zpr);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_inactive_organization() throws Exception {
    UCSBOrganization osli =
        UCSBOrganization.builder()
            .orgCode("OSLI")
            .orgTranslationShort("Student Life")
            .orgTranslation("Office of Student Life")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(osli))).thenReturn(osli);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBOrganization/post")
                    .param("orgCode", "OSLI")
                    .param("orgTranslationShort", "Student Life")
                    .param("orgTranslation", "Office of Student Life")
                    .param("inactive", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).save(osli);
    String expectedJson = mapper.writeValueAsString(osli);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
