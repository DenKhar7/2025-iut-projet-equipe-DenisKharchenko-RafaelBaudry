package iut.nantes.project.reservations.controller

import com.fasterxml.jackson.databind.ObjectMapper
import iut.nantes.project.reservations.domain.Reservation
import iut.nantes.project.reservations.service.ReservationService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.util.*

@WebMvcTest(ReservationController::class)
@ActiveProfiles("test")
class ReservationControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun reservationService(): ReservationService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    /**
     * Test creation dune reservation avec donnees valides
     * Verifie que le controller accepte une reservation pour demain
     * et retourne un code 201 avec les donnees creees
     */
    @Test
    fun `test POST reservation valide renvoie 201`() {
        val tomorrow = LocalDate.now().plusDays(1)
        val reservation = Reservation(
            null,
            ownerId = 1,
            peoples = listOf(1, 2),
            roomId = 5,
            start = 14,
            end = 16,
            day = tomorrow
        )
        
        val created = reservation.copy(id = UUID.randomUUID())
        every { reservationService.createReservation(any()) } returns created

        mockMvc.perform(
            post("/api/v1/reservations")
                .header("X-User", "testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservation))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.ownerId").value(1))
            .andExpect(jsonPath("$.roomId").value(5))

        verify { reservationService.createReservation(any()) }
    }

    @Test
    fun `test POST reservation avec heure fin avant debut renvoie 400`() {
        val tomorrow = LocalDate.now().plusDays(1)
        val reservation = Reservation(
            null,
            ownerId = 1,
            peoples = listOf(1),
            roomId = 5,
            start = 16,
            end = 14,
            day = tomorrow
        )

        mockMvc.perform(
            post("/api/v1/reservations")
                .header("X-User", "testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservation))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test POST reservation dans le passe renvoie 400`() {
        val yesterday = LocalDate.now().minusDays(1)
        val reservation = Reservation(
            null,
            ownerId = 1,
            peoples = listOf(1),
            roomId = 5,
            start = 10,
            end = 11,
            day = yesterday
        )

        mockMvc.perform(
            post("/api/v1/reservations")
                .header("X-User", "testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservation))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test GET reservations renvoie toutes les reservations`() {
        val date = LocalDate.of(2025, 6, 15)
        val reservations = listOf(
            Reservation(UUID.randomUUID(), 1, listOf(1), 10, 9, 10, date),
            Reservation(UUID.randomUUID(), 2, listOf(2), 11, 14, 15, date),
            Reservation(UUID.randomUUID(), 1, listOf(1, 2), 12, 10, 11, date)
        )
        
        every { reservationService.getAllReservations(null, null, null) } returns reservations

        mockMvc.perform(
            get("/api/v1/reservations")
                .header("X-User", "testuser")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
    }

    @Test
    fun `test GET reservations avec filtre roomId`() {
        val date = LocalDate.of(2025, 7, 1)
        val reservations = listOf(
            Reservation(UUID.randomUUID(), 1, listOf(1), 5, 9, 10, date),
            Reservation(UUID.randomUUID(), 2, listOf(2), 5, 14, 15, date)
        )
        
        every { reservationService.getAllReservations(5, null, null) } returns reservations

        mockMvc.perform(
            get("/api/v1/reservations")
                .header("X-User", "testuser")
                .param("roomId", "5")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `test GET reservations avec dayStart apres dayEnd renvoie 400`() {
        mockMvc.perform(
            get("/api/v1/reservations")
                .header("X-User", "testuser")
                .param("dayStart", "2025-12-31")
                .param("dayEnd", "2025-12-01")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test GET reservation par id renvoie la bonne`() {
        val id = UUID.randomUUID()
        val date = LocalDate.of(2025, 8, 20)
        val reservation = Reservation(id, 3, listOf(3, 4), 8, 11, 12, date)
        
        every { reservationService.getReservationById(id) } returns reservation

        mockMvc.perform(
            get("/api/v1/reservations/$id")
                .header("X-User", "testuser")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ownerId").value(3))
            .andExpect(jsonPath("$.roomId").value(8))
    }

    @Test
    fun `test GET reservation inexistante renvoie 404`() {
        val id = UUID.randomUUID()
        every { reservationService.getReservationById(id) } returns null

        mockMvc.perform(
            get("/api/v1/reservations/$id")
                .header("X-User", "testuser")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `test PUT reservation met a jour correctement`() {
        val id = UUID.randomUUID()
        val date = LocalDate.of(2025, 9, 15)
        val reservation = Reservation(id, 1, listOf(1, 2, 3), 10, 15, 16, date)
        
        every { reservationService.updateReservation(id, any()) } returns reservation

        mockMvc.perform(
            put("/api/v1/reservations/$id")
                .header("X-User", "testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservation))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.peoples.length()").value(3))
            .andExpect(jsonPath("$.start").value(15))
    }

    @Test
    fun `test DELETE reservation existante renvoie 204`() {
        val id = UUID.randomUUID()
        every { reservationService.deleteReservation(id) } returns true

        mockMvc.perform(
            delete("/api/v1/reservations/$id")
                .header("X-User", "testuser")
        )
            .andExpect(status().isNoContent)

        verify { reservationService.deleteReservation(id) }
    }

    @Test
    fun `test DELETE reservation inexistante renvoie 404`() {
        val id = UUID.randomUUID()
        every { reservationService.deleteReservation(id) } returns false

        mockMvc.perform(
            delete("/api/v1/reservations/$id")
                .header("X-User", "testuser")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `test GET reservations by owner id`() {
        val date = LocalDate.now()
        val reservations = listOf(
            Reservation(UUID.randomUUID(), 7, listOf(7), 10, 9, 10, date),
            Reservation(UUID.randomUUID(), 7, listOf(7, 8), 11, 14, 15, date)
        )
        
        every { reservationService.getAllReservationsByOwnerId(7) } returns reservations

        mockMvc.perform(
            get("/api/v1/reservations/owner/7")
                .header("X-User", "testuser")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `test DELETE reservations by owner id`() {
        every { reservationService.deleteReservationsByOwnerId(9) } returns Unit

        mockMvc.perform(
            delete("/api/v1/reservations/owner/9")
                .header("X-User", "testuser")
        )
            .andExpect(status().isNoContent)

        verify { reservationService.deleteReservationsByOwnerId(9) }
    }
}
