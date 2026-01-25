package iut.nantes.project.bff.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime

data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val message: String
)

/**
 * Class pour gerer les exceptions dans bff
 */
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = ex.statusCode.value(),
            message = ex.reason ?: "Error"
        )
        return ResponseEntity.status(ex.statusCode).body(error)
    }

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientError(ex: HttpClientErrorException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = ex.statusCode.value(),
            message = ex.message ?: "Client error"
        )
        return ResponseEntity.status(ex.statusCode).body(error)
    }

    @ExceptionHandler(HttpServerErrorException::class)
    fun handleHttpServerError(ex: HttpServerErrorException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = ex.statusCode.value(),
            message = ex.message ?: "Serveur error"
        )
        return ResponseEntity.status(ex.statusCode).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = 500,
            message = ex.message ?: "Interne serveur error"
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
