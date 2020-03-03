package xyz.schnabl.account.model

import xyz.schnabl.event.Event
import xyz.schnabl.transfer.dto.TransferResponse
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents the event of an AccountCreation
 * @property initialBalance : Long  The amount of money an account is initialised with
 */
data class AccountCreatedEvent(
    val initialBalance: Long = 0,
    override val aggregateUUID: UUID,
    override val occurredAt: LocalDateTime = LocalDateTime.now(),
    override val versionNumber: Long
) : Event {

    override fun toResponse(): TransferResponse {
        return TransferResponse(0L, occurredAt, "")
    }
}