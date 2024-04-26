package edu.tcu.cs.hogwartsartifactsonline.artifact;

import edu.tcu.cs.hogwartsartifactsonline.ServiceTestConfig;
import edu.tcu.cs.hogwartsartifactsonline.artifact.utils.IdWorker;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import edu.tcu.cs.hogwartsartifactsonline.wizard.Wizard;
import net.bytebuddy.implementation.bytecode.Throw;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static edu.tcu.cs.hogwartsartifactsonline.artifact.ArtifactUtils.generateArtifact;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ArtifactServiceTest extends ServiceTestConfig{

    @Mock
    ArtifactRepository artifactRepository;

    @Mock
    IdWorker idWorker;

    @InjectMocks
    ArtifactService artifactService;

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
    void testFindByIdSuccess() {
        // Given
        var a = generateArtifact("123456",
                "Invisibility Cloak",
                "An invisibility cloak is used to make the wearer invisible.",
                "ImageUrl" );

        var w = new Wizard();
        w.setId(2);
        w.setName("Harry Potter");

        a.setOwner(w);

        when(artifactRepository.findById(a.getId()))
                .thenReturn(Optional.of(a));

        // When
        var returnedArtifact = artifactService.findById(a.getId());

        // Then
        assertThat(returnedArtifact.getId()).isEqualTo(a.getId());
        assertThat(returnedArtifact.getName()).isEqualTo(a.getName());
        assertThat(returnedArtifact.getDescription()).isEqualTo(a.getDescription());
        assertThat(returnedArtifact.getImageUrl()).isEqualTo(a.getImageUrl());
        verify(artifactRepository, times(1)).findById(a.getId());
    }

    @Test
    void testFindByIdNotFound() {
        // Given
        var artifactId = "123456";
        when(artifactRepository.findById(Mockito.anyString()))
                .thenReturn(Optional.empty());

        // When - Then
        assertThatThrownBy(() -> artifactService.findById(artifactId))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find Artifact with Id %s".formatted(artifactId));
        verify(artifactRepository, times(1)).findById(artifactId);
    }

    @Test
    void testFindAllArtifactsSuccess() {
        // Given
        when(artifactRepository.findAll())
                .thenReturn(artifacts);

        // When
        var actualArtifacts = artifactService.findAll();

        // Then
        assertThat(actualArtifacts.size()).isEqualTo(this.artifacts.size());
        verify(artifactRepository, times(1)).findAll();
    }

    @Test
    void testSaveArtifactSuccess() {
        // Given
        var newArtifact = new Artifact();
        newArtifact.setName("Artifact 3");
        newArtifact.setDescription("Description...");
        newArtifact.setImageUrl("imageUrl...");

        when(idWorker.nextId())
                .thenReturn(123456L);

        when(artifactRepository.save(newArtifact))
                .thenReturn(newArtifact);

        // When
        var savedArtifact = this.artifactService.save(newArtifact);

        // Then
        assertThat(savedArtifact.getId()).isEqualTo("123456");
        assertThat(savedArtifact.getName()).isEqualTo(newArtifact.getName());
        assertThat(savedArtifact.getDescription()).isEqualTo(newArtifact.getDescription());
        assertThat(savedArtifact.getImageUrl()).isEqualTo(newArtifact.getImageUrl());
        verify(artifactRepository, times(1)).save(newArtifact);
    }

    @Test
    void testUpdateSuccess() {
        // Given
        var oldArtifact = generateArtifact("123456",
                "Invisibility Cloak",
                "An invisibility cloak is used to make the wearer invisible.",
                "ImageUrl" );

        var update = generateArtifact("123456",
                "Invisibility Cloak",
                "A new description",
                "ImageUrl" );

        when(artifactRepository.findById("123456"))
                .thenReturn(Optional.of(oldArtifact));

        when(artifactRepository.save(oldArtifact))
                .thenReturn(oldArtifact);

        // When
        var updatedArtifact = artifactService.update("123456", update);

        // Then
        assertThat(updatedArtifact.getId()).isEqualTo(oldArtifact.getId());
        assertThat(updatedArtifact.getDescription()).isEqualTo(update.getDescription());
        verify(artifactRepository, times(1)).findById(oldArtifact.getId());
        verify(artifactRepository, times(1)).save(oldArtifact);
    }

    @Test
    void testUpdateNotFound() {
        // Given
        var update = generateArtifact("123456",
                "Invisibility Cloak",
                "A new description",
                "ImageUrl" );

        when(artifactRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        // When - Then
        assertThatThrownBy(() -> artifactService.update("123456", update))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find Artifact with Id %s".formatted("123456"));
        verify(artifactRepository, times(1)).findById("123456");
        verify(artifactRepository, times(0)).save(any(Artifact.class));
    }

    @Test
    void testDeleteSuccess() {
        // Given
        var existingArtifact = generateArtifact("123456",
                "Invisibility Cloak",
                "A new description",
                "ImageUrl" );

        when(artifactRepository.findById("123456"))
                .thenReturn(Optional.of(existingArtifact));

        doNothing().when(artifactRepository).deleteById("123456");

        // When
        artifactService.delete("123456");

        // Then
        verify(artifactRepository, times(1)).findById("123456");
        verify(artifactRepository, times(1))
                .deleteById("123456");
    }

    @Test
    void testDeleteNotFound() {
        // Given
        when(artifactRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        // When - Then
        assertThatThrownBy(() -> artifactService.delete("123456"))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find Artifact with Id %s".formatted("123456"));
        verify(artifactRepository, times(1)).findById("123456");
        verify(artifactRepository, times(0)).deleteById("123456");
    }
}