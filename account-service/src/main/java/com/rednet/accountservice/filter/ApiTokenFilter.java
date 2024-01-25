package com.rednet.accountservice.filter;

import com.rednet.accountservice.config.ApiTokenConfig;
import com.rednet.accountservice.config.RolesEnum;
import com.rednet.accountservice.exception.InvalidTokenException;
import com.rednet.accountservice.model.SystemTokenClaims;
import com.rednet.accountservice.util.ApiTokenParser;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class ApiTokenFilter extends OncePerRequestFilter {
    private final ApiTokenConfig apiTokenConfig;
    private final ApiTokenParser apiTokenParser;

    public ApiTokenFilter(ApiTokenConfig apiTokenConfig,
                          ApiTokenParser apiTokenParser) {
        this.apiTokenConfig = apiTokenConfig;
        this.apiTokenParser = apiTokenParser;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        Optional<String> apiToken = extractApiTokenFromRequest(request);

        if (apiToken.isEmpty()) {
            filterChain.doFilter(request,response);
            return;
        }

        try {
            SystemTokenClaims claims = apiTokenParser.parseApiToken(apiToken.get());
            List<SimpleGrantedAuthority> authorities = convertRolesToAuthorities(claims.getRoles());

            UsernamePasswordAuthenticationToken contextAuthToken = new UsernamePasswordAuthenticationToken(
                    claims.getSubjectID(),
                    apiToken.get(),
                    authorities
            );

            contextAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(contextAuthToken);
        } catch (InvalidTokenException exception) {
            /*
             * LOG EVENT
             */
        }

        filterChain.doFilter(request,response);
    }

    private Optional<String> extractApiTokenFromRequest(HttpServletRequest request) {
        Optional<Cookie> cookie = Optional.ofNullable(WebUtils.getCookie(request, apiTokenConfig.getCookieName()));
        return cookie.map(Cookie::getValue);
    }

    private List<SimpleGrantedAuthority> convertRolesToAuthorities(List<RolesEnum> roles) {
        return roles.stream()
                .map(RolesEnum::name)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
