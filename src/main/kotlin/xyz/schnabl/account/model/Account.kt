package xyz.schnabl.account.model

import xyz.schnabl.event.Aggregate
import xyz.schnabl.event.Event
import xyz.schnabl.transfer.model.TransferDepositedEvent
import xyz.schnabl.transfer.model.TransferWithdrawnEvent
import java.util.UUID

/**
 * Aggregate representing an account
 * @property aggregateUuid: UUID  Unique Identifier for this aggregate
 * @property balance: Long  Current calculated balance
 */
class Account(aggregateUuid: UUID, private var balance: Long = 0) : Aggregate(aggregateUuid) {

    override fun apply(event: Event) {
        when (event) {
            is AccountCreatedEvent -> apply(event)
            is TransferDepositedEvent -> apply(event)
            is TransferWithdrawnEvent -> apply(event)
        }
    }

    private fun apply(accountCreatedEvent: AccountCreatedEvent) {
        balance = accountCreatedEvent.initialBalance
    }

    private fun apply(transferDepositedEvent: TransferDepositedEvent) {
        balance += transferDepositedEvent.amount
    }

    private fun apply(transferWithdrawnEvent: TransferWithdrawnEvent) {
        balance -= transferWithdrawnEvent.amount
    }

    fun getBalance(): Long {
        return balance
    }
}

fun Account.initAccountCreation(): Account {
    applyAndAdd(
        AccountCreatedEvent(
            getBalance(),
            getAggregateUuid(),
            versionNumber = getVersionNumber()
        )
    )
    return this
}