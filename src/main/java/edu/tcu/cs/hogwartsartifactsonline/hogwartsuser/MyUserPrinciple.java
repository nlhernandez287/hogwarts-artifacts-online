package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class MyUserPrinciple implements UserDetails {

    private final HogwartsUser hogwartsUser;

    public MyUserPrinciple(HogwartsUser hogwartsUser) {
        this.hogwartsUser = hogwartsUser;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return Arrays.stream(StringUtils.tokenizeToStringArray(this.hogwartsUser.getRoles(), " "))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.hogwartsUser.getPassword();
    }

    @Override
    public String getUsername() {
        return this.hogwartsUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.hogwartsUser.isEnable();
    }

    public HogwartsUser getHogwartsUser() {
        return hogwartsUser;
    }
}
