package com.luv2code.springbootlibrary.config;

import com.okta.spring.boot.oauth.Okta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        // disable Cross-Site Request Forgery
        http.csrf().disable();

        // protect our endpoints at "/api/<type>/secure"
        http.authorizeRequests(configurer ->
                configurer
                        .antMatchers("/api/books/secure/**")
                        .authenticated())
                .oauth2ResourceServer()
                .jwt();

        // add CORS filters
        http.cors();

        // add content negotiation strategy
        http.setSharedObject(ContentNegotiationStrategy.class,
                                new HeaderContentNegotiationStrategy());

        // force okta to return a non-empty response body for 401 to make response user-friendly
        Okta.configureResourceServer401ResponseBody(http);

        return http.build();
    }
}
