package com.ce.sr.security.jwt;

import java.util.Date;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.ce.sr.services.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;
import io.jsonwebtoken.*;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class JwtUtils {

    @Value("${com.ce.sr.jwtSecret}")
    private String jwtSecret;
    @Value("${com.ce.sr.jwtExpirationMs}")
    private int jwtExpirationMs;
    @Value("${com.ce.sr.jwtCookieName}")
    private String jwtCookie;

    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        return ResponseCookie.from(jwtCookie, jwt).path("/").maxAge(86400).httpOnly(true)
        .build();
    }

    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookie, null).path("/").build();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            JwtUtils.log.error("Firma JWT inválida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            JwtUtils.log.error("JWT token inválido: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            JwtUtils.log.error("El token JWT ha expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            JwtUtils.log.error("El token JWT no está soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            JwtUtils.log.info("La cadena de texto de JWT claims está vacía: {}", e.getMessage());
        }
        return false;
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
}
