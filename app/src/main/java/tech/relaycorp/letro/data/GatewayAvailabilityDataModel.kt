package tech.relaycorp.letro.data

sealed interface GatewayAvailabilityDataModel {
    object Unavailable : GatewayAvailabilityDataModel
    object Available : GatewayAvailabilityDataModel
    object Unknown : GatewayAvailabilityDataModel
}
