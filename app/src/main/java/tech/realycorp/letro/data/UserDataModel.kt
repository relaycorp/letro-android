package tech.realycorp.letro.data

data class UserDataModel(
    val username: String,
    val connections: List<String> = emptyList(),
)
