package iut.nantes.project.reservations.service

import iut.nantes.project.reservations.domain.Reservation
import iut.nantes.project.reservations.entity.ReservationEntity
import iut.nantes.project.reservations.exception.InvalidReservationException
import iut.nantes.project.reservations.exception.ReservationConflictException
import iut.nantes.project.reservations.repository.ReservationRepository
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import java.util.*

@Service
class ReservationService(
    private val repository: ReservationRepository,
    private val webClient: WebClient
) {
    
    fun createReservation(reservation: Reservation): Reservation {
        // Validate peoples exist
        reservation.peoples.forEach { peopleId ->
            checkPeopleExists(peopleId)
        }
        checkPeopleExists(reservation.ownerId)
        
        // Validate room exists
        checkRoomExists(reservation.roomId)
        
        // Check for conflicts
        val conflicts = repository.findConflictingReservations(
            reservation.roomId,
            reservation.day,
            reservation.start,
            reservation.end
        )
        if (conflicts.isNotEmpty()) {
            throw ReservationConflictException("Room is already reserved for this time slot")
        }
        
        val entity = ReservationEntity(
            id = UUID.randomUUID(),
            ownerId = reservation.ownerId,
            peoples = reservation.peoples,
            roomId = reservation.roomId,
            start = reservation.start,
            end = reservation.end,
            day = reservation.day
        )
        
        val saved = repository.save(entity)
        return toReservation(saved)
    }
    
    fun getAllReservations(roomId: Long?, dayStart: LocalDate?, dayEnd: LocalDate?): List<Reservation> {
        val entities = when {
            roomId != null && dayStart != null && dayEnd != null -> 
                repository.findByRoomIdAndDayBetween(roomId, dayStart, dayEnd)
            roomId != null -> 
                repository.findByRoomId(roomId)
            dayStart != null && dayEnd != null -> 
                repository.findByDayBetween(dayStart, dayEnd)
            else -> 
                repository.findAll()
        }
        return entities.map { toReservation(it) }
    }
    
    fun getReservationById(id: UUID): Reservation? {
        val entity = repository.findById(id).orElse(null) ?: return null
        return toReservation(entity)
    }
    
    fun updateReservation(id: UUID, reservation: Reservation): Reservation? {
        val existing = repository.findById(id).orElse(null) ?: return null
        
        // Validate peoples exist
        reservation.peoples.forEach { peopleId ->
            checkPeopleExists(peopleId)
        }
        
        // Validate room exists
        checkRoomExists(reservation.roomId)
        
        // Check for conflicts (excluding current reservation)
        val conflicts = repository.findConflictingReservations(
            reservation.roomId,
            reservation.day,
            reservation.start,
            reservation.end
        ).filter { it.id != id }
        
        if (conflicts.isNotEmpty()) {
            throw ReservationConflictException("Room is already reserved for this time slot")
        }
        
        // Update without changing ownerId (preserve original owner)
        val updated = existing.copy(
            peoples = reservation.peoples,
            roomId = reservation.roomId,
            start = reservation.start,
            end = reservation.end,
            day = reservation.day
            // Note: ownerId is NOT updated - preserved from existing
        )
        
        val saved = repository.save(updated)
        return toReservation(saved)
    }
    
    fun deleteReservation(id: UUID): Boolean {
        if (!repository.existsById(id)) {
            return false
        }
        repository.deleteById(id)
        return true
    }
    
    private fun checkPeopleExists(peopleId: Long) {
        val exists = webClient.get()
            .uri("http://localhost:8081/api/v1/peoples/{id}", peopleId)
            .retrieve()
            .toBodilessEntity()
            .block()
            ?.statusCode?.is2xxSuccessful ?: false
        
        if (!exists) {
            throw InvalidReservationException("People with id $peopleId not found")
        }
    }
    
    private fun checkRoomExists(roomId: Long) {
        val exists = webClient.get()
            .uri("http://localhost:8083/api/v1/rooms/{id}", roomId)
            .retrieve()
            .toBodilessEntity()
            .block()
            ?.statusCode?.is2xxSuccessful ?: false
        
        if (!exists) {
            throw InvalidReservationException("Room with id $roomId not found")
        }
    }
    
    private fun toReservation(entity: ReservationEntity) = Reservation(
        id = entity.id,
        ownerId = entity.ownerId,
        peoples = entity.peoples,
        roomId = entity.roomId,
        start = entity.start,
        end = entity.end,
        day = entity.day
    )

    // For the BFF service:
    fun getAllReservationsByOwnerId(ownerId: Long?): List<Reservation> {
        val entities = when {
            ownerId != null ->
                repository.findByOwnerId(ownerId)
            else ->
                repository.findAll()
        }
        return entities.map { toReservation(it) }
    }
    
    fun deleteReservationsByOwnerId(ownerId: Long) {
        repository.deleteByOwnerId(ownerId)
    }
}
