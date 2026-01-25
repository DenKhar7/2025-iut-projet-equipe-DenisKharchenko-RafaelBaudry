package iut.nantes.project.peoples.controller

import com.fasterxml.jackson.databind.ObjectMapper
import iut.nantes.project.peoples.repository.Address
import iut.nantes.project.peoples.repository.AddressDTO
import iut.nantes.project.peoples.repository.People
import iut.nantes.project.peoples.repository.PeopleDTO
import iut.nantes.project.peoples.service.PeopleService
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

@WebMvcTest(PeopleController::class)
@ActiveProfiles("test")
class PeopleControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun peopleService(): PeopleService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var peopleService: PeopleService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    /**
     * Test qui verifie la creation dune personne avec des donnees valides
     * Le controller doit valider les donnees et appeler le service
     * puis renvoyer un 201 avec la personne creee
     */
    @Test
    fun `test POST peoples avec donnees valides renvoie 201`() {
        val addressDTO = AddressDTO("123 rue de la Republique", "Tours", "37000", "France")
        val peopleDTO = PeopleDTO("Julien", "Mercier", 30, addressDTO)
        
        val address = Address(1, "123 rue de la Republique", "Tours", "37000", "France")
        val createdPeople = People(1, "Julien", "Mercier", 30, address)
        
        every { peopleService.createPeople(any()) } returns createdPeople

        mockMvc.perform(
            post("/api/v1/peoples")
                .header("X-User", "testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peopleDTO))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("Julien"))
            .andExpect(jsonPath("$.lastName").value("Mercier"))

        verify { peopleService.createPeople(any()) }
    }

    @Test
    fun `test POST peoples avec prenom trop court renvoie 400`() {
        val addressDTO = AddressDTO("10 avenue Test", "Angers", "49000", "France")
        val peopleDTO = PeopleDTO("A", "Test", 25, addressDTO)

        mockMvc.perform(
            post("/api/v1/peoples")
                .header("X-User", "testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peopleDTO))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test POST peoples avec age trop jeune renvoie 400`() {
        val addressDTO = AddressDTO("5 rue des Enfants", "Reims", "51000", "France")
        val peopleDTO = PeopleDTO("Jeune", "Mineur", 15, addressDTO)

        mockMvc.perform(
            post("/api/v1/peoples")
                .header("X-User", "testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peopleDTO))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test POST peoples avec code postal invalide renvoie 400`() {
        val addressDTO = AddressDTO("8 rue Test", "Dijon", "ABC", "France")
        val peopleDTO = PeopleDTO("Test", "User", 25, addressDTO)

        mockMvc.perform(
            post("/api/v1/peoples")
                .header("X-User", "testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peopleDTO))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `test GET peoples renvoie la liste complete`() {
        val addr1 = Address(1, "1 rue A", "Caen", "14000", "France")
        val addr2 = Address(2, "2 rue B", "Orleans", "45000", "France")
        
        val people = listOf(
            People(1, "Manon", "Lambert", 25, addr1),
            People(2, "Kevin", "Girard", 30, addr2)
        )
        
        every { peopleService.findPeople(null) } returns people

        mockMvc.perform(
            get("/api/v1/peoples")
                .header("X-User", "testuser")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].firstName").value("Manon"))
            .andExpect(jsonPath("$[1].firstName").value("Kevin"))
    }

    @Test
    fun `test GET peoples avec filtre name trouve les bonnes personnes`() {
        val addr = Address(1, "1 rue Test", "Nancy", "54000", "France")
        val people = listOf(
            People(1, "Sophie", "Rousseau", 25, addr),
            People(2, "Valentin", "Rousseau", 28, addr)
        )
        
        every { peopleService.findPeople("Rousseau") } returns people

        mockMvc.perform(
            get("/api/v1/peoples")
                .header("X-User", "testuser")
                .param("name", "Rousseau")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].lastName").value("Rousseau"))
    }

    @Test
    fun `test GET peoples par id renvoie la bonne personne`() {
        val address = Address(1, "42 rue Answer", "Amiens", "80000", "France")
        val people = People(1, "Douglas", "Adams", 42, address)
        
        every { peopleService.findPeopleById(1) } returns people

        mockMvc.perform(
            get("/api/v1/peoples/1")
                .header("X-User", "testuser")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value("Douglas"))
            .andExpect(jsonPath("$.age").value(42))
    }

    @Test
    fun `test GET peoples par id inexistant renvoie 404`() {
        every { peopleService.findPeopleById(999) } returns null

        mockMvc.perform(
            get("/api/v1/peoples/999")
                .header("X-User", "testuser")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `test PUT peoples met a jour correctement`() {
        val addressDTO = AddressDTO("15 rue Nouvelle", "Limoges", "87000", "France")
        val peopleDTO = PeopleDTO("Oceane", "Renard", 31, addressDTO)
        
        val address = Address(1, "15 rue Nouvelle", "Limoges", "87000", "France")
        val updated = People(5, "Oceane", "Renard", 31, address)
        
        every { peopleService.updatePeople(5, any()) } returns updated

        mockMvc.perform(
            put("/api/v1/peoples/5")
                .header("X-User", "testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(peopleDTO))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value("Oceane"))
            .andExpect(jsonPath("$.age").value(31))
    }

    @Test
    fun `test DELETE peoples supprime correctement`() {
        every { peopleService.deletePeople(3) } returns true

        mockMvc.perform(
            delete("/api/v1/peoples/3")
                .header("X-User", "testuser")
        )
            .andExpect(status().isNoContent)

        verify { peopleService.deletePeople(3) }
    }

    @Test
    fun `test DELETE peoples inexistante renvoie 404`() {
        every { peopleService.deletePeople(888) } returns false

        mockMvc.perform(
            delete("/api/v1/peoples/888")
                .header("X-User", "testuser")
        )
    }
}