package tech.relaycorp.letro.conversation.time

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.utils.time.isCurrentYear
import tech.relaycorp.letro.utils.time.isLessThanDayAgo
import tech.relaycorp.letro.utils.time.isLessThanHourAgo
import tech.relaycorp.letro.utils.time.isLessThanWeeksAgo
import tech.relaycorp.letro.utils.time.isToday
import tech.relaycorp.letro.utils.time.nowUTC
import java.time.ZoneId
import java.time.ZonedDateTime

class ZonedDateTimeTest {

    @Test
    fun `Test isToday() function`() {
        val today = ZonedDateTime.now()
        today.isToday() shouldBe true

        val tomorrow = ZonedDateTime.now().plusDays(1L)
        tomorrow.isToday() shouldBe false

        val weekAgo = ZonedDateTime.now().minusWeeks(1L)
        weekAgo.isToday() shouldBe false
    }

    @Test
    fun `Test isCurrentYear() function`() {
        val year2022 = ZonedDateTime.of(2022, 12, 31, 23, 59, 59, 9, ZoneId.systemDefault())

        year2022.isCurrentYear() shouldBe false

        val today = ZonedDateTime.now()
        today.isCurrentYear() shouldBe true
    }

    @Test
    fun `Test lessThanHourAgo() function`() {
        val halfOfAnHourAgo = ZonedDateTime.now().minusMinutes(30L)
        halfOfAnHourAgo.isLessThanHourAgo() shouldBe true

        val hourAndMinuteAgo = ZonedDateTime.now().minusHours(1L).minusMinutes(1L)
        hourAndMinuteAgo.isLessThanHourAgo() shouldBe false
    }

    @Test
    fun `Test lessThanDayAgo() function`() {
        val halfOfAnHourAgo = ZonedDateTime.now().minusMinutes(30L)
        halfOfAnHourAgo.isLessThanDayAgo() shouldBe true

        val dayAndMinuteAgo = ZonedDateTime.now().minusDays(1L).minusMinutes(1L)
        dayAndMinuteAgo.isLessThanDayAgo() shouldBe false

        val lastYear = ZonedDateTime.now().minusHours(1L).minusYears(1L)
        lastYear.isLessThanDayAgo() shouldBe false
    }

    @Test
    fun `Test lessThanWeeksAgo() function`() {
        val twoDaysAgo = ZonedDateTime.now().minusDays(2L)
        twoDaysAgo.isLessThanWeeksAgo(1L) shouldBe true

        val oneWeekAndMinuteAgo = ZonedDateTime.now().minusWeeks(1L).minusMinutes(1L)
        oneWeekAndMinuteAgo.isLessThanWeeksAgo(1L) shouldBe false
        oneWeekAndMinuteAgo.isLessThanWeeksAgo(2L) shouldBe true
    }

    @Test
    fun `Test UTC timezone`() {
        val oneMinuteAgoUTC = nowUTC().minusMinutes(1L)
        oneMinuteAgoUTC.isLessThanHourAgo() shouldBe true
    }
}
