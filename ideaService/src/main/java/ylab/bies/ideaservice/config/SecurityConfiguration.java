package ylab.bies.ideaservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .antMatchers(
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/swagger-ui/**"
                        ).permitAll()
                        .anyRequest().permitAll())
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
/*                .and()
                .oauth2ResourceServer().jwt();*/
        return http.build();
    }
}
