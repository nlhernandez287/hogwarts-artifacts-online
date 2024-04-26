package edu.tcu.cs.hogwartsartifactsonline.artifact;

import edu.tcu.cs.hogwartsartifactsonline.ControllerTestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.hogwartsartifactsonline.artifact.dto.ArtifactDto;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static edu.tcu.cs.hogwartsartifactsonline.artifact.ArtifactUtils.generateArtifact;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


class ArtifactControllerTest extends ControllerTestConfig{

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ArtifactService artifactService;

    @Value("${api.endpoint.base-url}/artifacts")
    String BASE_URL;

    @Autowired
    ObjectMapper objectMapper;

    List<Artifact> artifacts;

    @BeforeEach
    void setUp() {
        this.artifacts = Arrays.asList(
                generateArtifact(
                        "12345",
                        "Deluminator",
                        "A deluminator is a device invented by Albus Dumbledore.",
                        "imageUrl"
                ),
                generateArtifact(
                        "12346",
                        "Invisibility Cloak",
                        "An invibisibility cloak is used to make wearer invisible.",
                        "imageUrl"
                ),
                generateArtifact(
                        "12347",
                        "Elder Wand",
                        "The Elder Wand, known throughout history as the Deathstick.",
                        "imageUrl"
                )
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindArtifactByIdSuccess() throws Exception {
        // Given
        when(artifactService.findById("12345"))
                .thenReturn(this.artifacts.get(0));

        // When - Then
        var artifactIndex = 0;
        this.mockMvc.perform(
                        get(BASE_URL + "/12345").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find one success"))
                .andExpect(jsonPath("$.data.id").value(this.artifacts.get(artifactIndex).getId()))
                .andExpect(jsonPath("$.data.name").value(this.artifacts.get(artifactIndex).getName()))
                .andExpect(jsonPath("$.data.description").value(this.artifacts.get(artifactIndex).getDescription()))
                .andExpect(jsonPath("$.data.imageUrl").value(this.artifacts.get(artifactIndex).getImageUrl()));
    }

    @Test
    void testFindArtifactByIdNotFound() throws Exception {
        // Given
        when(artifactService.findById("12345"))
                .thenThrow(new ObjectNotFoundException(Artifact.class.getSimpleName() ,"12345"));

        // When - Then
        this.mockMvc.perform(
                        get(BASE_URL + "/12345").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find Artifact with Id 12345"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindAllArtifactsSuccess() throws Exception {
        // Given
        when(artifactService.findAll())
                .thenReturn(artifacts);

        // When - Then
        this.mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find all success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(this.artifacts.size())))
                .andExpect(jsonPath("$.data[0].id").value(this.artifacts.get(0).getId()))
                .andExpect(jsonPath("$.data[0].name").value(this.artifacts.get(0).getName()))
                .andExpect(jsonPath("$.data[0].description").value(this.artifacts.get(0).getDescription()))
                .andExpect(jsonPath("$.data[0].imageUrl").value(this.artifacts.get(0).getImageUrl()));
    }

    @Test
    void testSaveArtifactSuccess() throws Exception {
        // Given
        var artifactDto = new ArtifactDto(
                null,
                "Remembrall",
                "A Remembral was a magical large marble-sized glass ball",
                "imageUrl", null);

        var artifactDtoJson = objectMapper.writeValueAsString(artifactDto);

        var savedArtifact = new Artifact();
        savedArtifact.setId("123465");
        savedArtifact.setName("Remembrall");
        savedArtifact.setDescription("A Remembral was a magical large marble-sized glass ball");
        savedArtifact.setImageUrl("imageUrl...");

        when(artifactService.save(Mockito.any(Artifact.class)))
                .thenReturn(savedArtifact);

        // When - Then
        this.mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(artifactDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value(savedArtifact.getName()))
                .andExpect(jsonPath("$.data.description").value(savedArtifact.getDescription()))
                .andExpect(jsonPath("$.data.imageUrl").value(savedArtifact.getImageUrl()));
    }

    @Test
    void testUpdateArtifactSuccess() throws Exception {
        // Given
        var artifactDto = new ArtifactDto(
                "123456",
                "Invisibility Cloak",
                "A new description",
                "imageUrl", null);

        var artifactDtoJson = objectMapper.writeValueAsString(artifactDto);

        // When - Then
        var updatedArtifact = generateArtifact("123456",
                "Invisibility Cloak",
                "A new description",
                "ImageUrl" );

        when(artifactService.update(anyString() ,Mockito.any(Artifact.class)))
                .thenReturn(updatedArtifact);

        // When - Then
        this.mockMvc.perform(put(BASE_URL + "/123456").contentType(MediaType.APPLICATION_JSON).content(artifactDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.id").value(updatedArtifact.getId()))
                .andExpect(jsonPath("$.data.name").value(updatedArtifact.getName()))
                .andExpect(jsonPath("$.data.description").value(updatedArtifact.getDescription()))
                .andExpect(jsonPath("$.data.imageUrl").value(updatedArtifact.getImageUrl()));
    }

    @Test
    void testUpdateArtifactErrorWithNonExistenId() throws Exception {
        // Given
        var artifactDto = new ArtifactDto(
                "123456",
                "Invisibility Cloak",
                "A new description",
                "imageUrl", null);

        var artifactDtoJson = objectMapper.writeValueAsString(artifactDto);

        when(artifactService.update(anyString() ,Mockito.any(Artifact.class)))
                .thenThrow(new ObjectNotFoundException(Artifact.class.getSimpleName() ,"123456"));

        // When - Then
        this.mockMvc.perform(
                        put(BASE_URL + "/123456").contentType(MediaType.APPLICATION_JSON).content(artifactDtoJson).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find Artifact with Id 123456"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteArtifactSuccess() throws Exception {
        // Given
        var artifcatId = "12345";

        doNothing()
                .when(this.artifactService).delete(artifcatId);

        // When - Then
        this.mockMvc.perform(delete(BASE_URL + "/" + artifcatId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"));
    }

    @Test
    void testDeleteArtifactErrorWithNonExistenId() throws Exception {
        // Given
        var artifactId = "12345";

        doThrow(new ObjectNotFoundException(Artifact.class.getSimpleName() ,artifactId))
                .when(this.artifactService).delete(artifactId);

        // When - Then
        this.mockMvc.perform(
                        delete(BASE_URL + "/" + artifactId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find Artifact with Id " + artifactId))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}