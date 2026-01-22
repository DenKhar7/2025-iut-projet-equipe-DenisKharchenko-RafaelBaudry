package iut.nantes.project.reservations.domain

import java.time.LocalDate
import java.util.*

data class Reservation(
    val id: UUID? =null,
    val ownerId: Long,
    val peoples: List<Long>,
    val roomId: Long,
    val start: Int,
    val end: Int,
    val day: LocalDate
)

data class Peoples(
    val id: Long,
)