package com.obel.miniurl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Autowired
	UserService userDetailsService;
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {

        http
        	.authorizeRequests()
                .antMatchers(HttpMethod.GET, "/*/redirect").permitAll()
                .anyRequest().authenticated()
                .and()
            .httpBasic();
        
        http.csrf().disable();
        
        http.authenticationProvider(authenticationProvider());
    }
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
	    DaoAuthenticationProvider authProvider
	      = new DaoAuthenticationProvider();
	    authProvider.setUserDetailsService(userDetailsService);
	    authProvider.setPasswordEncoder(passwordEncoder());
	    
	    return authProvider;
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
}
