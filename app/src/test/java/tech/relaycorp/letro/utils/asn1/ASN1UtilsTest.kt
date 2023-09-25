package tech.relaycorp.letro.utils.asn1

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1StreamParser
import org.bouncycastle.asn1.ASN1TaggedObject
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.DEROctetStringParser
import org.bouncycastle.asn1.DERVisibleString
import org.bouncycastle.asn1.DLSequenceParser
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ASN1UtilsTest {
    val visibleString = DERVisibleString("foo")
    val octetString = DEROctetString("bar".toByteArray())

    @Nested
    inner class MakeSequence {
        @Test
        fun `Values should be explicitly tagged by default`() {
            val sequence = ASN1Utils.makeSequence(listOf(visibleString, octetString))

            sequence.size() shouldBe 2

            val item1 = sequence.getObjectAt(0)
            item1 should beInstanceOf<DERVisibleString>()
            visibleString.string shouldBe (item1 as DERVisibleString).string

            val item2 = sequence.getObjectAt(1)
            item2 should beInstanceOf<DEROctetString>()
            octetString.octets shouldBe (item2 as DEROctetString).octets
        }

        @Test
        fun `Implicitly-tagged values should be supported`() {
            val sequence = ASN1Utils.makeSequence(listOf(visibleString, octetString), false)

            sequence.size() shouldBe 2

            val item1 = ASN1Utils.getVisibleString(sequence.getObjectAt(0) as ASN1TaggedObject)
            visibleString.string shouldBe item1.string

            val item2 = ASN1Utils.getOctetString(sequence.getObjectAt(1) as ASN1TaggedObject)
            octetString.octets shouldBe item2.octets
        }
    }

    @Nested
    inner class SerializeSequence {
        @Test
        fun `Values should be explicitly tagged by default`() {
            val serialization = ASN1Utils.serializeSequence(listOf(visibleString, octetString))

            val parser = ASN1StreamParser(serialization)
            val sequence = parser.readObject() as DLSequenceParser

            val item1 = sequence.readObject()
            item1 should beInstanceOf<DERVisibleString>()
            visibleString.string shouldBe (item1 as DERVisibleString).string

            val item2 = sequence.readObject()
            item2 should beInstanceOf<DEROctetStringParser>()
            octetString.octets shouldBe
                ((item2 as DEROctetStringParser).loadedObject as DEROctetString).octets
        }

        @Test
        fun `Implicitly-tagged values should be supported`() {
            val serialization =
                ASN1Utils.serializeSequence(listOf(visibleString, octetString), false)

            val parser = ASN1StreamParser(serialization)
            val sequence =
                ASN1Sequence.getInstance(parser.readObject() as DLSequenceParser).toArray()

            val item1 = ASN1Utils.getVisibleString(sequence[0] as ASN1TaggedObject)
            visibleString.string shouldBe item1.string

            val item2 = ASN1Utils.getOctetString(sequence[1] as ASN1TaggedObject)
            octetString.octets shouldBe item2.octets
        }
    }

    @Nested
    inner class DeserializeSequence {
        @Test
        fun `Value should be refused if it's empty`() {
            val exception = assertThrows<ASN1Exception> {
                ASN1Utils.deserializeHeterogeneousSequence(byteArrayOf())
            }

            "Value is empty" shouldBe exception.message
        }

        @Test
        fun `Value should be refused if it's not DER-encoded`() {
            val exception = assertThrows<ASN1Exception> {
                ASN1Utils.deserializeHeterogeneousSequence("a".toByteArray())
            }

            "Value is not DER-encoded" shouldBe exception.message
        }

        @Test
        fun `Value should be refused if it's not a sequence`() {
            val serialization = DERVisibleString("hey").encoded

            val exception = assertThrows<ASN1Exception> {
                ASN1Utils.deserializeHeterogeneousSequence(serialization)
            }

            "Value is not an ASN.1 sequence" shouldBe exception.message
        }

        @Test
        fun `Explicitly tagged items should be deserialized with their corresponding types`() {
            val serialization = ASN1Utils.serializeSequence(listOf(visibleString, visibleString))

            val sequence = ASN1Utils.deserializeHomogeneousSequence<DERVisibleString>(serialization)

            2 shouldBe sequence.size
            val value1Deserialized = sequence.first()
            visibleString shouldBe value1Deserialized
            val value2Deserialized = sequence.last()
            visibleString shouldBe value2Deserialized
        }

        @Test
        fun `Explicitly tagged items with unexpected types should be refused`() {
            val serialization = ASN1Utils.serializeSequence(listOf(visibleString, octetString))

            val exception = assertThrows<ASN1Exception> {
                ASN1Utils.deserializeHomogeneousSequence<DERVisibleString>(serialization)
            }

            exception.message shouldBe
                "Sequence contains an item of an unexpected type " +
                "(${octetString::class.java.simpleName})"
        }

        @Test
        fun `Implicitly tagged items should be deserialized with their corresponding types`() {
            val serialization =
                ASN1Utils.serializeSequence(listOf(visibleString, octetString), false)

            val sequence = ASN1Utils.deserializeHeterogeneousSequence(serialization)

            2 shouldBe sequence.size
            visibleString.octets shouldBe
                ASN1Utils.getVisibleString(sequence.first()).octets
            octetString.octets shouldBe ASN1Utils.getOctetString(sequence[1]).octets
        }
    }
}
