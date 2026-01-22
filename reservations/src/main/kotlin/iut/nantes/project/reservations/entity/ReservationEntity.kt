package iut.nantes.project.reservations.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "reservations")
data class ReservationEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(nullable = false)
    val ownerId: Long,
    
    @ElementCollection
    @CollectionTable(name = "reservation_peoples", joinColumns = [JoinColumn(name = "reservation_id")])
    @Column(name = "people_id")
    val peoples: List<Long> = emptyList(),
    
    @Column(nullable = false)
    val roomId: Long,
    
    @Column(nullable = false)
    val start: Int,
    
    @Column(name = "end_time", nullable = false)
    val end: Int,
    
    @Column(name = "reservation_day", nullable = false)
    val day: LocalDate
)