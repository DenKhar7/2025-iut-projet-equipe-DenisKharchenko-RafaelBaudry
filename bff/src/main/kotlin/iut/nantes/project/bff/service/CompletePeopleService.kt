package iut.nantes.project.bff.service

import iut.nantes.project.bff.domain.PeopleSummary
import iut.nantes.project.bff.domain.PeopleWithReservationDTO
import iut.nantes.project.bff.domain.ReservationDetailDTO
import iut.nantes.project.bff.domain.RoomSummary
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CompletePeopleService(
    private val peopleClient: PeopleClient,
    private val reservationClient: ReservationClient,
    private val roomClient: RoomClient
) {
    fun getReservationDetail(id: UUID): ReservationDetailDTO {

        val res = reservationClient.getReservationById(id)

        val ownerSummary = try {
            val p = peopleClient.getPeopleById(res.ownerId)
            PeopleSummary(p.id, p.firstName, p.lastName)
        } catch (_: Exception) {
            PeopleSummary(res.ownerId, "DELETED", "DELETED")
        }

        val participants = res.peoples.map { personId ->
            try {
                val p = peopleClient.getPeopleById(personId)
                PeopleSummary(p.id, p.firstName, p.lastName)
            } catch (_: Exception) {
                PeopleSummary(personId, "DELETED", "DELETED")
            }
        }
        val roomSummary = try {
            roomClient.getRoomById(res.roomId)
        } catch (_: Exception) {
            RoomSummary(res.roomId, "Information salle indisponible")
        }

        return ReservationDetailDTO(
            id = res.id,
            owner = ownerSummary,
            peoples = participants,
            roomId = roomSummary,
            start = res.start,
            end = res.end,
            day = res.day
        )
    }

    fun getPeopleWithReservation(id: Long): PeopleWithReservationDTO {
        val p = peopleClient.getPeopleById(id)

        val reservationsDtos = reservationClient.getReservationsByOwner(id)

        return PeopleWithReservationDTO(
            id = p.id,
            firstName = p.firstName,
            lastName = p.lastName,
            age = p.age,
            address = p.address,
            reservations = reservationsDtos.map { it.id.toString() }
        )
    }
}