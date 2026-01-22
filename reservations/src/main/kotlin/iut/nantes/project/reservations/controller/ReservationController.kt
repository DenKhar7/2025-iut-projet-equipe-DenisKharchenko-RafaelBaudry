package iut.nantes.project.reservations.controller

import iut.nantes.project.reservations.domain.Reservation
import iut.nantes.project.reservations.service.ReservationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(private val service: ReservationService) {
    
    @PostMapping
    fun createReservation(@RequestBody reservation: Reservation): ResponseEntity<*> {
        return try {
            val created = service.createReservation(reservation)
            ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "timestamp" to ZonedDateTime.now(),
                    "status" to 400,
                    "message" to (e.message ?: "Invalid request")
                )
            )
        }
    }
    
    @GetMapping
    fun getAllReservations(
        @RequestParam(required = false) roomId: Long?,
        @RequestParam(required = false) dayStart: LocalDate?,
        @RequestParam(required = false) dayEnd: LocalDate?
    ): ResponseEntity<List<Reservation>> {
        val reservations = service.getAllReservations(roomId, dayStart, dayEnd)
        return ResponseEntity.ok(reservations)
    }
    
    @GetMapping("/{id}")
    fun getReservationById(@PathVariable id: UUID): ResponseEntity<*> {
        val reservation = service.getReservationById(id)
        return if (reservation != null) {
            ResponseEntity.ok(reservation)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "timestamp" to ZonedDateTime.now(),
                    "status" to 404,
                    "message" to "Reservation not found"
                )
            )
        }
    }
    
    @PutMapping("/{id}")
    fun updateReservation(
        @PathVariable id: UUID,
        @RequestBody reservation: Reservation
    ): ResponseEntity<*> {
        return try {
            val updated = service.updateReservation(id, reservation)
            if (updated != null) {
                ResponseEntity.ok(updated)
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    mapOf(
                        "timestamp" to ZonedDateTime.now(),
                        "status" to 404,
                        "message" to "Reservation not found"
                    )
                )
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                mapOf(
                    "timestamp" to ZonedDateTime.now(),
                    "status" to 400,
                    "message" to (e.message ?: "Invalid request")
                )
            )
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteReservation(@PathVariable id: UUID): ResponseEntity<*> {
        val deleted = service.deleteReservation(id)
        return if (deleted) {
            ResponseEntity.noContent().build<Void>()
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                mapOf(
                    "timestamp" to ZonedDateTime.now(),
                    "status" to 404,
                    "message" to "Reservation not found"
                )
            )
        }
    }

    // For the BFF service
    @GetMapping("/owner/{ownerId}")
    fun getAllReservationsByOwnerId(
        @RequestParam(required = false) ownerId: Long?,
    ): ResponseEntity<List<Reservation>> {
        val reservations = service.getAllReservationsByOwnerId(ownerId)
        return ResponseEntity.ok(reservations)
    }
}
