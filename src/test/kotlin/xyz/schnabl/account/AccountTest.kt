package xyz.schnabl.account

import org.junit.Test
import xyz.schnabl.account.model.Account
import xyz.schnabl.account.model.initAccountCreation
import xyz.schnabl.transfer.model.TransferDepositedEvent
import xyz.schnabl.transfer.model.TransferWithdrawnEvent
import java.util.UUID
import kotlin.test.assertEquals

class AccountTest {

    @Test
    fun `When an Empty Account is created Then the balance equals zero`() {
        val account = Account(UUID.randomUUID())
        assertEquals(account.getBalance(), 0)
        assertEquals(account.getEvents().size, 0)
    }

    @Test
    fun `When an Account Creation is initiated Then the event is applied`() {
        val account = Account(UUID.randomUUID(), 10).initAccountCreation()
        assertEquals(account.getEvents().size, 1)
        assertEquals(account.getBalance(), 10)
    }

    @Test
    fun `When an TransferWithdrawnEvent is applied Then the balance is decremented`() {
        val account = Account(UUID.randomUUID(), 10).initAccountCreation()
        account.applyAndAdd(
            TransferWithdrawnEvent(
                UUID.randomUUID(),
                4,
                account.getAggregateUuid(),
                versionNumber = account.getVersionNumber()
            )
        )
        assertEquals(account.getEvents().size, 2)
        assertEquals(account.getBalance(), 6)
    }

    @Test
    fun `When an TransferDepositedEvent is applied Then the balance is incremented`() {
        val account = Account(UUID.randomUUID(), 10).initAccountCreation()
        account.applyAndAdd(
            TransferDepositedEvent(
                UUID.randomUUID(),
                4,
                account.getAggregateUuid(),
                versionNumber = account.getVersionNumber()
            )
        )
        assertEquals(account.getEvents().size, 2)
        assertEquals(account.getBalance(), 14)
    }

}