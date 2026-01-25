package iut.nantes.project.reservations.exception

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
/**
 * Controller advice pour gérer les exceptions
 */
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

    @ExceptionHandler(ReservationNotFoundException::class)
    fun handleReservationNotFound(ex: ReservationNotFoundException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = 404,
            message = ex.message ?: "Reservation not found"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(ReservationConflictException::class)
    fun handleReservationConflict(ex: ReservationConflictException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = 409,
            message = ex.message ?: "Reservation conflict"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(InvalidReservationException::class)
    fun handleInvalidReservation(ex: InvalidReservationException): ResponseEntity<ErrorResponse> {
        val error = ErrorResponse(
            timestamp = ZonedDateTime.now().toString(),
            status = 400,
            message = ex.message ?: "Invalid reservation data"
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
