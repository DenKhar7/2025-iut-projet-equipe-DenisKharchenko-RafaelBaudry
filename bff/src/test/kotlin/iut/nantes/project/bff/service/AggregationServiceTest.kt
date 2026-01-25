package iut.nantes.project.bff.service

import iut.nantes.project.bff.domain.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AggregationServiceTest {

    private lateinit var peopleClient: PeopleClient
    private lateinit var reservationClient: ReservationClient
    private lateinit var roomClient: RoomClient
    private lateinit var service: AggregationService

    @BeforeEach
    fun setup() {
        peopleClient = mockk()
        reservationClient = mockk()
        roomClient = mockk()
        service = AggregationService(peopleClient, reservationClient, roomClient)
    }

    /**
     * Test de lagregation dune personne avec ses reservations
     * On recupere les infos de la personne et on va chercher toutes ses reservations
     * pour construire un objet complet avec la liste des IDs de reservations
     */
    @Test
    fun `test agregation personne avec ses reservations`() {
        val peopleDTO = PeopleDTO(
            1,
            "Karim",
            "Benzema",
            33,
            AddressDTO("7 rue du Foot", "Lyon", "69007", "France")
        )
        every { peopleClient.getPeopleById(1) } returns peopleDTO

        val resaId1 = UUID.randomUUID()
        val resaId2 = UUID.randomUUID()
        val reservations = listOf(
            ReservationDTO(resaId1, 1, listOf(1, 2), 5, 9, 10, LocalDate.now()),
            ReservationDTO(resaId2, 1, listOf(1), 8, 14, 15, LocalDate.now())
        )
        every { reservationClient.getReservationsByOwner(1) } returns reservations

        val result = service.getPeopleWithReservation(1)

        assertEquals("Karim", result.firstName)
        assertEquals("Benzema", result.lastName)
        assertEquals(2, result.reservations.size)
        assertEquals(resaId1.toString(), result.reservations[0])
    }

    @Test
    fun `test agregation personne sans reservations`() {
        val peopleDTO = PeopleDTO(
            5,
            "Simone",
            "Veil",
            45,
            AddressDTO("1 rue Nobel", "Paris", "75001", "France")
        )
        every { peopleClient.getPeopleById(5) } returns peopleDTO
        every { reservationClient.getReservationsByOwner(5) } returns emptyList()

        val result = service.getPeopleWithReservation(5)

        assertEquals("Simone", result.firstName)
        assertEquals(0, result.reservations.size)
    }

    @Test
    fun `test agregation reservation avec toutes les infos`() {
        val resaId = UUID.randomUUID()
        val date = LocalDate.of(2025, 6, 15)
        val reservation = ReservationDTO(
            resaId,
            ownerId = 2,
            peoples = listOf(2, 3, 4),
            roomId = 10,
            start = 10,
            end = 12,
            day = date
        )
        every { reservationClient.getReservationById(resaId) } returns reservation

        val owner = PeopleDTO(2, "Victor", "Hugo", 40, 
            AddressDTO("5 rue Miserables", "Paris", "75001", "France"))
        every { peopleClient.getPeopleById(2) } returns owner

        val p3 = PeopleDTO(3, "Cosette", "Fauchelevent", 20,
            AddressDTO("5 rue Miserables", "Paris", "75001", "France"))
        val p4 = PeopleDTO(4, "Marius", "Pontmercy", 25,
            AddressDTO("10 rue Barricade", "Paris", "75002", "France"))
        every { peopleClient.getPeopleById(3) } returns p3
        every { peopleClient.getPeopleById(4) } returns p4

        val room = RoomSummary(10, "Salle Victor Hugo")
        every { roomClient.getRoomById(10) } returns room

        val result = service.getReservationDetail(resaId)

        assertEquals("Victor", result.owner.firstName)
        assertEquals("Hugo", result.owner.lastName)
        assertEquals(3, result.peoples.size)
        assertEquals("Victor", result.peoples[0].firstName)
        assertEquals("Cosette", result.peoples[1].firstName)
        assertEquals("Marius", result.peoples[2].firstName)
        assertEquals("Salle Victor Hugo", result.roomId.name)
        assertEquals(10, result.start)
        assertEquals(12, result.end)
    }

    @Test
    fun `test agregation reservation avec personne supprimee`() {
        val resaId = UUID.randomUUID()
        val date = LocalDate.now()
        val reservation = ReservationDTO(
            resaId,
            ownerId = 1,
            peoples = listOf(1, 999),
            roomId = 5,
            start = 9,
            end = 10,
            day = date
        )
        every { reservationClient.getReservationById(resaId) } returns reservation

        val owner = PeopleDTO(1, "Alice", "Wonder", 30,
            AddressDTO("1 rue Lapin", "Marseille", "13001", "France"))
        every { peopleClient.getPeopleById(1) } returns owner

        every { peopleClient.getPeopleById(999) } throws RuntimeException("Not found")

        val room = RoomSummary(5, "Salle des Merveilles")
        every { roomClient.getRoomById(5) } returns room

        val result = service.getReservationDetail(resaId)

        assertEquals(2, result.peoples.size)
        assertEquals("Alice", result.peoples[0].firstName)
        assertEquals("DELETED", result.peoples[1].firstName)
        assertEquals("DELETED", result.peoples[1].lastName)
    }

    @Test
    fun `test agregation reservation avec owner supprime`() {
        val resaId = UUID.randomUUID()
        val reservation = ReservationDTO(
            resaId,
            ownerId = 888,
            peoples = listOf(1),
            roomId = 5,
            start = 14,
            end = 15,
            day = LocalDate.now()
        )
        every { reservationClient.getReservationById(resaId) } returns reservation

        every { peopleClient.getPeopleById(888) } throws RuntimeException("Gone")

        val p1 = PeopleDTO(1, "Bob", "Dylan", 35,
            AddressDTO("1 rue Music", "Lille", "59000", "France"))
        every { peopleClient.getPeopleById(1) } returns p1

        val room = RoomSummary(5, "Salle Concert")
        every { roomClient.getRoomById(5) } returns room

        val result = service.getReservationDetail(resaId)

        assertEquals("DELETED", result.owner.firstName)
        assertEquals("DELETED", result.owner.lastName)
        assertEquals(888, result.owner.id)
    }

    @Test
    fun `test agregation reservation avec salle inconnue`() {
        val resaId = UUID.randomUUID()
        val reservation = ReservationDTO(
            resaId,
            ownerId = 1,
            peoples = listOf(1),
            roomId = 777,
            start = 10,
            end = 11,
            day = LocalDate.now()
        )
        every { reservationClient.getReservationById(resaId) } returns reservation

        val owner = PeopleDTO(1, "Charlie", "Chaplin", 50,
            AddressDTO("1 rue Cinema", "Paris", "75001", "France"))
        every { peopleClient.getPeopleById(1) } returns owner

        every { roomClient.getRoomById(777) } throws RuntimeException("Room not found")

        val result = service.getReservationDetail(resaId)

        assertNotNull(result.roomId)
        assertEquals(777, result.roomId.id)
        assertEquals("Information salle indisponible", result.roomId.name)
    }

    @Test
    fun `test agregation reservation complete avec tout le monde present`() {
        val resaId = UUID.randomUUID()
        val date = LocalDate.of(2026, 1, 25)
        val reservation = ReservationDTO(
            resaId,
            ownerId = 1,
            peoples = listOf(1, 2, 3, 4, 5),
            roomId = 42,
            start = 8,
            end = 18,
            day = date
        )
        every { reservationClient.getReservationById(resaId) } returns reservation

        every { peopleClient.getPeopleById(1) } returns 
            PeopleDTO(1, "Patrick", "Boss", 45, AddressDTO("1 rue Chef", "Bordeaux", "33000", "France"))
        every { peopleClient.getPeopleById(2) } returns
            PeopleDTO(2, "Sophie", "Dev", 28, AddressDTO("2 rue Code", "Lyon", "69001", "France"))
        every { peopleClient.getPeopleById(3) } returns
            PeopleDTO(3, "Marc", "Design", 32, AddressDTO("3 rue Art", "Toulouse", "31000", "France"))
        every { peopleClient.getPeopleById(4) } returns
            PeopleDTO(4, "Julie", "Marketing", 30, AddressDTO("4 rue Pub", "Rennes", "35000", "France"))
        every { peopleClient.getPeopleById(5) } returns
            PeopleDTO(5, "Tom", "Sales", 35, AddressDTO("5 rue Vente", "Nice", "06000", "France"))

        every { roomClient.getRoomById(42) } returns RoomSummary(42, "Grande Salle Conference")

        val result = service.getReservationDetail(resaId)

        assertEquals("Patrick", result.owner.firstName)
        assertEquals(5, result.peoples.size)
        assertEquals("Grande Salle Conference", result.roomId.name)
        assertEquals(8, result.start)
        assertEquals(18, result.end)
    }
}
