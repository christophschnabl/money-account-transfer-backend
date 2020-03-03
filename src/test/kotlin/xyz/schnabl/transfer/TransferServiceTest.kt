package xyz.schnabl.transfer

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.Before
import org.junit.Test
import xyz.schnabl.account.AccountNotFoundException
import xyz.schnabl.account.AccountServiceImpl
import xyz.schnabl.account.InsufficientBalanceException
import xyz.schnabl.account.model.AccountCreatedEvent
import xyz.schnabl.event.Event
import xyz.schnabl.event.EventDataStore
import xyz.schnabl.transfer.model.CreateTransferCommand
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


private val RANDOM_UUID = UUID.randomUUID()
private val OTHER_UUID = UUID.randomUUID()
private val ACCOUNT_UUID = UUID.randomUUID()

class TransferServiceTest {

    private val eventDataStore = mock<EventDataStore> {
        on { get(RANDOM_UUID) } doReturn listOf<Event>(
            AccountCreatedEvent(
                aggregateUUID = RANDOM_UUID,
                versionNumber = 0
            )
        )
        on { get(ACCOUNT_UUID) } doReturn listOf<Event>(
            AccountCreatedEvent(
                aggregateUUID = ACCOUNT_UUID,
                initialBalance = 43,
                versionNumber = 0
            )
        )
        on { get(OTHER_UUID) } doReturn emptyList<Event>()
    }

    private val accountService = AccountServiceImpl(eventDataStore)
    private val transferService =
        TransferServiceImpl(eventDataStore, accountService)

    @Before
    fun setUp() {
        assertNotEquals(RANDOM_UUID, OTHER_UUID)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given a transfer When the amount is less than one Then the transfer fails`() {
        val command = CreateTransferCommand(RANDOM_UUID, OTHER_UUID, 0)
        transferService.processTransfer(command)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Given a transfer When the recipient equals the sender Then the transfer fails`() {
        val command = CreateTransferCommand(RANDOM_UUID, RANDOM_UUID, 1)
        transferService.processTransfer(command)
    }

    @Test(expected = AccountNotFoundException::class)
    fun `Given a transfer When one account does not exist Then the transfer fails`() {
        val command = CreateTransferCommand(RANDOM_UUID, OTHER_UUID, 42)
        transferService.processTransfer(command)
    }

    @Test(expected = InsufficientBalanceException::class)
    fun `Given a transfer When the sender account has an insufficient balance Then the transfer fails`() {
        val command = CreateTransferCommand(RANDOM_UUID, ACCOUNT_UUID, 42)
        transferService.processTransfer(command)
    }


    @Test
    fun `Given a transfer When it is valid Then the transfer succeeds`() {
        val command = CreateTransferCommand(ACCOUNT_UUID, RANDOM_UUID, 42)
        val transferWithdrawnEvent = transferService.processTransfer(command)
        assertEquals(transferWithdrawnEvent.amount, 42)
        assertEquals(transferWithdrawnEvent.toUUID, RANDOM_UUID)
        assertEquals(transferWithdrawnEvent.aggregateUUID, ACCOUNT_UUID)
        assertEquals(transferWithdrawnEvent.versionNumber, 1)
    }
}