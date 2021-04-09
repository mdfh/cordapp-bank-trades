package com.template

import com.google.common.collect.ImmutableList
import com.template.contracts.TradeContract
import org.junit.Before
import com.template.flows.TradeInitiator
import com.template.flows.TradeSettleResponder
import com.template.states.TradeState
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.*
import org.junit.After
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TradeInitiatorTests {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows"))))

        a = network.createNode(MockNodeParameters())
        b = network.createNode(MockNodeParameters())

        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        for (node in ImmutableList.of(a, b)) {
            node?.registerInitiatedFlow(TradeSettleResponder::class.java)
        }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    private fun getSignedTransaction() : SignedTransaction
    {
        val tradeFlow = TradeInitiator(100, b.info.legalIdentities.first())
        val future = a.startFlow(tradeFlow)
        return future.get();
    }

    @Test
    fun `zero input state test`() {
        val signedTransaction = getSignedTransaction()
        assertEquals(0, signedTransaction.coreTransaction.inputs.size.toLong())
    }

    @Test
    fun `one output state test`() {
        val signedTransaction = getSignedTransaction()
        assertEquals(1, signedTransaction.coreTransaction.outputs.size.toLong())
    }

    @Test
    fun `output state signature`() {
        val signedTransaction = getSignedTransaction()
        val output = signedTransaction.tx.outputsOfType<TradeState>().first()
        assertEquals(a.info.legalIdentities.first(), output.assignedBy)
    }

    @Test
    fun `transaction should have only one command`() {
        val signedTransaction = getSignedTransaction()
        assertEquals(1, signedTransaction.tx.commands.size)
    }

    @Test
    fun `transaction should have issue command`() {
        val signedTransaction = getSignedTransaction()
        val command = signedTransaction.tx.commands.first()
        assert(command.value is TradeContract.Commands.Submit)
    }

    @Test
    fun `transaction should be signed by issuer`() {
        val signedTransaction = getSignedTransaction()
        val command = signedTransaction.tx.commands.first()
        assertTrue(command.signers.contains(a.info.legalIdentities.first().owningKey))
    }
}