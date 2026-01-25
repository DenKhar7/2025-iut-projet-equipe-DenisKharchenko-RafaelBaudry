package iut.nantes.project.bff.controller

import iut.nantes.project.bff.domain.PeopleWithReservationDTO
import iut.nantes.project.bff.domain.ReservationDetailDTO
import iut.nantes.project.bff.domain.UserDTO
import iut.nantes.project.bff.service.AggregationService
import iut.nantes.project.bff.service.UserService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity
import java.util.UUID

@RestController
class BffController(
    private val userService: UserService,
    private val aggregationService: AggregationService,
    @param:Qualifier(value = "peopleRestClient") private val peopleClient: RestClient,
    @param:Qualifier(value = "reservationRestClient") private val reservationClient: RestClient
) {
    @PostMapping("/api/v1/user")
    fun createUser(@RequestBody dto: UserDTO): ResponseEntity<Void> {
        val isCreated = userService.createUser(dto.login, dto.password, dto.isAdmin)

        return if (isCreated) {
            ResponseEntity.status(HttpStatus.CREATED).build()
        } else {
            ResponseEntity.status(429).build()
        }
    }

    @GetMapping("/peoples/{id}")
    fun getPeopleWithReservations(@PathVariable id: Long): ResponseEntity<PeopleWithReservationDTO> {
        val result = aggregationService.getPeopleWithReservation(id)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/reservations/{id}")
    fun getReservationDetail(@PathVariable id: UUID): ResponseEntity<ReservationDetailDTO> {
        val result = aggregationService.getReservationDetail(id)
        return ResponseEntity.ok(result)
    }

    // Peoples
    @GetMapping("/api/v1/peoples")
    fun getAllPeoples(@RequestParam allParams: Map<String, String>) =
        proxyRequest(peopleClient, HttpMethod.GET, "/api/v1/peoples", params = allParams)

    @PostMapping("/api/v1/peoples")
    fun createPeople(@RequestBody body: Any) =
        proxyRequest(peopleClient, HttpMethod.POST, "/api/v1/peoples", body)

    @PutMapping("/api/v1/peoples/{id}")
    fun updatePeople(
        @PathVariable id: Long,
        @RequestBody body: Any
    ) =
        proxyRequest(peopleClient, HttpMethod.PUT, "/api/v1/peoples/$id", body)

    @DeleteMapping("/api/v1/peoples/{id}")
    fun deletePeople(@PathVariable id: Long) =
        proxyRequest(peopleClient, HttpMethod.DELETE, "/api/v1/peoples/$id")

    // Reservations
    @GetMapping("/api/v1/reservations")
    fun getAllReservations(@RequestParam allParams: Map<String, String>) =
        proxyRequest(reservationClient, HttpMethod.GET, "/api/v1/reservations", params = allParams)

    @PostMapping("/api/v1/reservations")
    fun createReservation(@RequestBody body: Any) =
        proxyRequest(reservationClient, HttpMethod.POST, "/api/v1/reservations", body)

    @PutMapping("/api/v1/reservations/{id}")
    fun updateReservation(@PathVariable id: UUID, @RequestBody body: Any) =
        proxyRequest(reservationClient, HttpMethod.PUT, "/api/v1/reservations/$id", body)

    @DeleteMapping("/api/v1/reservations/{id}")
    fun deleteReservation(@PathVariable id: UUID) =
        proxyRequest(reservationClient, HttpMethod.DELETE, "/api/v1/reservations/$id")


    private fun proxyRequest(
        client: RestClient,
        method: HttpMethod,
        path: String,
        body: Any? = null,
        params: Map<String, String> = emptyMap()
    ): ResponseEntity<Any> {

        var spec = client.method(method).uri { uriBuilder ->
            val builder = uriBuilder.path(path)
            params.forEach { (k, v) -> builder.queryParam(k, v) }
            builder.build()
        }

        if (body != null) {
            spec = spec.body(body)
        }

        return spec.retrieve().toEntity<Any>()
    }
}