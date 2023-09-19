package tech.relaycorp.letro.testing.veraid

import tech.relaycorp.letro.testing.crypto.generateRSAKeyPair

const val VERAID_USER_NAME = "alice"
const val VERAID_ORG_NAME = "example.com"
const val VERAID_MEMBER_ID = "$VERAID_USER_NAME@$VERAID_ORG_NAME"

val VERAID_MEMBER_KEY_PAIR = generateRSAKeyPair()
