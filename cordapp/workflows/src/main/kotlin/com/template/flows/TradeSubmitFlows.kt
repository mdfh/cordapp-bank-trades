package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.TradeContract
import com.template.states.TradeState
import com.template.states.TradeStatus
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*

@InitiatingFlow
@StartableByRPC
class TradeInitiator(private val tradeInfo : TradeInfo, private val assignedTo : Party) : FlowLogic<SignedTransaction>()
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
        val output = TradeState(tradeInfo.amount, ourIdentity, assignedTo)

        // Create a new TransactionBuilder object.
        progressTracker.currentStep = GENERATING_TRANSACTION
        val builder = TransactionBuilder(notary)

        // Add the trade as an output state, as well as a command to the transaction builder.
        builder.addOutputState(output)
        val command = Command(TradeContract.Commands.Submit(), listOf(ourIdentity.owningKey, assignedTo.owningKey))
        builder.addCommand(command)

        println("Linear ID : ${output.linearId}")
        // Sign the transaction
        progressTracker.currentStep = SIGNING_TRANSACTION
        val signedTransaction = serviceHub.signInitialTransaction(builder)

        // Create session with counterparty
        progressTracker.currentStep = COUNTERPARTY_SESSION
        val otherPartySession = listOf(initiateFlow(assignedTo))

        val fullSignedTransaction = subFlow(CollectSignaturesFlow(signedTransaction, otherPartySession))
        // Finalise the transaction
        progressTracker.currentStep = FINALISING_TRANSACTION
        return subFlow(FinalityFlow(fullSignedTransaction, otherPartySession))
    }
}

@InitiatedBy(TradeInitiator::class)
class TradeInitResponder(private val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        try {
            println("Transaction received")
            val stx = subFlow(object : SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) {
                    println("Check Transaction received")
                    val ledgerTx = stx.toLedgerTransaction(serviceHub, false)
                    val assignedTo = ledgerTx.outputsOfType<TradeState>().single().assignedTo
                    if (assignedTo != ourIdentity) {
                        throw FlowException("Assigned to wrong person")
                    }
                }
            })
            subFlow(ReceiveFinalityFlow(otherSideSession  = counterpartySession, expectedTxId  = stx.id))
        } catch (e: Exception) {
            throw FlowException(e)
        }
    }
}

@CordaSerializable
class TradeInfo(val amount: Int)