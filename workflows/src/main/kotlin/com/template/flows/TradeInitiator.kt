package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.TradeContract
import com.template.states.TradeState
import com.template.states.TradeStatus
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*

@InitiatingFlow
@StartableByRPC
class TradeInitiator(private val amount : Int, private val date : Date) : FlowLogic<SignedTransaction>()
{
    override val progressTracker = ProgressTracker()

    private lateinit var assignedBy: Party
    private lateinit var assignedTo: Party

    @Suspendable
    override fun call() : SignedTransaction{
        // Step 1. Get a reference to the notary service on our network and our key pair.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        assignedBy = ourIdentity
        assignedTo = ourIdentity

        //Compose the Trade State
        val output = TradeState(amount = amount, date = date, assignedBy = assignedBy, assignedTo = assignedTo, tradeStatus = TradeStatus.SUBMITTED)

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary)

        // Step 4. Add the trade as an output state, as well as a command to the transaction builder.
        builder.addOutputState(output)
        builder.addCommand(TradeContract.Commands.Submit(), listOf(assignedBy.owningKey, assignedBy.owningKey))

        // Step 5. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        // Step 6. Assuming no exceptions, we can now finalise the transaction
        return subFlow(FinalityFlow(ptx, emptySet<FlowSession>()))
    }
}