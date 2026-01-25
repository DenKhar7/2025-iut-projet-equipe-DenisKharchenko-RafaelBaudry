package iut.nantes.project.peoples.exception
// Exceptionns personaliser pour les Peoples
class PeopleNotFoundException(message: String) : RuntimeException(message)

class InvalidPeopleException(message: String) : RuntimeException(message)
