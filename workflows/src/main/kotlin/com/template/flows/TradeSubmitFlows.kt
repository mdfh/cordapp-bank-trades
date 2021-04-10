package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.TradeContract
import com.template.states.TradeState
import com.template.states.TradeStatus
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*

@InitiatingFlow
@StartableByRPC
class TradeInitiator(private val amount : Int, private val assignedTo : Party) : FlowLogic<SignedTransaction>()
{
    private final val RETREIVING_NOTARY = ProgressTracker.Step("Retrieving the notary")
    private final val GENERATING_TRANSACTION = ProgressTracker.Step("Generating transaction")
    private final val SIGNING_TRANSACTION = ProgressTracker.Step("Signing transaction with our private key")
    private final val COUNTERPARTY_SESSION = ProgressTracker.Step("Sending flow to counterparty")
    private final val FINALISING_TRANSACTION = ProgressTracker.Step("Obtaining notary signature and recording transaction")

    override val progressTracker = ProgressTracker(
        RETREIVING_NOTARY, GENERATING_TRANSACTION, SIGNING_TRANSACTION, COUNTERPARTY_SESSION, FINALISING_TRANSACTION
    )

    @Suspendable
    override fun call() : SignedTransaction{
        // Get a reference to the notary service on our network and our key pair.
        progressTracker.currentStep = RETREIVING_NOTARY
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        //Compose the Trade State
        val output = TradeState(amount, ourIdentity, assignedTo)

        // Create a new TransactionBuilder object.
        progressTracker.currentStep = GENERATING_TRANSACTION
        val builder = TransactionBuilder(notary)

        // Add the trade as an output state, as well as a command to the transaction builder.
        builder.addOutputState(output)
        val command = Command(TradeContract.Commands.Submit(), ourIdentity.owningKey)
        builder.addCommand(command)

        println("Linear ID : ${output.linearId}")
        // Sign the transaction
        progressTracker.currentStep = SIGNING_TRANSACTION
        val signedTransaction = serviceHub.signInitialTransaction(builder)

        // Create session with counterparty
        progressTracker.currentStep = COUNTERPARTY_SESSION
        val otherPartySession = initiateFlow(assignedTo)

        // Finalise the transaction
        progressTracker.currentStep = FINALISING_TRANSACTION
        return subFlow(FinalityFlow(signedTransaction, otherPartySession))
    }
}

@InitiatedBy(TradeInitiator::class)
class TradeInitResponder(private val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call() : SignedTransaction{
        println("Transaction received")
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}