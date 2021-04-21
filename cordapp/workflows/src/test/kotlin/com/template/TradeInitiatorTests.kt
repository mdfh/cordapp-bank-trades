package com.template

import com.google.common.collect.ImmutableList
import com.template.contracts.TradeContract
import com.template.flows.TradeInfo
import com.template.flows.TradeInitResponder
import org.junit.Before
import com.template.flows.TradeInitiator
import com.template.states.TradeState
import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.*
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
            node?.registerInitiatedFlow(TradeInitResponder::class.java)
        }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `flow returns correctly formed partially signed transaction` () {
        val assignedTo = b.info.legalIdentities.first()
        val flow = TradeInitiator(TradeInfo(1000), assignedTo)
        val future = a.startFlow(flow)
        network.runNetwork()
        // Return the unsigned(!) SignedTransaction object from the TradeInProcessInitiator.
        val ptx: SignedTransaction = future.getOrThrow()
        // Check the transaction is well formed...
        // No outputs, one input TradeState and a command with the right properties.
        assert(ptx.tx.inputs.isEmpty())
        assert(ptx.tx.outputs.single().data is TradeState)
        val command = ptx.tx.commands.single()
        assert(command.value is TradeContract.Commands.Submit)
        ptx.verifySignaturesExcept(
                assignedTo.owningKey,
                network.defaultNotaryNode.info.legalIdentitiesAndCerts.first().owningKey
        )
    }

    @Test
    fun `flow returns transaction signed by both parties` () {
        val assignedTo = b.info.legalIdentities.first()
        val flow = TradeInitiator(TradeInfo(1000), assignedTo)
        val future = a.startFlow(flow)
        network.runNetwork()
        val stx = future.getOrThrow()
        stx.verifyRequiredSignatures()
    }

    @Test
    fun `flow records the same transaction in both party vaults` () {
        val assignedTo = b.info.legalIdentities.first()
        val flow = TradeInitiator(TradeInfo(1000), assignedTo)
        val future = a.startFlow(flow)
        network.runNetwork()
        val stx = future.getOrThrow()
        println("Signed transaction hash: ${stx.id}")
        listOf(a, b).map {
            it.services.validatedTransactions.getTransaction(stx.id)
        }.forEach {
            val txHash = (it as SignedTransaction).id
            println("$txHash == ${stx.id}")
            assertEquals(stx.id, txHash)
        }
    }
}