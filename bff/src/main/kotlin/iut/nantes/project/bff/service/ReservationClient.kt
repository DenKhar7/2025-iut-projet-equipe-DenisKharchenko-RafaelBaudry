package iut.nantes.project.bff.service

import iut.nantes.project.bff.domain.ReservationDTO
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class ReservationClient(
    @Qualifier("reservationRestClient") private val restClient: RestClient
) {
    fun getReservationById(id: UUID): ReservationDTO {
        return restClient.get()
            .uri("/api/v1/reservations/{id}", id)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { _, response ->
                throw ResponseStatusException(response.statusCode, "Erreur service Reservation")
            }
            .body(ReservationDTO::class.java)!!
    }

    fun getReservationsByOwner(ownerId: Long): List<ReservationDTO> {
        return restClient.get()
            .uri("/api/v1/reservations?ownerId={id}", ownerId)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<ReservationDTO>>() {})
            ?: emptyList()
    }
}