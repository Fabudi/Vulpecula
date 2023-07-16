package inc.fabudi.vulpecula.domain

data class User(
    val uid: String? = null, val name: String? = null, val lastname: String? = null
)

data class Ticket(
    val id: String? = null,
    val from: String? = null,
    val to: String? = null,
    val departureDate: String? = null,
    val departureTime: String? = null,
    val arrivalDate: String? = null,
    val arrivalTime: String? = null,
    val bus: String? = null,
    val seatsLeft: Int? = 0,
    val driver: String? = null,
    val price: Int? = 0
)

data class Route(
    val id: String? = null,
    val from: String? = null,
    val to: String? = null,
    val departureDate: String? = null,
    val departureTime: String? = null,
    val arrivalDate: String? = null,
    val arrivalTime: String? = null,
    val bus: String? = null,
    val seatsLeft: String? = null,
    val driver: String? = null,
    val price: String? = null,
    val travelTime: String = arrivalTime + departureDate
)

data class Stop(
    val name: String? = null
) {
    override fun toString(): String = name.toString()
}