package xyz.schnabl.transfer.dto

import java.time.LocalDateTime

data class TransferResponse(
    val amount: Long,
    val occurredAt: LocalDateTime,
    val otherAccount: String
)