package xyz.schnabl.account.model

import xyz.schnabl.event.Event
import java.util.UUID

interface AccountService {

    /**
     * Creates a new account, stores and returns it
     * @param command: CreateAccountCommand  Command specifying an Account creation
     * @return Account  Newly created account
     */
    fun processAccountCreation(command: CreateAccountCommand): Account


    /**
     * Loads an account from the store, applies all events and returns the aggregated account.
     * @param accountUuid : UUID    Unique identifier for the account to be loaded
     * @throws AccountNotFoundException
     * @return Account  Loaded account with all events applied to it
     */
    fun getAccount(accountUuid: UUID): Account


    /**
     * Loads all events for an account from the store
     * Throws an Exception if the account could not be found
     * @param accountUuid : UUID    Unique identifier for the account to be loaded
     * @throws AccountNotFoundException
     * @return List<Event>  List of all events for this account
     */
    fun getEventsForAccount(accountUuid: UUID): List<Event>
}