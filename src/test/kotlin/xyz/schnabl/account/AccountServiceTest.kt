package xyz.schnabl.account

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import xyz.schnabl.account.model.AccountCreatedEvent
import xyz.schnabl.account.model.CreateAccountCommand
import xyz.schnabl.event.Event
import xyz.schnabl.event.EventAggregate
import xyz.schnabl.event.EventDataStore
import xyz.schnabl.event.LockingException
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull


private val RANDOM_UUID = UUID.randomUUID()
private val OTHER_UUID = UUID.randomUUID()

class AccountServiceTest {

    @Before
    fun setUp() {
        assertNotEquals(
            RANDOM_UUID,
            OTHER_UUID
        )
    }

    private val eventDataStore = mock<EventDataStore> {
        on { get(RANDOM_UUID) } doReturn listOf<Event>(
            AccountCreatedEvent(
                aggregateUUID = RANDOM_UUID,
                versionNumber = 0
            )
        )
        on { get(OTHER_UUID) } doReturn emptyList<Event>()
    }

    private val duplicateUUIDStore = mock<EventDataStore> {
        on { store(any<EventAggregate>()) } doAnswer { throw LockingException("Duplicate UUIDs huh?") }
    }

    private val accountService = AccountServiceImpl(eventDataStore)

    @Test
    fun `Given a CreateAccountCommand Then an Account is created and stored successfully`() {
        val expectedBalance = 42L
        val createAccountCommand = CreateAccountCommand(expectedBalance)
        val actualAccount = accountService.processAccountCreation(createAccountCommand)

        assertEquals(expectedBalance, actualAccount.getBalance())
        val expected = EventAggregate(actualAccount.getAggregateUuid(), actualAccount.getEvents(), 0)
        verify(eventDataStore).store(eq(expected))
    }

    @Test
    fun `Given I want to retrieve an Account by Uuid then the Account is returned`() {
        val actualAccount = accountService.getAccount(RANDOM_UUID)

        assertNotNull(actualAccount)
        assertEquals(actualAccount.getBalance(), 0)
        assertEquals(actualAccount.getAggregateUuid(), RANDOM_UUID)
    }

    @Test(expected = AccountNotFoundException::class)
    fun `Given I want to retrieve an unknown Account by Uuid then the account is not found`() {
        accountService.getAccount(OTHER_UUID)
    }

    @Test(expected = IllegalStateException::class)
    fun `Given magically the UUID was already generated then the account can not be created`() {
        val uuidAccountService = AccountServiceImpl(duplicateUUIDStore)
        uuidAccountService.processAccountCreation(CreateAccountCommand(0))
    }

    @Test
    fun `Given I want to retrieve the Events for an Account Then all Events are returned`() {
        val events = accountService.getEventsForAccount(RANDOM_UUID)
        assertEquals(events.size, 1)
        assertEquals(events[0].aggregateUUID, RANDOM_UUID)
    }

}