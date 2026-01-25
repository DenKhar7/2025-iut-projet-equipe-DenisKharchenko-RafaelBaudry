package iut.nantes.project.peoples

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class PeoplesApplication {
    // WebClient pour les appels HTTP
    @Bean
    fun webClient(): WebClient {
        return WebClient.builder().build()
    }
}

fun main(args: Array<String>) {
    runApplication<PeoplesApplication>(*args)
}
