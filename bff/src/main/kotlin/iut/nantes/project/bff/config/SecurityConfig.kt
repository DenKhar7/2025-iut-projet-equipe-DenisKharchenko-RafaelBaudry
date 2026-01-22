package iut.nantes.project.bff.config

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod

import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.provisioning.UserDetailsManager
import javax.sql.DataSource


@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            authorizeHttpRequests {
                authorize(HttpMethod.POST, "/**", hasRole("ADMIN"))
                authorize(HttpMethod.PUT, "/**", hasRole("ADMIN"))
                authorize(HttpMethod.DELETE, "/**", hasRole("ADMIN"))
                authorize(anyRequest, authenticated)
            }
            httpBasic { }
            formLogin { }
        }
        return http.build()
    }
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
    @Bean
    @ConditionalOnProperty(name = ["bff.security"], havingValue = "database", matchIfMissing = true)
    fun jdbcUserDetails(dataSource: DataSource): UserDetailsManager {
        return JdbcUserDetailsManager(dataSource)
    }

    @Bean
    @ConditionalOnProperty(name = ["bff.security"], havingValue = "database", matchIfMissing = true)
    fun databaseInitializer(manager: UserDetailsManager, passwordEncoder: PasswordEncoder): CommandLineRunner {
        return CommandLineRunner {
            if (!manager.userExists("ADMIN")) {
                val admin = User.withUsername("ADMIN")
                    .password(passwordEncoder.encode("ADMIN"))
                    .roles("ADMIN")
                    .build()
                manager.createUser(admin)
            }
        }
    }
}