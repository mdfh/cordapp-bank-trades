package com.template.contracts

import com.template.states.TradeState
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
            is Commands.Submitted -> requireThat {  }
            is Commands.InProcess -> requireThat {  }
            is Commands.Settled -> requireThat {  }
            else -> throw IllegalArgumentException()
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Submitted : Commands
        class InProcess : Commands
        class Settled : Commands
    }
}