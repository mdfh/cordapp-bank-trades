package com.template.contracts

import com.template.states.TradeState
import com.template.states.TradeStatus
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.lang.IllegalArgumentException

// ************
// * Contract *
// ************
class TradeContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.TradeContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>();
        val outputs = tx.outputsOfType<TradeState>()
        val inputs = tx.inputsOfType<TradeState>()

        when(command.value)
        {
            is Commands.Submit -> requireThat {
                "There is an input state" using (inputs.isEmpty())
                "There is more than one output state" using (outputs.size == 1)
                "Issuer cannot issue to himself" using (outputs.first().assignedBy.owningKey != outputs.first().assignedTo.owningKey)
                "Trade amount is zero" using (outputs.first().amount != 0)
                "Trade status should be Submitted" using (outputs.first().tradeStatus == TradeStatus.SUBMITTED)
            }
            is Commands.InProcess -> requireThat {
                "There is more that one input state" using (inputs.size == 1)
                "There is more that one output state" using (outputs.size == 1)
                "Trade status should be InProcess" using (outputs.first().tradeStatus == TradeStatus.IN_PROCESS)
            }
            is Commands.Settle -> requireThat {
                "There is more that one input state" using (inputs.size == 2)
                "There is more that one output state" using (outputs.size == 1)
                "Trade status should be InProcess" using (outputs.first().tradeStatus == TradeStatus.SUBMITTED)
            }
            else -> throw IllegalArgumentException()
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Submit : Commands
        class InProcess : Commands
        class Settle : Commands
    }
}