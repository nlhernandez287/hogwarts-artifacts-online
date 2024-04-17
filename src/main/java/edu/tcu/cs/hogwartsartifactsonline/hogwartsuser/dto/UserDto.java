package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto;

import jakarta.validation.constraints.NotEmpty;

public record UserDto(
        Integer id,

        @NotEmpty(message = "Username is required.")
        String username,
        boolean enabled,

        @NotEmpty(message = "Roles are required.")
        String roles
) {
}
