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
    val busPlate: String? = null,
    val busColor: String? = null,
    val driver: String? = null,
    val seats: String? = null,
    val price: String? = null
) {
    constructor(id: String, busColor: String, seats: String, route: Route) : this(
        id,
        route.from,
        route.to,
        route.departureDate,
        route.departureTime,
        route.bus,
        busColor,
        route.driver,
        seats,
        (route.price.toString().toInt() * seats.toInt()).toString()
    )
}

data class Route(
    val id: String? = null,
    val from: String? = null,
    val to: String? = null,
    val departureDate: String? = null,
    val departureTime: String? = null,
    val arrivalDate: String? = null,
    val arrivalTime: String? = null,
    val bus: String? = null,
    val ticketsLeft: String? = null,
    val driver: String? = null,
    val price: String? = null,
    val travelTime: String = arrivalTime + departureDate
)

data class Stop(
    val name: String? = null
) {
    override fun toString(): String = name.toString()
}