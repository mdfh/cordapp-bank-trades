package com.template.states

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class TradeStatus(val status: String) {
    SUBMITTED("SUBMITTED"),
    IN_PROCESS("IN_PROCESS"),
    SETTLED("SETTLED")
}