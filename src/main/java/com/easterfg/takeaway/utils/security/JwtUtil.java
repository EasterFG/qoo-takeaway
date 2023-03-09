package com.easterfg.takeaway.utils.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author EasterFG on 2022/9/25
 * <p>
 * jwt 工具
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
@Slf4j
public class JwtUtil {

    private String secret;

    private String header;

    private Long expire;

    public String generateToken(Long id, String username, String name, String... role) {
        Date date = new Date();
        Date expireData = new Date(date.getTime() + 1000 * expire);
        // id, username, name
        return Jwts.builder()
                .setHeaderParam("typ", "jwt")
                .setSubject(username)
                .setIssuedAt(date)
                .claim("id", id)
                .claim("name", name)
                .claim("role", role)
                .setExpiration(expireData)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Claims getClaimsByToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
