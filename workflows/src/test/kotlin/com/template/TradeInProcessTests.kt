package com.template

import com.google.common.collect.ImmutableList
import com.template.flows.TradeInProcessInitiator
import com.template.flows.TradeInitiator
import org.junit.Before
import com.template.flows.TradeSettleResponder
import net.corda.core.transactions.SignedTransaction
import net.corda.testing.node.*
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals

class TradeInProcessTests {
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
        val tradeFlow = TradeInProcessInitiator("33c5d822-1084-4b96-898d-01b556962f79")
        val future = b.startFlow(tradeFlow)
        return future.get();
    }

    @Test
    fun `zero input state test`() {
        val signedTransaction = getSignedTransaction()
        assertEquals(0, signedTransaction.coreTransaction.inputs.size.toLong())
    }
}