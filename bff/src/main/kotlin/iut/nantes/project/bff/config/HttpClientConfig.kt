package iut.nantes.project.bff.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(ClientProperties::class)
class HttpClientConfig(val clientProperties: ClientProperties){
    private fun addSecurityHeader(builder: RestClient.Builder): RestClient.Builder {
        return builder.requestInterceptor { request, body, execution ->
            val auth = SecurityContextHolder.getContext().authentication

            if (auth != null && auth.isAuthenticated) {
                request.headers.add("X-User", auth.name)
            }

            execution.execute(request, body)
        }
    }
    @Bean
    fun peopleRestClient(builder: RestClient.Builder): RestClient {
        return addSecurityHeader(builder)
            .baseUrl(clientProperties.peopleUrl)
            .build()
    }

    @Bean
    fun reservationRestClient(builder: RestClient.Builder): RestClient {
        return addSecurityHeader(builder)
            .baseUrl(clientProperties.reservationUrl)
            .build()
    }

}