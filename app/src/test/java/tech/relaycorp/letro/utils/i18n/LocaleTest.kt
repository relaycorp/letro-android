package tech.relaycorp.letro.utils.i18n

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.beUpperCase
import io.kotest.matchers.string.match
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Locale

val LOCALE = Locale("EN", "GB")

class LocaleTest {
    @Nested
    inner class NormaliseString {
        @Test
        fun `Should lower case the country code`() {
            LOCALE.country should beUpperCase()

            val localeString = LOCALE.normaliseString()

            val countryCode = localeString.split("-")[1]
            countryCode shouldBe LOCALE.country.lowercase()
        }

        @Test
        fun `Should result in empty string if language code is missing`() {
            val locale = Locale("", LOCALE.country)

            val localeString = locale.normaliseString()

            localeString shouldBe ""
        }

        @Test
        fun `Should only return the language code if country code is missing`() {
            val locale = Locale(LOCALE.language)

            val localeString = locale.normaliseString()

            localeString shouldBe locale.language
        }

        @Test
        fun `Should not serialise the variant`() {
            val locale = Locale(LOCALE.language, LOCALE.country, "Oxford")

            val localeString = locale.normaliseString()

            localeString should match(Regex("[a-z]{2}-[a-z]{2}"))
        }
    }

    @Nested
    inner class ParseLocale {
        val localeString = LOCALE.normaliseString()

        @Nested
        inner class LanguageCode {
            @Test
            fun `Language should be decoded`() {
                localeString.parseLocale().language shouldBe LOCALE.language
            }

            @Test
            fun `Language should be lower cased for consistency with Android`() {
                "EN-gb".parseLocale().language shouldBe "en"
            }
        }

        @Nested
        inner class CountryCode {
            @Test
            fun `Should be decoded if present`() {
                localeString.parseLocale().country shouldBe LOCALE.country
            }

            @Test
            fun `Should be upper cased for consistency with Android`() {
                "en-gb".parseLocale().country shouldBe "GB"
            }

            @Test
            fun `Should be absent if not present in string`() {
                "en".parseLocale().country shouldBe ""
            }
        }

        @Nested
        inner class VariantCode {
            @Test
            fun `Should be decoded if present`() {
                "en-gb-oxford".parseLocale().variant shouldBe "oxford"
            }

            @Test
            fun `Should be absent if not present in string`() {
                "en-gb".parseLocale().variant shouldBe ""
            }
        }
    }
}
