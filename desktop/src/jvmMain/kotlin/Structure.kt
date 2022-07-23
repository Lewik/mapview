import kotlinx.serialization.Serializable

//for internal use

@Serializable
data class MercatorPoint1(
    /**
     * Longitude, x
     * Avoid ambiguity! Mercator only!
     */
    val lng: Double = 0.toDouble(),
    /**
     * Latitude, y
     * Avoid ambiguity! Mercator only!
     */
    val lat: Double = 0.toDouble(),
)

@Serializable
data class Device(
    val id: String,
    val type: Type,
    val label: String,
    var dispatcherName: String = "",
    var state: State,
    var avr: Avr,
    var apv: Apv,
    val connections: MutableList<String>,
    val protectedConnections: MutableList<String>,
    var allowedOutputConnections: MutableList<String>,
    var networks: MutableList<String>,
    val powerSource: Boolean,
    val power: Int,
    val averagePower: Double,
    val busNumber: Int?,
    val numberOfCustomers: Int,
    val isInterBus: Boolean,
    val serialNumber: String,
    val decommissioned: Decommissioned = Decommissioned.FALSE,
    val plusConnection: String? = null,
    var sides: Map<String, String>,
    val pillarType: PillarType?,
    val voltage: Voltage,
) {


    /**
     * Класс напряжения
     */
    enum class Voltage {
        KV_04,
        KV_10
    }

    enum class Type {
        ASD,
        MLS,
        JUNCTION,
        PILLAR,
        BUS,
        THREE_PHASE_SEPARATOR,
        ONE_PHASE_SEPARATOR,
        LOAD,
        FUSE,
        GROUND_SEPARATOR,
        GROUND,
        OVERVOLTAGE_LIMITER,
        CABLE_CLUTCH,
        BREAKER,
        AV
    }

    enum class PillarType {
        ANCHOR,
        INTERMEDIATE
    }

    enum class Decommissioned {
        TRUE,
        FALSE
    }

    enum class State {
        OPEN,
        CLOSED;
    }

    enum class Avr {
        UNKNOWN,
        ON,
        OFF
    }

    enum class Apv {
        UNKNOWN,
        ON,
        OFF
    }
}

@Serializable
data class Building(
    val id: String,
    var type: Type,
    var deviceList: MutableList<Device>,
    val coordinates: MercatorPoint1,
    var label: String,
    var dispatcherName: String,
    val owner: Owner,
) {

    enum class Type {
        SUB_STATION,
        RP,
        ZTP,
        KTP,
        RECLOSER,
        PILLAR,
        DELIMITER,
        CABLE_ROTATION_POINT,
        CONTROL_ROOM
    }

    /**
     * Владелец
     */
    enum class Owner {
        SUBSCRIBER,
        COMPANY
    }

}

@Serializable
data class Line(
    val id: String,
    val connections: MutableList<String>, //TODO : make immutable
    val type: Type,
    val type2: Type2,
    val geoLength: Double,
    val dispatcherName: String,
    var owner: Owner?,
) {

    enum class Type {
        INNER,
        OUTER
    }

    enum class Type2 {
        AIR,
        CABLE,
        AIR_STUB
    }


    enum class Owner {
        SUBSCRIBER,
        COMPANY
    }

}

@Serializable
data class Structure(
    val buildingsById: Map<String, Building>,
    val linesById: Map<String, Line>,
)
