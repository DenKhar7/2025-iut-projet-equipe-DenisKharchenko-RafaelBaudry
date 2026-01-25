package iut.nantes.project.bff.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDate
import java.util.UUID

data class PeopleWithReservationDTO(
    @JsonIgnore
    val id: Long,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val address: AddressDTO,
    val reservations: List<String>
)

data class AddressDTO(
    val street: String,
    val city: String,
    val zipCode: String,
    val country: String
)

data class ReservationDTO(
    val id: UUID,
    val ownerId: Long,
    val peoples: List<Long>,
    val roomId: Long,
    val start: Int,
    val end: Int,
    val day: LocalDate
)

data class PeopleDTO(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val address: AddressDTO
)