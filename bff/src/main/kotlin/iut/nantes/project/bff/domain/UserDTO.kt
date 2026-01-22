package iut.nantes.project.bff.domain

data class UserDTO (
    val login: String,
    val password: String,
    val isAdmin: Boolean = false
)