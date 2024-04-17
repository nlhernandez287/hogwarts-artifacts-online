package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.converter;

import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.HogwartsUser;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto.UserSaveDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserSaveDtoToUserConverter implements Converter<UserSaveDto, HogwartsUser> {
    @Override
    public HogwartsUser convert(UserSaveDto source) {
        var user = new HogwartsUser();
        user.setId(source.id());
        user.setUsername(source.username());
        user.setPassword(source.password());
        user.setEnable(source.enable());
        user.setRoles(source.roles());
        return user;
    }
}
