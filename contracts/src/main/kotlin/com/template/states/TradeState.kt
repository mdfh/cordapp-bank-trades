package com.template.states

import com.template.contracts.TradeContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.ConstructorForDeserialization
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(TradeContract::class)
data class TradeState(
    val amount: Int,
    val date: Date,
    val assignedBy: Party,
    val assignedTo: Party,
    val tradeStatus : TradeStatus,
    override val participants: List<AbstractParty> = listOf(assignedBy, assignedTo),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : ContractState, LinearState {

   /* @ConstructorForDeserialization
    constructor(
        assignedBy: Party,
        date: Date,
        assignedTo: Party,
        amount: Int,
        linearId: UniqueIdentifier
    ) : this(amount, date, assignedBy, assignedTo, linearId = linearId)*/

    fun markInProcess(): TradeState {
        return copy(tradeStatus = TradeStatus.IN_PROCESS)
    }

    fun markSettled(): TradeState {
        return copy(tradeStatus = TradeStatus.SETTLED)
    }
}
