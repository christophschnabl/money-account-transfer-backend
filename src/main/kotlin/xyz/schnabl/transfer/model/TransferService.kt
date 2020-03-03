package xyz.schnabl.transfer.model

import xyz.schnabl.event.Event
import java.util.UUID

interface TransferService {
    /**
     * Processes the transfer from one account to another
     * @param command: CreateTransferCommand   command to initiate transfer
     * @return TransferWithdrawnEvent from the sender's perspective
     */
    fun processTransfer(command: CreateTransferCommand): TransferWithdrawnEvent


    /**
     * Returns all transactions for an account
     * @param accountUuid: Request uuid for the account
     * @return AccountWithTransferResponse  account with list of transactions
     */
    fun getTransactionsForAccount(accountUuid: UUID): List<Event>
}