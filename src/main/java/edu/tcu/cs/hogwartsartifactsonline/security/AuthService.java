package edu.tcu.cs.hogwartsartifactsonline.security;

import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.MyUserPrinciple;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.converter.UserToUserDtoConverter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final JWTProvider jwtProvider;

    private final UserToUserDtoConverter userToUserDtoConverter;

    public AuthService(JWTProvider jwtProvider, UserToUserDtoConverter userToUserDtoConverter) {
        this.jwtProvider = jwtProvider;
        this.userToUserDtoConverter = userToUserDtoConverter;
    }

    public Map<String, Object> createLoginInfo(Authentication authentication) {
        // Create user info
        var principal = (MyUserPrinciple) authentication.getPrincipal();
        var hogwartsUser = principal.getHogwartsUser();

        var hogwartsUserDto = this.userToUserDtoConverter.convert(hogwartsUser);

        // Create a JWT
        var token = this.jwtProvider.createToken(authentication);
        Map<String, Object> loginResultMap = new HashMap<>();

        loginResultMap.put("userInfo", hogwartsUserDto);
        loginResultMap.put("token", token);

        return loginResultMap;
    }
}