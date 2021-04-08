package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.TradeContract
import com.template.states.TradeState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.*

@InitiatingFlow
@StartableByRPC
class TradeInProcessInitiator(private val linearId : String, private val assignToName : String) : FlowLogic<SignedTransaction>()
{
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() : SignedTransaction {
        val q: QueryCriteria = QueryCriteria.LinearStateQueryCriteria(null, listOf(UUID.fromString(linearId)))
        System.out.println(q);
        val taskStatePage: Vault.Page<TradeState> = serviceHub.vaultService.queryBy(TradeState::class.java, q)
        val states: List<StateAndRef<TradeState>> = taskStatePage.states
        val currentStateAndRefToDo = states[0]
        val toDoState = currentStateAndRefToDo.state.data

        // Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        val sender = ourIdentity
        val parties = serviceHub.identityService.partiesFromName(assignToName, true)
        val receiver = parties.iterator().next()
        val newTodoState = toDoState.assign(receiver)
        val builder = TransactionBuilder(notary)
            .addInputState(currentStateAndRefToDo)
            .addOutputState(newTodoState)
            .addCommand(TradeContract.Commands.InProcess(), sender.owningKey, receiver.owningKey)


        // Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        val assignedToSession = initiateFlow(receiver)
        return subFlow(CollectSignaturesFlow(ptx, listOf(assignedToSession)))
    }
}