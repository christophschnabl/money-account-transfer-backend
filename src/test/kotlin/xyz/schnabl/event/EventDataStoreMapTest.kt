package xyz.schnabl.event

import org.junit.Assert.assertEquals
import org.junit.Test
import xyz.schnabl.account.model.Account
import xyz.schnabl.account.model.initAccountCreation
import xyz.schnabl.transfer.model.TransferDepositedEvent
import java.time.LocalDateTime
import java.util.UUID
import kotlin.concurrent.thread

class EventDataStoreMapTest {

    @Test(expected = LockingException::class)
    fun `Given a transfer When it conflicts existing events Then the transfer fails`() {
        val store: EventDataStore = EventDataStoreMap()

        val account = Account(UUID.randomUUID())
        assertEquals(account.getVersionNumber(), 0)
        val a = account.initAccountCreation()
        assertEquals(account.getVersionNumber(), 1)

        val uuid = UUID.randomUUID()

        val expected = TransferDepositedEvent(uuid, 10, a.getAggregateUuid(), LocalDateTime.now(), a.getVersionNumber())
        a.applyAndAdd(expected)
        assertEquals(a.getVersionNumber(), 2)

        store.store(EventAggregate(account.getAggregateUuid(), account.getEvents(), account.getVersionNumber()))

        val events = store.get(a.getAggregateUuid())
        val deposited = events[1]
        assertEquals(expected, deposited)

        val b = Account(account.getAggregateUuid())
        b.applyAll(store.get(account.getAggregateUuid()))
        assertEquals(b.getVersionNumber(), 2)

        b.applyAndAdd(
            TransferDepositedEvent(
                UUID.randomUUID(),
                10,
                b.getAggregateUuid(),
                versionNumber = b.getVersionNumber()
            )
        )
        assertEquals(b.getVersionNumber(), 3)

        store.store(EventAggregate(b.getAggregateUuid(), b.getEvents(), b.getVersionNumber()))

        a.applyAndAdd(
            TransferDepositedEvent(
                UUID.randomUUID(),
                1,
                a.getAggregateUuid(),
                versionNumber = a.getVersionNumber()
            )
        )
        store.store(
            EventAggregate(
                a.getAggregateUuid(),
                a.getEvents(),
                a.getVersionNumber()
            )
        ) // a is an older version of the aggregate and fails
    }


    @Test
    fun `Given two concurrent transfers When they conflict existing events Then the transfer fails`() {
        val store: EventDataStore = EventDataStoreMap()

        // INIT

        val account = Account(UUID.randomUUID()).initAccountCreation()

        store.store(EventAggregate(account.getAggregateUuid(), account.getEvents(), account.getVersionNumber()))


        // A
        thread(start = true) {
            val a = Account(account.getAggregateUuid())
            a.applyAll(store.get(account.getAggregateUuid()))
            a.applyAndAdd(
                TransferDepositedEvent(
                    UUID.randomUUID(),
                    10,
                    a.getAggregateUuid(),
                    versionNumber = a.getVersionNumber()
                )
            )

            Thread.yield()

            store.store(EventAggregate(a.getAggregateUuid(), a.getEvents(), a.getVersionNumber()))
        }

        // B
        thread(start = true) {
            val b = Account(account.getAggregateUuid())
            b.applyAll(store.get(account.getAggregateUuid()))
            b.applyAndAdd(
                TransferDepositedEvent(
                    UUID.randomUUID(),
                    20,
                    b.getAggregateUuid(),
                    versionNumber = b.getVersionNumber()
                )
            )
            b.applyAndAdd(
                TransferDepositedEvent(
                    UUID.randomUUID(),
                    30,
                    b.getAggregateUuid(),
                    versionNumber = b.getVersionNumber()
                )
            )
            store.store(EventAggregate(b.getAggregateUuid(), b.getEvents(), b.getVersionNumber()))
        }

    }
}