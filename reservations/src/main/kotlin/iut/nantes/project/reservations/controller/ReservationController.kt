package iut.nantes.project.reservations.controller

import iut.nantes.project.reservations.domain.Reservation
import iut.nantes.project.reservations.exception.InvalidReservationException
import iut.nantes.project.reservations.exception.ReservationNotFoundException
import iut.nantes.project.reservations.service.ReservationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(private val service: ReservationService) {
    
    @PostMapping
    fun createReservation(@Valid @RequestBody reservation: Reservation): ResponseEntity<Reservation> {
        // Validation supp
        if (reservation.end <= reservation.start) {
            throw InvalidReservationException("Dernier heure doit etre superieur a l'heure de debut")
        }
        if (reservation.day.isBefore(LocalDate.now())) {
            throw InvalidReservationException("La date de réservation ne peut pas être dans le passé")
        }
        
        val created = service.createReservation(reservation)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }
    
    @GetMapping
    fun getAllReservations(
        @RequestParam(required = false) roomId: Long?,
        @RequestParam(required = false) dayStart: LocalDate?,
        @RequestParam(required = false) dayEnd: LocalDate?
    ): ResponseEntity<List<Reservation>> {
        // Validation supp
        if (dayStart != null && dayEnd != null && dayStart.isAfter(dayEnd)) {
            throw InvalidReservationException("dayStart doit être avant ou égal à dayEnd")
        }
        
        val reservations = service.getAllReservations(roomId, dayStart, dayEnd)
        return ResponseEntity.ok(reservations)
    }
    
    @GetMapping("/{id}")
    fun getReservationById(@PathVariable id: UUID): ResponseEntity<Reservation> {
        val reservation = service.getReservationById(id)
            ?: throw ReservationNotFoundException("Reservation not found")
        return ResponseEntity.ok(reservation)
    }
    
    @PutMapping("/{id}")
    fun updateReservation(
        @PathVariable id: UUID,
        @Valid @RequestBody reservation: Reservation
    ): ResponseEntity<Reservation> {
        // Additional validation
        if (reservation.end <= reservation.start) {
            throw InvalidReservationException("Dernier heure doit etre superieur a l'heure de debut")
        }
        
        val updated = service.updateReservation(id, reservation)
            ?: throw ReservationNotFoundException("Reservation not Found")
        return ResponseEntity.ok(updated)
    }
    
    @DeleteMapping("/{id}")
    fun deleteReservation(@PathVariable id: UUID): ResponseEntity<Void> {
        val deleted = service.deleteReservation(id)
        if (!deleted) {
            throw ReservationNotFoundException("Reservation not Found")
        }
        return ResponseEntity.noContent().build()
    }

    // BFF
    @GetMapping("/owner/{ownerId}")
    fun getAllReservationsByOwnerId(@PathVariable ownerId: Long): ResponseEntity<List<Reservation>> {
        val reservations = service.getAllReservationsByOwnerId(ownerId)
        return ResponseEntity.ok(reservations)
    }
    
    @DeleteMapping("/owner/{ownerId}")
    fun deleteReservationsByOwnerId(@PathVariable ownerId: Long): ResponseEntity<Void> {
        service.deleteReservationsByOwnerId(ownerId)
        return ResponseEntity.noContent().build()
    }
}
