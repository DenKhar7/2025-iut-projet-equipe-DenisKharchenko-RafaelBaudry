package iut.nantes.project.bff.service

import iut.nantes.project.bff.domain.PeopleDTO
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.server.ResponseStatusException

@Service
class PeopleClient(
    @param:Qualifier("peopleRestClient") private val restClient: RestClient
) {

    fun getPeopleById(id: Long): PeopleDTO {
        return restClient.get()
            .uri("/api/v1/peoples/{id}", id)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { _, response ->
                throw ResponseStatusException(response.statusCode, "Erreur client People")
            }
            .body(PeopleDTO::class.java)!!
    }


}