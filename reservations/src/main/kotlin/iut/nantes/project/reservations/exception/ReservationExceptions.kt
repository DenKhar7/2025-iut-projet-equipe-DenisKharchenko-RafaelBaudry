package iut.nantes.project.reservations.exception
// Exceptions personnalisees pour la gestion des resa
class ReservationNotFoundException(message: String) : RuntimeException(message)

class ReservationConflictException(message: String) : RuntimeException(message)

class InvalidReservationException(message: String) : RuntimeException(message)
