package tech.realycorp.letro.data

sealed interface GatewayAvailabilityDataModel {
    object Unavailable : GatewayAvailabilityDataModel
    object Available : GatewayAvailabilityDataModel
    object Unknown : GatewayAvailabilityDataModel
}
