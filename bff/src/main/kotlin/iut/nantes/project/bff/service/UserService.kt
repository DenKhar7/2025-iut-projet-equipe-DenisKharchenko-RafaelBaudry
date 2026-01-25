package iut.nantes.project.bff.service

import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userDetailsManager: UserDetailsManager,
    private val passwordEncoder: PasswordEncoder
) {

    fun createUser(username: String, rawPassword: String, isAdmin: Boolean = false): Boolean {
        if (userDetailsManager.userExists(username)) {
            return false
        }

        val role = if (isAdmin) "ADMIN" else "USER"
        
        val newUser = User.withUsername(username)
            .password(passwordEncoder.encode(rawPassword))
            .roles(role)
            .build()

        userDetailsManager.createUser(newUser)
        return true
    }
}