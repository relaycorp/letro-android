package tech.relaycorp.letro.navigation

import io.kotest.matchers.string.shouldEndWith
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.ui.navigation.Route

class RouteTest {

    @Test
    fun `Test that names of all inheritances of Route ends with _name suffix (only for singletons)`() {
        val allRoutes = Route::class.nestedClasses
        val singletons = allRoutes
            .mapNotNull {
                try {
                    it.objectInstance as Route
                } catch (e: Exception) {
                    null
                }
            }
        singletons.forEach {
            it.name shouldEndWith "_route"
        }
    }
}
