package com.rednet.accountservice.util.impl;

import com.rednet.accountservice.config.ApiTokenConfig;
import com.rednet.accountservice.config.RolesEnum;
import com.rednet.accountservice.exception.InvalidTokenException;
import com.rednet.accountservice.model.SystemTokenClaims;
import com.rednet.accountservice.util.ApiTokenParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.jsonwebtoken.io.Decoders.BASE64;

@Component
public class ApiTokenParserImpl implements ApiTokenParser {
    private final ApiTokenConfig config;
    private final JwtParser jwtParser;

    public ApiTokenParserImpl(ApiTokenConfig config) {
        this.config = config;
        jwtParser = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(BASE64.decode(config.getSecretKey())))
                .requireIssuer(config.getIssuer())
                .setAllowedClockSkewSeconds(config.getAllowedClockSkew())
                .build();
    }

    @Override
    public SystemTokenClaims parseApiToken(String token) {
        try {
            Claims claimsMap = extractClaimsMapFrom(token);
            String subjectID = extractSubjectIDFrom(claimsMap);
            String sessionID = extractSessionIDFrom(claimsMap);
            String tokenID = extractTokenIDFrom(claimsMap);
            List<RolesEnum> roles = extractRolesFrom(claimsMap);

            return new SystemTokenClaims(subjectID, sessionID, tokenID, roles);
        } catch (SignatureException |
                 MalformedJwtException |
                 ExpiredJwtException |
                 UnsupportedJwtException |
                 IllegalArgumentException |
                 ClassCastException exception) {
            throw new InvalidTokenException(config.getTokenTypeName());
        }
    }

    private String extractSessionIDFrom(Claims claimsMap) {
        return claimsMap.get("sid", String.class);
    }

    private String extractSubjectIDFrom(Claims claimsMap) {
        return claimsMap.getSubject();
    }

    private List<RolesEnum> extractRolesFrom(Claims claimsMap) {
        List<?> convertedRoles = claimsMap.get("roles", ArrayList.class);

        return convertedRoles.stream()
                .map(String::valueOf)
                .map(RolesEnum::valueOf)
                .toList();
    }

    private String extractTokenIDFrom(Claims claimsMap) {
        return claimsMap.getId();
    }

    private Claims extractClaimsMapFrom(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
    }
}
