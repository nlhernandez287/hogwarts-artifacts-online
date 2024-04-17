package edu.tcu.cs.hogwartsartifactsonline.wizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.hogwartsartifactsonline.artifact.Artifact;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import edu.tcu.cs.hogwartsartifactsonline.wizard.dto.WizardDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class WizardControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    WizardService wizardService;

    List<Wizard> wizards;

    @BeforeEach
    void setUp() throws Exception {
        Artifact a1 = new Artifact();
        a1.setId("1250808601744904191");
        a1.setName("Deluminator");
        a1.setDescription("A Deluminator is a device invented by Albus Dumbledore");
        a1.setImageUrl("ImageUrl");

        Artifact a2 = new Artifact();
        a2.setId("1250808601744904192");
        a2.setName("Invisibility Cloak");
        a2.setDescription("An invisibility cloak is used to make th wearer invisible");
        a2.setImageUrl("ImageUrl");

        Artifact a3 = new Artifact();
        a3.setId("1250808601744904193");
        a3.setName("Elder Wand");
        a3.setDescription("The Elder Wand, known throughout history as the Death");
        a3.setImageUrl("ImageUrl");

        Artifact a4 = new Artifact();
        a4.setId("1250808601744904194");
        a4.setName("The Marauder's Map");
        a4.setDescription("A magical map of Hogwarts created by Remus Lupin");
        a4.setImageUrl("ImageUrl");

        Artifact a5 = new Artifact();
        a5.setId("1250808601744904195");
        a5.setName("The Sword of Gryffindor");
        a5.setDescription("A goblin-made sword adorned with large rubies on the hilt.");
        a5.setImageUrl("ImageUrl");

        Artifact a6 = new Artifact();
        a4.setId("1250808601744904196");
        a4.setName("Resurrection Stone");
        a4.setDescription("The Resurrection Stone allows the holder to bring back a person from the dead");
        a4.setImageUrl("ImageUrl");

        this.wizards = new ArrayList<>();

        Wizard w1 = new Wizard();
        w1.setId(1);
        w1.setName("Albus Dumbledore");
        w1.addArtifact(a1);
        w1.addArtifact(a3);
        this.wizards.add(w1);

        Wizard w2 = new Wizard();
        w2.setId(2);
        w2.setName("Harry Potter");
        w2.addArtifact(a2);
        w2.addArtifact(a4);
        this.wizards.add(w2);

        Wizard w3 = new Wizard();
        w3.setId(3);
        w3.setName("Neville Longbottom");
        w3.addArtifact(a5);
        this.wizards.add(w3);


    }

    @AfterEach
    void tearDown(){
    }

    @Test
    void testFindAllWizardsSuccess() throws Exception {
        // Given. Arrange inputs and targets. Define the behavior of Mock objects
        given(this.wizardService.findAll()).willReturn(this.wizards);

        // When and then
        this.mockMvc.perform(get("/api/v1/wizards").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find one success"))
                .andExpect(jsonPath("$.data.id").value(wizard.getId()))
                .andExpect(jsonPath("$.data.name").value(wizard.getName()))
                .andExpect(jsonPath("$.data.numberOfArtifacts").value(0));
    }

    @Test
    void testFindWizardByIdSuccess() throws Exception {
        // Given. Arrange inputs and targets. Define the behavior of Mock objects
        given(this.wizardService.findById(1)).willReturn(this.wizards.get(1));

        this.mockMvc.perform(get("/api/v1/wizards").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find one success"))
                .andExpect(jsonPath("$.data.id").value(wizard.getId()))
                .andExpect(jsonPath("$.data.name").value(wizard.getName()))
                .andExpect(jsonPath("$.data.numberOfArtifacts").value(0));
    }

    @Test
    void testFindWizardByIdNotFound() throws Exception {
        // Given
        var wizardId = 1;
        when(this.wizardService.findById(wizardId))
                .thenThrow(new ObjectNotFoundException(Wizard.class.getSimpleName(), wizardId));

        // When - Then
        this.mockMvc.perform(
                        get("/api/v1/wizards").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not found Wizard with Id %d".formatted(wizardId)))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAddWizardSuccess() throws Exception {
        WizardDto wizardDto = new WizardDto(null, "Hermoine Granger", 0);

        String json = this.objectMapper.writeValueAsString(wizardDto);

        Wizard savedWizard = new Wizard();
        savedWizard.setId(4);
        savedWizard.setName("Hermoine Granger");

        // Given
        //given(this.wizardService.save(Mockito.any(Wizard.class))).willReturn
    }

    @Test
    void testUpdateWizardSuccess() throws Exception{
        WizardDto wizardDto = new WizardDto(null, "Updated wizard name", 0);

        Wizard updatedWizard = new Wizard();
        updatedWizard.setId(1);
        updatedWizard.setName("Updated wizard name");

        String json = this.objectMapper.writeValueAsString(updatedWizard);

        // Given
        given(this.wizardService.update(eq(1), Mockito.any(Wizard.class)));
    }

}