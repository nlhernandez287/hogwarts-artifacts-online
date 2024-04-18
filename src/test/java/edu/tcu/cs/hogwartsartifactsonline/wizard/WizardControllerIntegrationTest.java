package edu.tcu.cs.hogwartsartifactsonline.wizard;

import edu.tcu.cs.hogwartsartifactsonline.IntegrationTest;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import edu.tcu.cs.hogwartsartifactsonline.wizard.dto.WizardDto;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class WizardControllerIntegrationTest extends IntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String token;


    @Value("${api.endpoint.base-url}")
    String BASE_URL;

    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.BASE_URL + "/users/login")
                .with(httpBasic("admin", "password2")));

        MvcResult mvcResult = resultActions.andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(contentAsString);
        this.token = "Bearer " + jsonObject.getJSONObject("data").getString("token");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindAllWizardsSuccess() throws Exception {
        this.mockMvc.perform(get(BASE_URL + "/wizards").header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find all success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
    }

    @Test
    void testFindAllWizardsErrorWhenInvalidTokenIdProvided() throws Exception {
        this.mockMvc.perform(get(BASE_URL + "/wizards").header("Authorization", this.token + "invalid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Access token provided is expired, revoked, malformed, or invalid for other reasons."))
                .andExpect(jsonPath("$.data").value("An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature"));
    }

    @Test
    void testFindWizardByIdSuccess() throws Exception {
        var wizardId = 1;
        this.mockMvc.perform(get(BASE_URL + "/wizards/" + wizardId).header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find one success"))
                .andExpect(jsonPath("$.data.id").value(wizardId))
                .andExpect(jsonPath("$.data.name").value("Albus Dumbledore"))
                .andExpect(jsonPath("$.data.numberOfArtifacts").value(2));
    }

    @Test
    void testFindWizardByIdErrorWithNonExistentWizardId() throws Exception {
        var invalidId = 5;
        this.mockMvc.perform(get(BASE_URL + "/wizards/" + invalidId).header("Authorization", this.token).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find %s with Id %s".formatted("wizard", invalidId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindWizardByIdErrorWhenInvalidTokenIdProvided() throws Exception {
        var wizardId = 1;
        this.mockMvc.perform(get(BASE_URL + "/wizards" + wizardId).header("Authorization", this.token + "invalid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Access token provided is expired, revoked, malformed, or invalid for other reasons."))
                .andExpect(jsonPath("$.data").value("An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature"));
    }

    @Test
    void testAddWizardSuccess() throws Exception {
        var wizardDto = new WizardDto(null,
                "Harry Potter",
                null);

        var wizardDtoJson = objectMapper.writeValueAsString(wizardDto);

        this.mockMvc.perform(post(BASE_URL + "/wizards").header("Authorization", this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wizardDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value(wizardDto.name()))
                .andExpect(jsonPath("$.data.numberOfArtifacts").value(0));

        this.mockMvc.perform(get(BASE_URL + "/wizards").header("Authorization", this.token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data", Matchers.hasSize(4)));
    }

    @Test
    void testAddWizardErrorWhenInvalidTokenIdProvided() throws Exception {
        var wizardDto = new WizardDto(null,
                "Harry Potter",
                null);

        var wizardDtoJson = objectMapper.writeValueAsString(wizardDto);

        this.mockMvc.perform(post(BASE_URL + "/wizards").header("Authorization", this.token + "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wizardDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Access token provided is expired, revoked, malformed, or invalid for other reasons."))
                .andExpect(jsonPath("$.data").value("An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature"));
    }

    @Test
    void testUpdateWizardSuccess() throws Exception {
        var wizardId = 3;

        var wizardDto = new WizardDto(null,
                "Nevile Longbottom Updated",
                null);

        var wizardDtoJson = objectMapper.writeValueAsString(wizardDto);

        this.mockMvc.perform(put(BASE_URL + "/wizards/" + wizardId).header("Authorization", this.token)
                        .contentType(MediaType.APPLICATION_JSON).content(wizardDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.id").value(wizardId))
                .andExpect(jsonPath("$.data.name").value(wizardDto.name()))
                .andExpect(jsonPath("$.data.numberOfArtifacts").value(1));
    }

    @Test
    void testUpdateWizardErrorWithNonExistentWizardId() throws Exception {
        var invalidId = 5;

        var wizardDto = new WizardDto(null,
                "Nevile Longbottom Updated",
                null);

        var wizardDtoJson = objectMapper.writeValueAsString(wizardDto);

        this.mockMvc.perform(put(BASE_URL + "/wizards/" + invalidId).header("Authorization", this.token)
                        .contentType(MediaType.APPLICATION_JSON).content(wizardDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find %s with Id %d".formatted("wizard", invalidId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testUpdateWizardErrorWhenInvalidTokenIdProvided() throws Exception {
        var wizardId = 3;

        var wizardDto = new WizardDto(null,
                "Nevile Longbottom Updated",
                null);

        var wizardDtoJson = objectMapper.writeValueAsString(wizardDto);

        this.mockMvc.perform(put(BASE_URL + "/wizards/" + wizardId).header("Authorization", this.token + "invalid")
                        .contentType(MediaType.APPLICATION_JSON).content(wizardDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Access token provided is expired, revoked, malformed, or invalid for other reasons."))
                .andExpect(jsonPath("$.data").value("An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature"));
    }

    @Test
    void testDeleteWizardByIdSuccess() throws Exception {
        var wizardId = 3;

        this.mockMvc.perform(delete(BASE_URL + "/wizards/" + wizardId)
                        .header("Authorization", this.token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"));
    }

    @Test
    void testDeleteWizardByIdErrorWithNonExistentWizardId() throws Exception {
        var invalidId = 5;

        this.mockMvc.perform(delete(BASE_URL + "/wizards/" + invalidId)
                        .header("Authorization", this.token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find %s with Id %d".formatted("wizard" ,invalidId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteWizardByIdErrorWhenInvalidTokenIdProvided() throws Exception {
        var invalidId = 5;

        this.mockMvc.perform(delete(BASE_URL + "/wizards/" + invalidId)
                        .header("Authorization", this.token + "invalid")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value("Access token provided is expired, revoked, malformed, or invalid for other reasons."))
                .andExpect(jsonPath("$.data").value("An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature"));
    }


}
