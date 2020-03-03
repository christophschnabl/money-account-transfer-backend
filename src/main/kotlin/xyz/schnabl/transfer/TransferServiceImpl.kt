package xyz.schnabl.transfer

import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.google.inject.Singleton
import xyz.schnabl.account.InsufficientBalanceException
import xyz.schnabl.account.model.Account
import xyz.schnabl.account.model.AccountService
import xyz.schnabl.event.Event
import xyz.schnabl.event.EventAggregate
import xyz.schnabl.event.EventDataStore
import xyz.schnabl.transfer.model.CreateTransferCommand
import xyz.schnabl.transfer.model.TransferDepositedEvent
import xyz.schnabl.transfer.model.TransferService
import xyz.schnabl.transfer.model.TransferWithdrawnEvent
import java.util.UUID

/**
 * The TransferService is responsible for processing TransferCommands and persisting withdrawal and deposit events
 * @property eventDataStore : EventDataStore    store to persist events
 * @property accountService : AccountService    to get accounts by id
 */
@Singleton
class TransferServiceImpl @Inject constructor(
    private val eventDataStore: EventDataStore,
    private val accountService: AccountService
) : TransferService {

    override fun processTransfer(command: CreateTransferCommand): TransferWithdrawnEvent {
        if (command.amount < 1) {
            throw IllegalArgumentException("Amount must be greater than 0")
        }

        if (command.from == command.to) {
            throw IllegalArgumentException("Cannot transfer to the same account ID: ${command.from}.")
        }

        val sender: Account = accountService.getAccount(command.from)
        val recipient: Account = accountService.getAccount(command.to)

        if (command.amount > sender.getBalance()) {
            throw InsufficientBalanceException("Account from has an insufficient balance with id ${command.from}.")
        }

        val withdrawnTransferFromAccount = TransferWithdrawnEvent(
            recipient.getAggregateUuid(),
            command.amount,
            sender.getAggregateUuid(),
            versionNumber = sender.getVersionNumber()
        )

        val depositedTransferToAccount = TransferDepositedEvent(
            sender.getAggregateUuid(),
            command.amount,
            recipient.getAggregateUuid(),
            versionNumber = recipient.getVersionNumber()
        )

        sender.applyAndAdd(withdrawnTransferFromAccount)
        recipient.applyAndAdd(depositedTransferToAccount)

        eventDataStore.store(
            listOf(
                EventAggregate(
                    sender.getAggregateUuid(),
                    ImmutableList.copyOf(sender.getEvents()),
                    sender.getVersionNumber()
                ),
                EventAggregate(
                    recipient.getAggregateUuid(),
                    ImmutableList.copyOf(recipient.getEvents()),
                    recipient.getVersionNumber()
                )
            )
        )

        return withdrawnTransferFromAccount
    }


    override fun getTransactionsForAccount(accountUuid: UUID): List<Event> {
        return accountService.getEventsForAccount(accountUuid)
    }
}