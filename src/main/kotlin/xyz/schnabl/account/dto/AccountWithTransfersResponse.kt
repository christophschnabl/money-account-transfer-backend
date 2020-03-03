package xyz.schnabl.account.dto

import xyz.schnabl.transfer.dto.TransferResponse
import java.time.LocalDateTime

data class AccountWithTransfersResponse(
    val accountUuid: String,
    val createdAt: LocalDateTime?,
    val transfers: List<TransferResponse>
)