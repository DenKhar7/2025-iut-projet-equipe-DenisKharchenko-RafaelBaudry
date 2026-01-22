package iut.nantes.project.bff.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(ClientProperties::class)
class HttpClientConfig(val clientProperties: ClientProperties){
    @Bean
    fun peopleRestClient(builder: RestClient.Builder): RestClient {
        return builder
            .baseUrl(clientProperties.peopleUrl)
            .build()
    }

    @Bean
    fun reservationRestClient(builder: RestClient.Builder): RestClient {
        return builder
            .baseUrl(clientProperties.reservationUrl)
            .build()
    }

}