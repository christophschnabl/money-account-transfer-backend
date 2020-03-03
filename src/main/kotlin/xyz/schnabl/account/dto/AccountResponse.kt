package xyz.schnabl.account.dto

import xyz.schnabl.account.model.Account
import java.util.UUID

/**
 * Response serialized and sent for account requests
 */
data class AccountResponse(
    val accountUuid: UUID,
    var balance: Long
)

fun Account.toResponse(): AccountResponse {
    return AccountResponse(getAggregateUuid(), getBalance())
}