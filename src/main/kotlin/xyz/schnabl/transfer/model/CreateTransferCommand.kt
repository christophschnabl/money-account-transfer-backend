package xyz.schnabl.transfer.model

import java.util.UUID

/**
 *   Specifies the command to initiate a transfer
 *   @property from : UUID  UUID of the sender
 *   @property to : UUID    UUID of the recipient
 *   @property amount : Long    Amount to be transferred from sender to recipient
 */
data class CreateTransferCommand(
    val from: UUID,
    val to: UUID,
    val amount: Long
)