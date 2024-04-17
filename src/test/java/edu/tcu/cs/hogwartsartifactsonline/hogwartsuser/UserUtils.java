package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

public class UserUtils {

    protected static HogwartsUser createUser(Integer id,
                                             String username,
                                             String password,
                                             boolean enable,
                                             String roles) {
        var newUser = new HogwartsUser();
        newUser.setId(id);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEnable(enable);
        newUser.setRoles(roles);
        return newUser;
    }
}
