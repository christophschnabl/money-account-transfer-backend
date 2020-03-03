package xyz.schnabl.event

import xyz.schnabl.transfer.dto.TransferResponse
import java.time.LocalDateTime
import java.util.UUID

/**
 * Defines a basic event
 * @property aggregateUUID : UUID   UUID of the aggregate the event belongs to
 * @property occurredAt : UUID      Timestamp of the event occurrence
 * @property versionNumber : Long   Version of aggregate during the occurrence
 */
interface Event {
    val aggregateUUID: UUID
    val occurredAt: LocalDateTime
    val versionNumber: Long

    fun toResponse(): TransferResponse
}