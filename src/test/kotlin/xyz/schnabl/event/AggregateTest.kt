package xyz.schnabl.event

import org.junit.Test
import xyz.schnabl.account.model.Account
import xyz.schnabl.account.model.AccountCreatedEvent
import xyz.schnabl.transfer.model.TransferDepositedEvent
import xyz.schnabl.transfer.model.TransferWithdrawnEvent
import java.util.UUID
import kotlin.test.assertEquals


private val INITIAL_BASE_VERSION = 0L
private val EMPTY_LIST_COUNT = 0

class AggregateTest {

    @Test
    fun `Given a newly created Aggregate then the versionNumber equals zero`() {
        val given = Account(UUID.randomUUID())
        val actual = given.getVersionNumber()
        assertEquals(INITIAL_BASE_VERSION, actual)
    }

    @Test
    fun `Given a newly created Aggregate Then the event list is empty`() {
        val given = Account(UUID.randomUUID())
        val actual = given.getEvents().size
        assertEquals(EMPTY_LIST_COUNT, actual)
    }

    @Test
    fun `Given a newly created Aggregate When an event is applied Then the versionNumber equals one`() {
        val given = Account(UUID.randomUUID())
        assertEquals(INITIAL_BASE_VERSION, given.getVersionNumber())

        given.applyAndAdd(
            AccountCreatedEvent(
                aggregateUUID = given.getAggregateUuid(),
                versionNumber = given.getVersionNumber()
            )
        )

        assertEquals(INITIAL_BASE_VERSION, given.getEvents()[0].versionNumber)
        assertEquals(INITIAL_BASE_VERSION + 1, given.getVersionNumber())
    }

    @Test
    fun `Given a newly created Aggregate When an event is applied Then list is one element long`() {
        val given = Account(UUID.randomUUID())
        given.applyAndAdd(
            AccountCreatedEvent(
                aggregateUUID = given.getAggregateUuid(),
                versionNumber = given.getVersionNumber()
            )
        )

        val actual = given.getEvents().size
        assertEquals(1, actual)
    }

    @Test
    fun `Given a newly created Aggregate When multiple events are applied The the version equals n`() {
        val given = Account(UUID.randomUUID())
        given.applyAll(
            listOf(
                AccountCreatedEvent(aggregateUUID = given.getAggregateUuid(), versionNumber = given.getVersionNumber()),
                TransferWithdrawnEvent(
                    UUID.randomUUID(),
                    10,
                    given.getAggregateUuid(),
                    versionNumber = given.getVersionNumber()
                ),
                TransferDepositedEvent(
                    UUID.randomUUID(),
                    4,
                    given.getAggregateUuid(),
                    versionNumber = given.getVersionNumber()
                )
            )
        )
        val actual = given.getVersionNumber()

        assertEquals(3L, actual)
    }

    @Test
    fun `Given a newly created Aggregate When multiple events are applied The the list is n elements long`() {
        val given = Account(UUID.randomUUID())
        given.applyAll(
            listOf(
                AccountCreatedEvent(aggregateUUID = given.getAggregateUuid(), versionNumber = given.getVersionNumber()),
                TransferWithdrawnEvent(
                    UUID.randomUUID(),
                    10,
                    given.getAggregateUuid(),
                    versionNumber = given.getVersionNumber()
                ),
                TransferDepositedEvent(
                    UUID.randomUUID(),
                    4,
                    given.getAggregateUuid(),
                    versionNumber = given.getVersionNumber()
                )
            )
        )

        assertEquals(3, 3)
    }

}