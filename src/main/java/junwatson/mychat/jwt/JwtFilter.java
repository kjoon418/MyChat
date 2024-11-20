package junwatson.mychat.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends GenericFilterBean {

    private final TokenProvider tokenProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        log.info("JwtFilter.doFilter() called");

        String token = tokenProvider.resolveToken((HttpServletRequest) request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token) && tokenProvider.hasProperType(token, TokenType.ACCESS)) {
            Authentication authentication = tokenProvider.getAuthentication(token);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}