package contract.management.bea.CONTRACT_MANAGER.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.filter.OncePerRequestFilter;
import contract.management.bea.CONTRACT_MANAGER.config.LocaleConfig;

import contract.management.bea.CONTRACT_MANAGER.repository.UserRepository;
import contract.management.bea.CONTRACT_MANAGER.service.UserDetailsServiceimpl;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig1 {
    private final UserRepository userRepository;
    private final LocaleConfig localeConfig;

    public WebSecurityConfig1(UserRepository userRepository, LocaleConfig localeConfig) {
        this.userRepository = userRepository;
        this.localeConfig = localeConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/css/**", "/js/**", "/img/**", "/webfonts/**", "/webjars/**").permitAll()
                .requestMatchers("/", "/login").permitAll()
                .requestMatchers("/dashboard/**", "/analyse/**").authenticated()
                .requestMatchers("/contractsManager/**").hasAnyRole("VALIDATOR1","ADMIN")
                .requestMatchers("/track/**").hasAnyRole("VALIDATOR2","ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .usernameParameter("matricule")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .addFilterBefore(localeConfig.localeFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceimpl(userRepository);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService()); 
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
