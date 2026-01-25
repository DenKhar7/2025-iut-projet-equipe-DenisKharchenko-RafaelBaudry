package iut.nantes.project.peoples.service

import iut.nantes.project.peoples.repository.Address
import iut.nantes.project.peoples.repository.People
import iut.nantes.project.peoples.repository.PeopleDatabase
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.reactive.function.client.WebClient

class PeopleService(
    private val database: PeopleDatabase,
    private val webClient: WebClient? = null,
    // URL du service de resa
    @Value("\${reservations.url:http://localhost:8082}") private val reservationsUrl: String = "http://localhost:8082" 
){

    private fun resolveAddress(address: Address): Address {
        val existing = database.findAddress(
            address.street, address.city, address.zipCode, address.country
        )
        return existing ?: address
    }

    fun createPeople(people: People): People {
        val peopleWithAddress = people.copy(address = resolveAddress(people.address))
        return database.save(peopleWithAddress)
    }

    fun updatePeople(id: Long, newInfo: People): People? {
        val existingPeople = database.findById(id) ?: return null

        val properAddress = resolveAddress(newInfo.address)

        val updatedPeople = existingPeople.copy(
            firstName = newInfo.firstName,
            lastName = newInfo.lastName,
            age = newInfo.age,
            address = properAddress
        )
        return database.save(updatedPeople)
    }

    fun deletePeople(id: Long): Boolean {
        // Delete resa par Peoples
        webClient?.let {
            try {
                it.delete()
                    .uri("$reservationsUrl/api/v1/reservations/owner/$id")
                    .retrieve()
                    .toBodilessEntity()
                    .block()
            } catch (e: Exception) {
                // Je laisse vide car on veut juste essayer de supprimer les résa meme si il n'existe pas
            }
        }
        
        return database.deleteById(id) > 0
    }
    fun findPeople(name: String?): List<People> {
        return database.findAll(name)
    }

    fun findPeopleById(id: Long): People? {
        return database.findById(id)
    }
}