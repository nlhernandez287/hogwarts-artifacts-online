package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto;

import jakarta.validation.constraints.NotEmpty;

public record UserSaveDto(Integer id,

                          @NotEmpty(message = "Username is required.")
                          String username,

                          @NotEmpty(message = "Password is required.")
                          String password,

                          boolean enable,

                          @NotEmpty(message = "Roles are required.")
                          String roles
) {
}