package com.template

import com.google.common.collect.ImmutableList
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.Before
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.TestCordapp
import com.template.flows.TradeSettleResponder
import org.junit.After

class TradeInProcessTests {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows"))))
        a = network.createPartyNode(null)
        b = network.createPartyNode(null)
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
}