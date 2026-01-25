package iut.nantes.project.bff.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.UserDetailsManager
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserServiceTest {

    private lateinit var userDetailsManager: UserDetailsManager
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var service: UserService

    @BeforeEach
    fun setup() {
        userDetailsManager = mockk(relaxed = true)
        passwordEncoder = mockk(relaxed = true)
        service = UserService(userDetailsManager, passwordEncoder)
    }

    /**
     * Test de creation dun utilisateur normal
     * Verifie que le UserDetailsManager est appele avec le bon user
     * et que le mot de passe est bien encode avant creation
     */
    @Test
    fun `test creation utilisateur normal fonctionne`() {
        every { userDetailsManager.userExists("john") } returns false
        every { passwordEncoder.encode("password123") } returns "encoded_password"
        every { userDetailsManager.createUser(any()) } returns Unit

        val created = service.createUser("john", "password123", false)

        assertTrue(created)
        verify { userDetailsManager.createUser(any()) }
    }

    @Test
    fun `test creation utilisateur admin donne le bon role`() {
        every { userDetailsManager.userExists("boss") } returns false
        every { passwordEncoder.encode("superpass") } returns "encoded_pass"
        every { userDetailsManager.createUser(any()) } returns Unit

        val created = service.createUser("boss", "superpass", true)

        assertTrue(created)
        verify { 
            userDetailsManager.createUser(match { user ->
                user.authorities.any { it.authority == "ROLE_ADMIN" }
            })
        }
    }

    @Test
    fun `test creation utilisateur existant renvoie false`() {
        every { userDetailsManager.userExists("duplicate") } returns true

        val created = service.createUser("duplicate", "pass", false)

        assertEquals(false, created)
        verify(exactly = 0) { userDetailsManager.createUser(any()) }
    }

    @Test
    fun `test mot de passe est bien encode`() {
        every { userDetailsManager.userExists("alice") } returns false
        every { passwordEncoder.encode("plaintext") } returns "hashed_value"
        every { userDetailsManager.createUser(any()) } returns Unit

        service.createUser("alice", "plaintext", false)

        verify { passwordEncoder.encode("plaintext") }
        verify {
            userDetailsManager.createUser(match { user ->
                user.password == "hashed_value"
            })
        }
    }

    @Test
    fun `test utilisateur normal a le role USER`() {
        every { userDetailsManager.userExists("normaluser") } returns false
        every { passwordEncoder.encode(any()) } returns "encoded"
        every { userDetailsManager.createUser(any()) } returns Unit

        service.createUser("normaluser", "pass", false)

        verify {
            userDetailsManager.createUser(match { user ->
                user.authorities.any { it.authority == "ROLE_USER" }
            })
        }
    }

    @Test
    fun `test creation plusieurs utilisateurs successifs`() {
        every { userDetailsManager.userExists(any()) } returns false
        every { passwordEncoder.encode(any()) } returns "encoded"
        every { userDetailsManager.createUser(any()) } returns Unit

        val result1 = service.createUser("user1", "pass1", false)
        val result2 = service.createUser("user2", "pass2", true)
        val result3 = service.createUser("user3", "pass3", false)

        assertTrue(result1)
        assertTrue(result2)
        assertTrue(result3)
        verify(exactly = 3) { userDetailsManager.createUser(any()) }
    }

    @Test
    fun `test nom utilisateur est conserve correctement`() {
        val username = "marc.lefebvre"
        every { userDetailsManager.userExists(username) } returns false
        every { passwordEncoder.encode(any()) } returns "encoded"
        every { userDetailsManager.createUser(any()) } returns Unit

        service.createUser(username, "password", false)

        verify {
            userDetailsManager.createUser(match { user ->
                user.username == username
            })
        }
    }
}
