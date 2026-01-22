package iut.nantes.project.bff.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "bff.clients")
data class ClientProperties(
    val peopleUrl: String,
    val reservationUrl: String,
    val roomsUrl: String
)