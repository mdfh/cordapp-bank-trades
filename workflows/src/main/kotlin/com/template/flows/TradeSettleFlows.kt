package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.TradeContract
import com.template.states.TradeState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.lang.IllegalStateException
import java.util.*

@InitiatingFlow
@StartableByRPC
class TradeSettleInitiator(private val linearId : String) : FlowLogic<SignedTransaction>()
{
    private final val QUERY_STATE = ProgressTracker.Step("Query State")
    private final val RETREIVING_NOTARY = ProgressTracker.Step("Retrieving the notary")
    private final val GENERATING_TRANSACTION = ProgressTracker.Step("Generating transaction")
    private final val SIGNING_TRANSACTION = ProgressTracker.Step("Signing transaction with our private key")
    private final val COUNTERPARTY_SESSION = ProgressTracker.Step("Sending flow to counterparty")
    private final val FINALISING_TRANSACTION = ProgressTracker.Step("Obtaining notary signature and recording transaction")

    override val progressTracker = ProgressTracker(
        QUERY_STATE, RETREIVING_NOTARY, GENERATING_TRANSACTION, SIGNING_TRANSACTION, COUNTERPARTY_SESSION, FINALISING_TRANSACTION
    )

    @Suspendable
    override fun call() : SignedTransaction {
        // Query state
        progressTracker.currentStep = QUERY_STATE
        val q: QueryCriteria = QueryCriteria.LinearStateQueryCriteria(null, listOf(UUID.fromString(linearId)))
        val taskStatePage: Vault.Page<TradeState> = serviceHub.vaultService.queryBy(TradeState::class.java, q)
        val states: List<StateAndRef<TradeState>> = taskStatePage.states

        if(states.isEmpty())
            throw IllegalStateException("Cannot query state with linear ID")

        val currentStateAndRefTrade = states.last()
        val tradeState = currentStateAndRefTrade.state.data

        // Get a reference to the notary service on our network and our key pair.
        progressTracker.currentStep = RETREIVING_NOTARY
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        // Mark trade as InProcess and create new command
        val newTradeState = tradeState.markInProcess()
        val command = Command(
            TradeContract.Commands.Settle(), listOf(
            ourIdentity.owningKey))

        // Create transaction builder
        progressTracker.currentStep = GENERATING_TRANSACTION
        val builder = TransactionBuilder(notary)
            .addInputState(currentStateAndRefTrade)
            .addOutputState(newTradeState)
            .addCommand(command)

        // Verify and sign it with our KeyPair.
        progressTracker.currentStep = SIGNING_TRANSACTION
        builder.verify(serviceHub)
        val stx = serviceHub.signInitialTransaction(builder)

        // Send the state to the counterparty, and receive it back with their signature.
        val otherPartySession = initiateFlow(tradeState.assignedBy)

        // Finalise the transaction
        progressTracker.currentStep = FINALISING_TRANSACTION
        return subFlow(FinalityFlow(stx, listOf(otherPartySession)))
    }
}

@InitiatedBy(TradeSettleInitiator::class)
class TradeSettleResponder(private val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call() : SignedTransaction{
        println("Transaction received")
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}