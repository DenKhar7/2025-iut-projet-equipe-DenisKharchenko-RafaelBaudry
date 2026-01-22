package iut.nantes.project.bff.service

import iut.nantes.project.bff.domain.ReservationDTO
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.util.UUID

@Service
class ReservationClient(
    private val reservationRestClient: RestClient
) {
    fun getReservationById(id: UUID): ReservationDTO {
        val username = SecurityContextHolder.getContext().authentication.name

        return reservationRestClient
            .get()
            .uri("/api/v1/reservations/{id}", id)
            .header("X-User", username)
            .retrieve()
            .body(ReservationDTO::class.java)!!
    }

    fun getReservationsByOwner(ownerId: Long): List<ReservationDTO> {
        val username = SecurityContextHolder.getContext().authentication.name

        return reservationRestClient.get()
            .uri("/api/v1/reservations/owner/{ownerId}", ownerId)
            .header("X-User", username)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<ReservationDTO>>() {})
            ?: emptyList()
    }
}