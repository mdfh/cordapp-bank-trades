package com.template.schema
import com.template.states.TradeStatus
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*

/**
 * An object used to fully qualify the [TradeSchema] family name (i.e. independent of version).
 * https://crpdev.medium.com/liquibase-to-the-rescue-of-off-ledger-state-persistence-in-r3-corda-4-6-4366c2c585af
 */
object TradeSchema

object TradeSchemaV1 : MappedSchema(schemaFamily = TradeSchema.javaClass, version = 1,
    mappedTypes = listOf(TradeModel::class.java))
{
    @Entity
    @Table(name = "trade_model")
    class TradeModel(
        @Column(name = "id")
        @Type(type = "uuid-char")
        private val linearId: UUID,

        @Column(name = "amount")
        private val amount : Int,

        @Column(name = "date")
        private val date : Date,

        @Column(name = "status")
        @Enumerated(EnumType.STRING)
        private val status : TradeStatus

    ) : PersistentState() {
        // Default constructor required by hibernate.
        // constructor(): this(UUID.randomUUID(),,"")
    }

    override val migrationResource: String?
        get() = "trade.changelog-master"
}

