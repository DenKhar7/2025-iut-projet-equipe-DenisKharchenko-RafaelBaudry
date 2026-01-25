package iut.nantes.project.reservations.repository

import iut.nantes.project.reservations.entity.ReservationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface ReservationRepository : JpaRepository<ReservationEntity, UUID> {
    
    fun findByRoomId(roomId: Long): List<ReservationEntity>
    
    fun findByDayBetween(dayStart: LocalDate, dayEnd: LocalDate): List<ReservationEntity>
    
    fun findByRoomIdAndDayBetween(roomId: Long, dayStart: LocalDate, dayEnd: LocalDate): List<ReservationEntity>
    
    @Query("SELECT r FROM ReservationEntity r WHERE r.roomId = :roomId AND r.day = :day AND " +
           "((r.start < :end AND r.end > :start))")
    fun findConflictingReservations(
        roomId: Long, 
        day: LocalDate, 
        start: Int, 
        end: Int
    ): List<ReservationEntity>
    
    fun findByOwnerId(ownerId: Long): List<ReservationEntity>
    
    fun deleteByOwnerId(ownerId: Long)
}