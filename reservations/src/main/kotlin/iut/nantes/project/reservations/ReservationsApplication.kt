package iut.nantes.project.reservations

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class ReservationsApplication {
    
    @Bean
    fun webClient(): WebClient {
        return WebClient.builder().build()
    }
}

fun main(args: Array<String>) {
    runApplication<ReservationsApplication>(*args)
}
