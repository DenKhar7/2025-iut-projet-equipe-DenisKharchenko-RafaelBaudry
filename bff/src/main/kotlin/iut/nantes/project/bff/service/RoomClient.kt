package iut.nantes.project.bff.service

import iut.nantes.project.bff.config.ClientProperties
import iut.nantes.project.bff.domain.RoomSummary
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient


@Service
class RoomClient(
    private val restClientBuilder: RestClient.Builder,
    private val properties: ClientProperties
) {
    fun getRoomById(id: Long): RoomSummary {
        return restClientBuilder.baseUrl(properties.roomsUrl).build()
            .get()
            .uri("/api/v1/rooms/{id}", id)
            .retrieve()
            .body(RoomSummary::class.java)!!
    }
}