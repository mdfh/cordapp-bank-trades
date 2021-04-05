package com.template.contracts

import com.template.states.TradeState
import net.corda.testing.node.MockServices
import net.corda.testing.core.TestIdentity
import net.corda.core.identity.CordaX500Name
import org.junit.jupiter.api.DisplayName
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import org.junit.Assert
import org.junit.Test
import java.util.*

class TradeStateTests {
    private val ledgerServices = MockServices()
    private val bankA = TestIdentity(CordaX500Name("BankA", "", "SA")).party
    private val bankB = TestIdentity(CordaX500Name("BankB", "", "SA")).party
    private val tradeState = TradeState(100, Calendar.getInstance().time, bankA, bankB)
    @Test
    @DisplayName("trade state implements contract")
    fun tradeStateImplementsContract() {
        assert(tradeState is ContractState)
    }

    @Test
    @DisplayName("trade state implements linear state")
    fun tradeStateImplementsLinear() {
        assert(tradeState is LinearState)
    }

    @Test
    @DisplayName("trade state participants")
    fun tradeStateParticipants() {
        Assert.assertEquals(2, tradeState.participants.size.toLong())
        Assert.assertTrue(tradeState.participants.contains(bankA))
        Assert.assertTrue(tradeState.participants.contains(bankB))
    }

    @Test
    @DisplayName("trade state getters")
    fun tradeStateGetters() {
        Assert.assertEquals(100, tradeState.amount)
        Assert.assertEquals(bankA, tradeState.assignedBy)
        Assert.assertEquals(bankB, tradeState.assignedTo)
    }
}