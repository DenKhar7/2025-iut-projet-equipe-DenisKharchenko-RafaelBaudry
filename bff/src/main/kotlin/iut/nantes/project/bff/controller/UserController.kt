package iut.nantes.project.bff.controller

import iut.nantes.project.bff.domain.PeopleWithReservationDTO
import iut.nantes.project.bff.domain.ReservationDetailDTO
import iut.nantes.project.bff.domain.UserDTO
import iut.nantes.project.bff.service.CompletePeopleService
import iut.nantes.project.bff.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class BffController(
    private val userService: UserService,
    private val completePeopleService: CompletePeopleService
) {


    @PostMapping("/api/v1/user")
    fun createUser(@RequestBody dto: UserDTO): ResponseEntity<Void> {
        val isCreated = userService.createUser(dto.login, dto.password)

        return if (isCreated) {
            ResponseEntity.status(HttpStatus.CREATED).build()
        } else {
            ResponseEntity.status(429).build()
        }
    }

    @GetMapping("/peoples/{id}")
    fun getPeopleWithReservations(@PathVariable id: Long): ResponseEntity<PeopleWithReservationDTO> {
        val result = completePeopleService.getPeopleWithReservation(id)

        return ResponseEntity.ok(result)
    }

    @GetMapping("/reservations/{id}")
    fun getReservationDetail(@PathVariable id: UUID): ResponseEntity<ReservationDetailDTO> {
        val result = completePeopleService.getReservationDetail(id)
        return ResponseEntity.ok(result)
    }
}