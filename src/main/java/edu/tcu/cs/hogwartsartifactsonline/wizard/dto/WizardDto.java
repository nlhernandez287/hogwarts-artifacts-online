package edu.tcu.cs.hogwartsartifactsonline.wizard.dto;

import jakarta.validation.constraints.NotEmpty;


public record WizardDto(Integer id,

                        @NotEmpty(message = "Name is required")
                        String name,
                        Integer numberOfArtifacts) {
}
