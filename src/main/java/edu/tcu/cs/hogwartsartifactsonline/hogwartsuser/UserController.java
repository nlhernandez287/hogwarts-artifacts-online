package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;


import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.converter.UserDtoToUserConverter;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.converter.UserSaveDtoToUserConverter;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.converter.UserToUserDtoConverter;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto.UserSaveDto;
import edu.tcu.cs.hogwartsartifactsonline.system.Result;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {

    private final UserService userService;

    private final UserToUserDtoConverter userToUserDtoConverter;

    private final UserSaveDtoToUserConverter userSaveDtoToUserConverter;

    private final UserDtoToUserConverter userDtoToUserConverter;

    public UserController(UserService userService, UserToUserDtoConverter userToUserDtoConverter, UserSaveDtoToUserConverter userSaveDtoToUserConverter, UserDtoToUserConverter userDtoToUserConverter) {
        this.userService = userService;
        this.userToUserDtoConverter = userToUserDtoConverter;
        this.userSaveDtoToUserConverter = userSaveDtoToUserConverter;
        this.userDtoToUserConverter = userDtoToUserConverter;
    }

    @PostMapping
    public Result saveUser(@Valid @RequestBody UserSaveDto userSaveDto) {
        var newUser = userSaveDtoToUserConverter.convert(userSaveDto);
        var savedUser = this.userService.save(newUser);
        var savedUserDto = userToUserDtoConverter.convert(savedUser);
        return new Result(true, StatusCode.SUCCESS, "Add success", savedUserDto);
    }

    @GetMapping("/{userId}")
    public Result findUserById(@PathVariable Integer userId) {
        var user = this.userService.findById(userId);
        var userDto = userToUserDtoConverter.convert(user);
        return new Result(true, StatusCode.SUCCESS, "Find one success", userDto);
    }

    @GetMapping
    public Result findAllUsers() {
        var userResponseDtoList = this.userService.findAll()
                .stream().map(this.userToUserDtoConverter::convert).toList();
        return new Result(true, StatusCode.SUCCESS, "Find all success", userResponseDtoList);
    }

    @PutMapping("/{userId}")
    public Result updateUser(@PathVariable Integer userId, @Valid @RequestBody UserDto userDto) {
        var update = userDtoToUserConverter.convert(userDto);
        var updatedUser = this.userService.update(userId, Objects.requireNonNull(update));
        var updatedUserDto = userToUserDtoConverter.convert(updatedUser);
        return new Result(true, StatusCode.SUCCESS, "Update success", updatedUserDto);
    }

    @DeleteMapping("/{userId}")
    public Result deleteUserById(@PathVariable Integer userId) {
        this.userService.delete(userId);
        return new Result(true, StatusCode.SUCCESS, "Delete success");
    }
}
