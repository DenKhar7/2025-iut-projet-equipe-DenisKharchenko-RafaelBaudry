package iut.nantes.project.bff.service

import iut.nantes.project.bff.domain.PeopleSummary
import iut.nantes.project.bff.domain.PeopleWithReservationDTO
import iut.nantes.project.bff.domain.ReservationDetailDTO
import iut.nantes.project.bff.domain.RoomSummary
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AggregationService(
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

        val people = peopleClient.getPeopleById(id)

        val reservations = reservationClient.getReservationsByOwner(id)

        return PeopleWithReservationDTO(
            id = people.id,
            firstName = people.firstName,
            lastName = people.lastName,
            age = people.age,
            address = people.address,
            reservations = reservations.map { it.id.toString() }
        )
    }
}