package iut.nantes.project.bff.domain

import java.time.LocalDate
import java.util.UUID

data class ReservationDetailDTO(
    val id: UUID,
    val owner: PeopleSummary,
    val peoples: List<PeopleSummary>,
    val roomId: RoomSummary,
    val start: Int,
    val end: Int,
    val day: LocalDate
)


data class PeopleSummary(
    val id: Long,
    val firstName: String,
    val lastName: String
)

data class RoomSummary(
    val id: Long,
    val name: String
)