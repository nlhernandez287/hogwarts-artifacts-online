package edu.tcu.cs.hogwartsartifactsonline.wizard.dto;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Component;


public record WizardDto(Integer id,
                        String name,
                        Integer numberOfArtifacts) {
}
