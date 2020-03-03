package xyz.schnabl.transfer

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import spark.Request
import xyz.schnabl.account.dto.AccountWithTransfersResponse
import xyz.schnabl.account.model.AccountCreatedEvent
import xyz.schnabl.readJson
import xyz.schnabl.transfer.model.CreateTransferCommand
import xyz.schnabl.transfer.model.TransferService
import xyz.schnabl.transfer.model.TransferWithdrawnEvent
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals


class TransferControllerTest {
    private class RequestStub(private val body: String = "", private val req_param: String? = null) : Request() {
        override fun body(): String {
            return body
        }

        override fun params(param: String?): String? {
            return req_param
        }
    }

    private val transfersAccountResponse = Gson().fromJson<AccountWithTransfersResponse>(
        readJson("/transfers/get/valid-transfer-response.json"),
        AccountWithTransfersResponse::class.java
    )
    private val created = transfersAccountResponse.createdAt ?: LocalDateTime.now()
    private val occurred = transfersAccountResponse.transfers[0].occurredAt

    private val exampleUUID = UUID.fromString("7f392356-0baf-424f-82e7-9523407f19fd")
    private val toUUID = UUID.fromString("4ea972d6-f23d-40e6-bf2a-86f1a97ae76a")
    private val exampleEvents = listOf(
        AccountCreatedEvent(10, toUUID, occurredAt = created, versionNumber = 1),
        TransferWithdrawnEvent(
            exampleUUID,
            -transfersAccountResponse.transfers[0].amount,
            toUUID,
            occurredAt = occurred,
            versionNumber = 2
        )
    )

    private val transferService = mock<TransferService> {
        on { processTransfer(any()) } doReturn TransferWithdrawnEvent(toUUID, 1, exampleUUID, LocalDateTime.now(), 2)
        on { getTransactionsForAccount(any()) } doReturn exampleEvents
    }

    private val transferController = TransferController(transferService)


    @Test(expected = IllegalArgumentException::class)
    fun `Given an empty request body When an transfer is created Then an Exception is thrown`() {
        val request = RequestStub()
        transferController.createTransfer(request)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `Given an invalid request body When an account is created Then an Exception is thrown`() {
        val request = RequestStub(readJson("/transfers/post/invalid-transfer.json"))
        transferController.createTransfer(request)
    }

    @Test
    fun `Given a valid request body Then a transfer is created`() {
        val json = readJson("/transfers/post/valid-transfer.json")
        val given = RequestStub(json)
        val actual = transferController.createTransfer(given)
        val expected = Gson().fromJson(json, CreateTransferCommand::class.java)

        assertEquals(expected.to.toString(), actual.otherAccount)
        assertEquals(expected.amount, -actual.amount)
    }

    @Test(expected = NullPointerException::class)
    fun `Given an request with no params When an account is queried Then an Exception is thrown`() {
        val request = RequestStub()
        transferController.getTransfersForAccount(request)
    }

    @Test
    fun `Given an request with a valid param When an account is queried Then a Transfer is returned`() {
        val given = RequestStub(req_param = toUUID.toString())
        val actual = transferController.getTransfersForAccount(given)

        assertEquals(transfersAccountResponse, actual)
    }
}