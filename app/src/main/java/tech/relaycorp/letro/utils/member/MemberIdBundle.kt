package tech.relaycorp.letro.utils.member

import tech.relaycorp.letro.server.messages.InvalidAccountCreationException
import tech.relaycorp.letro.utils.LetroOids
import tech.relaycorp.veraid.Member
import tech.relaycorp.veraid.pki.MemberIdBundle
import tech.relaycorp.veraid.pki.PkiException
import java.time.ZonedDateTime

@Throws(InvalidAccountCreationException::class)
suspend fun verifyBundle(veraidBundle: ByteArray): Pair<MemberIdBundle, Member> {
    val bundle = try {
        MemberIdBundle.deserialise(veraidBundle)
    } catch (exc: PkiException) {
        throw InvalidAccountCreationException("Member id bundle is malformed", exc)
    }

    val now = ZonedDateTime.now()
    val verificationPeriod = now..now
    val bundleMember = try {
        bundle.verify(LetroOids.LETRO_VERAID_OID, verificationPeriod)
    } catch (exc: PkiException) {
        throw InvalidAccountCreationException("Member id bundle is invalid", exc)
    }
    return Pair(bundle, bundleMember)
}
