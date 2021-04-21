package com.template

import com.google.common.collect.ImmutableList
import com.template.contracts.TradeContract
import com.template.flows.*
import com.template.states.TradeState
import net.corda.core.contracts.StateRef
import org.junit.Before
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.*
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TradeSettleTests {
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
        val startedNodes = arrayListOf(a, b)
        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach { it.registerInitiatedFlow(TradeInitResponder::class.java) }
        startedNodes.forEach { it.registerInitiatedFlow(TradeInProcessResponder::class.java) }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    /**
     * Issue a Trade on the ledger, we need to do this before marking the trade as InProcess.
     */
    private fun inProcessTrade(): SignedTransaction {
        val assignedTo = b.info.legalIdentities.first()
        val flow = TradeInitiator(TradeInfo(1000), assignedTo)
        val future = a.startFlow(flow)
        network.runNetwork()
        val stx =  future.getOrThrow()

        val tradeState = stx.tx.outputs.single().data as TradeState
        val inProcessFlow = TradeInProcessInitiator(TradeInProcessInfo(tradeState.linearId.id.toString()))
        val inProcessFuture = b.startFlow(inProcessFlow)
        network.runNetwork()
       return inProcessFuture.getOrThrow()
    }

    @Test
    fun `flow returns correctly formed partially signed transaction` () {
        val stx = inProcessTrade()
        val tradeState = stx.tx.outputs.single().data as TradeState
        val flow = TradeSettleInitiator(TradeInProcessInfo(tradeState.linearId.id.toString()))
        val future = b.startFlow(flow)
        network.runNetwork()
        val ptx = future.getOrThrow()
        // Check the transaction is well formed...
        // One output TradeState, one input state reference and a Transfer command with the right properties.
        assert(ptx.tx.inputs.size == 1)
        assert(ptx.tx.outputs.size == 1)
        assert(ptx.tx.inputs.single() == StateRef(stx.id, 0))
        val outputTrade = ptx.tx.outputs.single().data as TradeState
        println("Output state: $outputTrade")
        val command = ptx.tx.commands.single()
        assert(command.value is TradeContract.Commands.Settle)
        ptx.verifySignaturesExcept(b.info.chooseIdentityAndCert().party.owningKey, b.info.chooseIdentityAndCert().party.owningKey,
                network.defaultNotaryNode.info.legalIdentitiesAndCerts.first().owningKey)
    }

    @Test
    fun `flow returns transaction signed by required parties` () {
        val stx = inProcessTrade()
        val inputTrade = stx.tx.outputs.single().data as TradeState
        val flow = TradeSettleInitiator(TradeInProcessInfo(inputTrade.linearId.id.toString()))
        val future = b.startFlow(flow)
        network.runNetwork()
        val ptx = future.getOrThrow()
        ptx.verifyRequiredSignatures()
    }

    @Test
    fun `flow records the same transaction in both party vaults` () {
        val stx = inProcessTrade()
        val inputTrade = stx.tx.outputs.single().data as TradeState
        val flow = TradeSettleInitiator(TradeInProcessInfo(inputTrade.linearId.id.toString()))
        val future = b.startFlow(flow)
        network.runNetwork()
        val ptx = future.getOrThrow()
        listOf(a, b).map {
            it.services.validatedTransactions.getTransaction(ptx.id)
        }.forEach {
            val txHash = (it as SignedTransaction).id
            println("$txHash == ${ptx.id}")
            assertEquals(ptx.id, txHash)
        }
    }
}