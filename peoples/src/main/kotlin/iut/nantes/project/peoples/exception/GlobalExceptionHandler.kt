package iut.nantes.project.peoples.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.ZonedDateTime

data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val message: String
)

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }
            .joinToString(", ")
        
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = 400,
            message = errors.ifEmpty { "Validation failed" }
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(PeopleNotFoundException::class)
    fun handlePeopleNotFound(ex: PeopleNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = 404,
            message = ex.message ?: "People not found"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(InvalidPeopleException::class)
    fun handleInvalidPeople(ex: InvalidPeopleException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = 400,
            message = ex.message ?: "Invalid people data"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = 400,
            message = ex.message ?: "Invalid request"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = 500,
            message = ex.message ?: "Internal server error"
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
