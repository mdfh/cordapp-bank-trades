package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction

@InitiatedBy(TradeInProcessInitiator::class)
class TradeSettleResponder     //Constructor
    (  //private variable
    private val counterpartySession: FlowSession
) : FlowLogic<SignedTransaction>() {
    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        val signedTransaction = subFlow(object : SignTransactionFlow(counterpartySession) {
            @Suspendable
            @Throws(FlowException::class)
            override fun checkTransaction(stx: SignedTransaction) {
                /*
                 * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
                 * However, just because a transaction is contractually valid doesn’t mean we necessarily want to sign.
                 * What if we don’t want to deal with the counterparty in question, or the value is too high,
                 * or we’re not happy with the transaction’s structure? checkTransaction
                 * allows us to define these additional checks. If any of these conditions are not met,
                 * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
                 * ----------
                 * For this hello-world cordapp, we will not implement any aditional checks.
                 * */
            }
        })
        //Stored the transaction into data base.
        return subFlow(ReceiveFinalityFlow(counterpartySession, signedTransaction.id))
    }
}