package xyz.schnabl.account

import com.google.inject.Inject
import com.google.inject.Singleton
import xyz.schnabl.account.model.Account
import xyz.schnabl.account.model.AccountService
import xyz.schnabl.account.model.CreateAccountCommand
import xyz.schnabl.account.model.initAccountCreation
import xyz.schnabl.event.Event
import xyz.schnabl.event.EventAggregate
import xyz.schnabl.event.EventDataStore
import xyz.schnabl.event.LockingException
import java.util.UUID

/**
 * Service used to create and retrieve accounts
 * @property eventDataStore : EventDataStore    Store for the events to be stored in
 */
@Singleton
class AccountServiceImpl @Inject constructor(private val eventDataStore: EventDataStore) :
    AccountService {

    override fun processAccountCreation(command: CreateAccountCommand): Account {
        if (command.balance < 0) {
            throw IllegalArgumentException("Cannot initialize account with negative balance")
        }

        val account = Account(UUID.randomUUID(), command.balance).initAccountCreation()

        try {
            eventDataStore.store(EventAggregate(account.getAggregateUuid(), account.getEvents(), 0))
        } catch (exception: LockingException) {
            throw IllegalStateException("Account with this UUID already exists.")  // I'll buy you a coffee if this really happens
        }

        return account
    }

    override fun getAccount(accountUuid: UUID): Account {
        val account = Account(accountUuid)
        account.applyAll(getEventsForAccount(accountUuid))
        return account
    }

    override fun getEventsForAccount(accountUuid: UUID): List<Event> {
        val accountEvents = eventDataStore.get(accountUuid)
        return accountEvents.ifEmpty {
            throw AccountNotFoundException("Account does not exist with id $accountUuid")
        }
    }
}