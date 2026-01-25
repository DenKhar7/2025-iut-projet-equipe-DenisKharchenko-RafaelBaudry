package iut.nantes.project.peoples.service

import iut.nantes.project.peoples.repository.Address
import iut.nantes.project.peoples.repository.HashPeopleDatabase
import iut.nantes.project.peoples.repository.People
import iut.nantes.project.peoples.repository.PeopleDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PeopleServiceTest {

    private lateinit var database: PeopleDatabase
    private lateinit var webClient: WebClient
    private lateinit var service: PeopleService

    @BeforeEach
    fun setup() {
        database = HashPeopleDatabase()
        webClient = mockk(relaxed = true)
        service = PeopleService(database, webClient)
    }

    @Test
    fun `test creation d une personne simple`() {
        val address = Address(0, "12 rue de la Paix", "Marseille", "13001", "France")
        val people = People(0, "Maxime", "Bertrand", 25, address)

        val result = service.createPeople(people)

        assertNotNull(result.id)
        assertEquals("Maxime", result.firstName)
        assertEquals("Bertrand", result.lastName)
        assertEquals(25, result.age)
    }

    @Test
    fun `test creation de deux personnes avec la meme adresse partage l adresse`() {
        val address1 = Address(0, "5 rue Victor Hugo", "Lille", "59000", "France")
        val people1 = People(0, "Amelie", "Laurent", 30, address1)
        
        val address2 = Address(0, "5 rue Victor Hugo", "Lille", "59000", "France")
        val people2 = People(0, "Thomas", "Laurent", 32, address2)

        val result1 = service.createPeople(people1)
        val result2 = service.createPeople(people2)

        assertEquals(result1.address.addressId, result2.address.addressId)
    }

    @Test
    fun `test mise a jour d une personne existante`() {
        val address = Address(0, "10 avenue Kennedy", "Strasbourg", "67000", "France")
        val people = People(0, "Clara", "Fontaine", 28, address)
        val created = service.createPeople(people)

        val newAddress = Address(0, "15 boulevard Emile", "Strasbourg", "67100", "France")
        val updated = People(0, "Clara-Marie", "Fontaine", 29, newAddress)
        val result = service.updatePeople(created.id, updated)

        assertNotNull(result)
        assertEquals("Clara-Marie", result!!.firstName)
        assertEquals(29, result.age)
        assertEquals("67100", result.address.zipCode)
    }

    @Test
    fun `test mise a jour d une personne qui existe pas renvoie null`() {
        val id = 999L
        val address = Address(0, "1 rue Test", "Grenoble", "38000", "France")
        val people = People(0, "Test", "Test", 25, address)

        val result = service.updatePeople(id, people)

        assertNull(result)
    }

    /**
     * Test pour verifier que quand on supprime une personne
     * le service appelle bien le endpoint de reservations
     * pour supprimer toutes les reservations de cette personne
     */
    @Test
    fun `test suppression d une personne appelle le service de reservations`() {
        val address = Address(0, "8 rue des Lilas", "Toulouse", "31000", "France")
        val people = People(0, "Hugo", "Moreau", 35, address)
        val created = service.createPeople(people)

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>(relaxed = true)
        val requestBodySpec = mockk<WebClient.RequestBodySpec>(relaxed = true)
        val responseSpec = mockk<WebClient.ResponseSpec>(relaxed = true)
        
        every { webClient.delete() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.retrieve() } returns responseSpec
        every { responseSpec.toBodilessEntity() } returns Mono.empty()

        val deleted = service.deletePeople(created.id)

        assertTrue(deleted)
        verify { webClient.delete() }
    }

    @Test
    fun `test recherche par nom de famille trouve les bonnes personnes`() {
        val addr1 = Address(0, "1 rue A", "Bordeaux", "33000", "France")
        val addr2 = Address(0, "2 rue B", "Bordeaux", "33100", "France")
        val addr3 = Address(0, "3 rue C", "Bordeaux", "33200", "France")
        
        service.createPeople(People(0, "Lucas", "Rousseau", 25, addr1))
        service.createPeople(People(0, "Lea", "Rousseau", 27, addr2))
        service.createPeople(People(0, "Nathan", "Garnier", 30, addr3))

        val results = service.findPeople("Rousseau")

        assertEquals(2, results.size)
        assertTrue(results.all { it.lastName == "Rousseau" })
    }

    @Test
    fun `test recherche sans filtre renvoie tout le monde`() {
        val addr1 = Address(0, "1 rue A", "Nice", "06000", "France")
        val addr2 = Address(0, "2 rue B", "Lyon", "69000", "France")
        val addr3 = Address(0, "3 rue C", "Rennes", "35000", "France")
        
        service.createPeople(People(0, "Chloe", "Mercier", 22, addr1))
        service.createPeople(People(0, "Antoine", "Blanc", 24, addr2))
        service.createPeople(People(0, "Camille", "Lefebvre", 26, addr3))

        val results = service.findPeople(null)

        assertEquals(3, results.size)
    }

    @Test
    fun `test recherche par id trouve la bonne personne`() {
        val address = Address(0, "20 rue Mozart", "Montpellier", "34000", "France")
        val people = People(0, "Emma", "Simon", 29, address)
        val created = service.createPeople(people)

        val found = service.findPeopleById(created.id)

        assertNotNull(found)
        assertEquals("Emma", found!!.firstName)
        assertEquals("Simon", found.lastName)
    }

    @Test
    fun `test recherche par id inexistant renvoie null`() {
        val id = 12345L
        val found = service.findPeopleById(id)
        
        assertNull(found)
    }
}
