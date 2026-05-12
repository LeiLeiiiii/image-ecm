package com.sunyard.module.auth.oauth.dto;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;
import lombok.Setter;

/**
 * @author P-JWei
 * @date 2024/4/10 15:42:14
 * @title
 * @description
 */
@Getter
@Setter
public class CustomUser extends User {

    private String referer;
    private String publicKey;
    private String privateKey;

    public CustomUser(String username, String password,
                      Collection<? extends GrantedAuthority> authorities, String referer,
                      String publicKey, String privateKey) {
        super(username, password, authorities);
        this.referer = referer;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
}
