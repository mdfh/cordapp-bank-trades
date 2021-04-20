package com.template.contracts

import com.template.states.TradeState
import net.corda.core.contracts.Contract
import net.corda.core.identity.CordaX500Name
import net.corda.testing.contracts.DummyState
import net.corda.testing.core.DummyCommandData
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.transaction
import org.junit.Test
import kotlin.test.assertTrue

class ContractTests {
    private val traderA = TestIdentity(CordaX500Name("TraderA", "", "SA"))
    private val traderB = TestIdentity(CordaX500Name("TraderB", "", "SA"))

    private val ledgerServices = MockServices()

    // For issue flow, tradeState
    // For transfer state, one trade input state and one trade output state

    private val tradeState = TradeState(1000, traderA.party, traderB.party)
    private val tradeStateInput = TradeState(1000, traderA.party, traderB.party)
    private val tradeStateOutput = TradeState(1000,  traderA.party, traderB.party)

    @Test
    fun `trade contract implements contract`()
    {
        assertTrue(TradeContract() is Contract )
    }

    @Test
    fun `trade contract zero input`() {
        ledgerServices.transaction {
            input(TradeContract.ID, tradeState)
            command(traderA.publicKey, TradeContract.Commands.Submit())
            fails()
        }

        ledgerServices.transaction {
            output(TradeContract.ID, tradeState)
            command(traderA.publicKey, TradeContract.Commands.Submit())
            verifies()
        }
    }

    @Test
    fun `trade contract one output`() {
        ledgerServices.transaction {
            output(TradeContract.ID, tradeState)
            output(TradeContract.ID, tradeState)
            command(traderA.publicKey, TradeContract.Commands.Submit())
            fails()
        }

        ledgerServices.transaction {
            output(TradeContract.ID, tradeState)
            command(traderA.publicKey, TradeContract.Commands.Submit())
            verifies()
        }
    }

    @Test
    fun `trade contract requires the transaction output to be trade state`() {
        ledgerServices.transaction {
            output(TradeContract.ID, DummyState())
            command(traderA.publicKey, TradeContract.Commands.Submit())
            fails()
        }

        ledgerServices.transaction {
            output(TradeContract.ID, tradeState)
            command(traderA.publicKey, TradeContract.Commands.Submit())
            verifies()
        }
    }

    @Test
    fun `trade contract requires command to be issue command`() {
        ledgerServices.transaction {
            output(TradeContract.ID, tradeState)
            command(traderA.publicKey, DummyCommandData)
            fails()
        }

        ledgerServices.transaction {
            output(TradeContract.ID, tradeState)
            command(traderA.publicKey, TradeContract.Commands.Submit())
            verifies()
        }
    }

    @Test
    fun `trade contract requires issuer to be required signer`() {
        ledgerServices.transaction {
            output(TradeContract.ID, tradeState)
            command(traderA.publicKey, DummyCommandData)
            fails()
        }

        ledgerServices.transaction {
            output(TradeContract.ID, tradeState)
            command(traderA.publicKey, TradeContract.Commands.Submit())
            verifies()
        }
    }

    // ------ InProcess command -------

    @Test
    fun `trade contract InProcess should have one input one output`() {
        ledgerServices.transaction {
            input(TradeContract.ID, tradeStateInput)
            output(TradeContract.ID, tradeStateOutput.markInProcess())
            command(traderB.publicKey, TradeContract.Commands.InProcess())
            verifies()
        }

        ledgerServices.transaction {
            output(TradeContract.ID, tradeStateOutput)
            command(traderA.publicKey, TradeContract.Commands.InProcess())
            fails()
        }

        ledgerServices.transaction {
            input(TradeContract.ID, tradeStateInput)
            command(traderA.publicKey, TradeContract.Commands.InProcess())
            fails()
        }
    }

    @Test
    fun `trade contract requires command to be InProcess command`() {
        ledgerServices.transaction {
            input(TradeContract.ID, tradeStateInput)
            output(TradeContract.ID, tradeStateOutput)
            command(traderA.publicKey, DummyCommandData)
            fails()
        }

        ledgerServices.transaction {
            input(TradeContract.ID, tradeStateInput)
            output(TradeContract.ID, tradeStateOutput.markInProcess())
            command(traderA.publicKey, TradeContract.Commands.InProcess())
            verifies()
        }
    }

    // ------ Settle command -------

    @Test
    fun `trade contract Settle should have one input one output and status should be InProcess`() {
        ledgerServices.transaction {
            input(TradeContract.ID, tradeStateInput.markInProcess())
            output(TradeContract.ID, tradeStateOutput.markSettled())
            command(traderB.publicKey, TradeContract.Commands.Settle())
            verifies()
        }

        ledgerServices.transaction {
            output(TradeContract.ID, tradeStateOutput)
            command(traderA.publicKey, TradeContract.Commands.InProcess())
            fails()
        }

        ledgerServices.transaction {
            input(TradeContract.ID, tradeStateInput)
            command(traderA.publicKey, TradeContract.Commands.InProcess())
            fails()
        }
    }
}