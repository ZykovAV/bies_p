package ylab.bies.ideaservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {


    private final JwtAuthConverter jwtAuthConverter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .antMatchers(
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/swagger-ui/**"
                        ).permitAll()
                        .antMatchers(HttpMethod.GET, "/api/v1/ideas").authenticated()
                        .antMatchers(HttpMethod.POST, "/api/v1/ideas/draft").authenticated()
                        .antMatchers(HttpMethod.GET, "/api/v1/ideas/{id}").authenticated()
                        .antMatchers(HttpMethod.PUT, "/api/v1/ideas/{id}").authenticated()
                        .antMatchers(HttpMethod.PATCH, "/api/v1/ideas/{id}/like").authenticated()
                        .antMatchers(HttpMethod.PATCH, "/api/v1/ideas/{id}/dislike").authenticated()
                        .antMatchers(HttpMethod.GET, "/api/v1/ideas/drafts").authenticated()
                        .antMatchers(HttpMethod.PATCH, "/api/v1/ideas/{id}/status").hasRole("EXPERT")
                        .anyRequest().denyAll())
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .oauth2ResourceServer().jwt()
                .jwtAuthenticationConverter(jwtAuthConverter);
        return http.build();
    }
}
