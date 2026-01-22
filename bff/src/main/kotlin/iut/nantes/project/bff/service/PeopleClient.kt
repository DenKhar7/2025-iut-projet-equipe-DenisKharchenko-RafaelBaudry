package iut.nantes.project.bff.service

import iut.nantes.project.bff.domain.PeopleDTO
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.server.ResponseStatusException

@Service
class PeopleClient(
    private val peopleRestClient: RestClient
) {

    fun getPeopleById(id: Long): PeopleDTO {
        val username = SecurityContextHolder.getContext().authentication.name

        return peopleRestClient.get()
            .uri("/api/v1/peoples/{id}", id)
            .header("X-User", username)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { _, response ->
                throw ResponseStatusException(response.statusCode, "Erreur client People")
            }
            .body(PeopleDTO::class.java)!!
    }


}