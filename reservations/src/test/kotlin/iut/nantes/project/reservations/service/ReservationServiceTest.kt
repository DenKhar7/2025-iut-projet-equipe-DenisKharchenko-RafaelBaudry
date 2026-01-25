package iut.nantes.project.reservations.service

import iut.nantes.project.reservations.domain.Reservation
import iut.nantes.project.reservations.entity.ReservationEntity
import iut.nantes.project.reservations.exception.InvalidReservationException
import iut.nantes.project.reservations.exception.ReservationConflictException
import iut.nantes.project.reservations.repository.ReservationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ReservationServiceTest {

    private lateinit var repository: ReservationRepository
    private lateinit var webClient: WebClient
    private lateinit var service: ReservationService

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        webClient = mockk(relaxed = true)
        service = ReservationService(repository, webClient)
        
        // on clean le context pour eviter les pb de threads
        RequestContextHolder.resetRequestAttributes()
    }

    /**
     * Test qui verifie la creation dune reservation valide
     * On doit verifier lexistence des personnes via WebClient
     * et sassurer quil ny a pas de conflit dhoraires avant de sauvegarder
     */
    @Test
    fun `test creation reservation valide fonctionne`() {
        val tomorrow = LocalDate.now().plusDays(1)
        val reservation = Reservation(
            null,
            ownerId = 1,
            peoples = listOf(1, 2),
            roomId = 10,
            start = 9,
            end = 10,
            day = tomorrow
        )

        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>(relaxed = true)
        val responseSpec = mockk<WebClient.ResponseSpec>(relaxed = true)
        
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any<Long>()) } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.header(any(), any()) } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.retrieve() } returns responseSpec
        every { responseSpec.toBodilessEntity() } returns Mono.just(ResponseEntity.ok().build())
        
        every { repository.findConflictingReservations(any(), any(), any(), any()) } returns emptyList()
        
        val savedEntity = ReservationEntity(
            UUID.randomUUID(), 1, listOf(1, 2), 10, 9, 10, tomorrow
        )
        every { repository.save(any()) } returns savedEntity

        val result = service.createReservation(reservation)

        assertNotNull(result.id)
        assertEquals(1, result.ownerId)
        assertEquals(2, result.peoples.size)
        
        verify { repository.save(any()) }
    }

    @Test
    fun `test creation reservation avec personne inexistante echoue`() {
        val tomorrow = LocalDate.now().plusDays(1)
        val reservation = Reservation(
            null,
            ownerId = 999,
            peoples = listOf(1),
            roomId = 10,
            start = 9,
            end = 10,
            day = tomorrow
        )

        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>(relaxed = true)
        val responseSpec = mockk<WebClient.ResponseSpec>(relaxed = true)
        
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any<Long>()) } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.header(any(), any()) } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.retrieve() } returns responseSpec
        every { responseSpec.toBodilessEntity() } returns Mono.just(ResponseEntity.status(404).build())

        assertThrows<InvalidReservationException> {
            service.createReservation(reservation)
        }
    }

    @Test
    fun `test creation reservation avec conflit echoue`() {
        val tomorrow = LocalDate.now().plusDays(1)
        val reservation = Reservation(
            null,
            ownerId = 1,
            peoples = listOf(1),
            roomId = 10,
            start = 9,
            end = 10,
            day = tomorrow
        )

        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>(relaxed = true)
        val responseSpec = mockk<WebClient.ResponseSpec>(relaxed = true)
        
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any<Long>()) } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.header(any(), any()) } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.retrieve() } returns responseSpec
        every { responseSpec.toBodilessEntity() } returns Mono.just(ResponseEntity.ok().build())

        val existing = ReservationEntity(
            UUID.randomUUID(), 2, listOf(2), 10, 9, 11, tomorrow
        )
        every { repository.findConflictingReservations(any(), any(), any(), any()) } returns listOf(existing)

        assertThrows<ReservationConflictException> {
            service.createReservation(reservation)
        }
    }

    @Test
    fun `test recherche toutes reservations sans filtre`() {
        val date1 = LocalDate.of(2025, 6, 15)
        val date2 = LocalDate.of(2025, 6, 16)
        
        val entities = listOf(
            ReservationEntity(UUID.randomUUID(), 1, listOf(1), 10, 9, 10, date1),
            ReservationEntity(UUID.randomUUID(), 2, listOf(2), 11, 14, 15, date1),
            ReservationEntity(UUID.randomUUID(), 1, listOf(1, 2), 10, 10, 11, date2)
        )
        
        every { repository.findAll() } returns entities

        val results = service.getAllReservations(null, null, null)

        assertEquals(3, results.size)
    }

    @Test
    fun `test recherche reservations par salle`() {
        val date = LocalDate.of(2025, 7, 1)
        val entities = listOf(
            ReservationEntity(UUID.randomUUID(), 1, listOf(1), 5, 9, 10, date),
            ReservationEntity(UUID.randomUUID(), 2, listOf(2), 5, 14, 15, date)
        )
        
        every { repository.findByRoomId(5) } returns entities

        val results = service.getAllReservations(5, null, null)

        assertEquals(2, results.size)
        assertTrue(results.all { it.roomId == 5L })
    }

    @Test
    fun `test recherche reservations par periode`() {
        val start = LocalDate.of(2025, 8, 1)
        val end = LocalDate.of(2025, 8, 10)
        
        val entities = listOf(
            ReservationEntity(UUID.randomUUID(), 1, listOf(1), 10, 9, 10, LocalDate.of(2025, 8, 5)),
            ReservationEntity(UUID.randomUUID(), 2, listOf(2), 11, 14, 15, LocalDate.of(2025, 8, 8))
        )
        
        every { repository.findByDayBetween(start, end) } returns entities

        val results = service.getAllReservations(null, start, end)

        assertEquals(2, results.size)
    }

    @Test
    fun `test mise a jour reservation change bien les infos`() {
        val id = UUID.randomUUID()
        val date = LocalDate.of(2025, 9, 15)
        val existing = ReservationEntity(id, 1, listOf(1), 10, 9, 10, date)
        
        every { repository.findById(id) } returns Optional.of(existing)
        
        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>(relaxed = true)
        val responseSpec = mockk<WebClient.ResponseSpec>(relaxed = true)
        
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>(), any<Long>()) } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.header(any(), any()) } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.retrieve() } returns responseSpec
        every { responseSpec.toBodilessEntity() } returns Mono.just(ResponseEntity.ok().build())
        
        every { repository.findConflictingReservations(any(), any(), any(), any()) } returns emptyList()
        
        every { repository.save(any()) } answers { firstArg() }

        val updated = Reservation(id, 1, listOf(1, 2, 3), 10, 11, 12, date)
        val result = service.updateReservation(id, updated)

        assertNotNull(result)
        assertEquals(3, result.peoples.size)
        assertEquals(1, result.ownerId)
        assertEquals(11, result.start)
        assertEquals(12, result.end)
    }

    @Test
    fun `test suppression reservation existante fonctionne`() {
        val id = UUID.randomUUID()
        every { repository.existsById(id) } returns true
        every { repository.deleteById(id) } returns Unit

        val deleted = service.deleteReservation(id)

        assertTrue(deleted)
        verify { repository.deleteById(id) }
    }

    @Test
    fun `test suppression reservation inexistante renvoie false`() {
        val id = UUID.randomUUID()
        every { repository.existsById(id) } returns false

        val deleted = service.deleteReservation(id)

        assertEquals(false, deleted)
    }

    @Test
    fun `test recherche reservations par owner id`() {
        val entities = listOf(
            ReservationEntity(UUID.randomUUID(), 5, listOf(5), 10, 9, 10, LocalDate.now()),
            ReservationEntity(UUID.randomUUID(), 5, listOf(5, 6), 11, 14, 15, LocalDate.now())
        )
        
        every { repository.findByOwnerId(5) } returns entities

        val results = service.getAllReservationsByOwnerId(5)

        assertEquals(2, results.size)
        assertTrue(results.all { it.ownerId == 5L })
    }

    @Test
    fun `test suppression reservations par owner id`() {
        every { repository.deleteByOwnerId(7) } returns Unit

        service.deleteReservationsByOwnerId(7)

        verify { repository.deleteByOwnerId(7) }
    }
}
