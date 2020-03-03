package xyz.schnabl.transfer

import com.google.gson.Gson
import com.google.inject.Inject
import com.google.inject.Singleton
import spark.Request
import xyz.schnabl.UUID_PARAMETER
import xyz.schnabl.account.dto.AccountWithTransfersResponse
import xyz.schnabl.transfer.dto.TransferResponse
import xyz.schnabl.transfer.model.CreateTransferCommand
import xyz.schnabl.transfer.model.TransferService
import java.util.UUID


/**
 * Responsible for creating transfers between accounts and returning transfers for a given account
 * @property transferService : TransferService  Service to interact with transfers
 */
@Singleton
class TransferController @Inject constructor(private val transferService: TransferService) {
    /**
     * Creates a transfer between the two specified accounts
     * @param request : Request   Request with the two accounts and amount in the body
     * @return TransferResponse   The created transfer from the view of the from user
     */
    fun createTransfer(request: Request): TransferResponse {
        val body = request.body()

        if (body.isEmpty()) {
            throw IllegalArgumentException("Request body must not be empty for a transfer")
        }

        val createTransferCommand = Gson()
            .fromJson(body, CreateTransferCommand::class.java)

        return transferService
            .processTransfer(createTransferCommand)
            .toResponse()
    }

    /**
     * Retrieves all transfers for a given account
     * @param request : Request   Request with the two accounts and amount in the body
     * @return AccountWithTransferResponse  Returns the account with all its transfers sorted by date
     */
    fun getTransfersForAccount(request: Request): AccountWithTransfersResponse {
        val accountUuid = request.params(UUID_PARAMETER)
        val events = transferService.getTransactionsForAccount(UUID.fromString(accountUuid))

        val transfers = events
            .map { event -> event.toResponse() }
            .sortedBy { it.occurredAt }

        return AccountWithTransfersResponse(
            accountUuid,
            transfers.findLast { it.amount == 0L }?.occurredAt,
            transfers.filter { it.amount != 0L }
        )
    }
}