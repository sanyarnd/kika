package kika.configuration.security;

import kika.security.Oauth2SuccessHandler;
import kika.security.jwt.decode.JwtTokenAuthenticationFilter;
import kika.security.jwt.decode.JwtTokenAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@Configuration
@EnableWebSecurity
@Import(SecurityProblemSupport.class)
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    public static final String ACCESS_TOKEN_COOKIE_NAME = "X-Access-Token";
    public static final String ACCESS_TOKEN_HEADER_NAME = ACCESS_TOKEN_COOKIE_NAME;
    public static final String REFRESH_TOKEN_COOKIE_NAME = "X-Refresh-Token";

    private final SecurityProblemSupport problemSupport;
    private final Oauth2SuccessHandler oauth2SuccessHandler;
    private final JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter;
    private final JwtTokenAuthenticationProvider jwtTokenAuthenticationProvider;

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/login/refresh");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(jwtTokenAuthenticationProvider)
            .addFilterAt(jwtTokenAuthenticationFilter, BasicAuthenticationFilter.class)
            .exceptionHandling(c -> {
                c.authenticationEntryPoint(problemSupport);
                c.accessDeniedHandler(problemSupport);
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // disabled
            .httpBasic().disable()
            .requestCache().disable()
            .rememberMe().disable()
            .formLogin().disable()
            .logout().disable()
            .csrf().disable()
            .anonymous().disable()

            // enabled
            .headers().and()
            .cors().and()
            .oauth2Login(c -> {
                // OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
                c.authorizationEndpoint().baseUri("/api/oauth2/authorization");
                // OAuth2LoginAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI
                c.redirectionEndpoint().baseUri("/api/oauth2/code/*");

                c.successHandler(oauth2SuccessHandler);
            })

            .authorizeRequests(requests -> {
                requests.mvcMatchers(HttpMethod.OPTIONS).permitAll();
                requests.mvcMatchers("/api/oauth2/**", "/api/login/refresh").permitAll();
                requests.mvcMatchers("/api/**", "/swagger-ui/**").authenticated();
                requests.mvcMatchers("/actuator/**").authenticated(); // TODO: admin only
                requests.anyRequest().denyAll();
            });
    }
}
