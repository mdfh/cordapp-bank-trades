package com.template.states

import com.template.contracts.TradeContract
import com.template.schema.TradeSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.ConstructorForDeserialization
import java.lang.IllegalArgumentException
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(TradeContract::class)
data class TradeState(
    val amount: Int,
    val assignedBy: Party,
    val assignedTo: Party,
    val date: Date = Calendar.getInstance().time,
    val tradeStatus : TradeStatus = TradeStatus.SUBMITTED,
    override val participants: List<AbstractParty> = listOf(assignedBy, assignedTo),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : ContractState, LinearState, QueryableState {

    fun markInProcess(): TradeState {
        return copy(tradeStatus = TradeStatus.IN_PROCESS)
    }

    fun markSettled(): TradeState {
        return copy(tradeStatus = TradeStatus.SETTLED)
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return if (schema is TradeSchemaV1) {
            TradeSchemaV1.TradeModel(linearId.id, amount, date, tradeStatus)
        } else throw IllegalArgumentException("No supported schema found")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(TradeSchemaV1)
    }
}
