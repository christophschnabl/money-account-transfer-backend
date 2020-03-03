package xyz.schnabl.transfer.model

import xyz.schnabl.event.Event
import xyz.schnabl.transfer.dto.TransferResponse
import java.time.LocalDateTime
import java.util.UUID

/**
 * Represents the event of a withdrawal during a transfer
 * @property toUUID : UUID  The recipient's UUID
 * @property amount : Long  The amount that is withdrawn
 */
class TransferWithdrawnEvent(
    val toUUID: UUID,
    val amount: Long,
    override val aggregateUUID: UUID,
    override val occurredAt: LocalDateTime = LocalDateTime.now(),
    override val versionNumber: Long
) : Event {

    override fun toResponse(): TransferResponse {
        return TransferResponse(-amount, occurredAt, toUUID.toString())
    }
}