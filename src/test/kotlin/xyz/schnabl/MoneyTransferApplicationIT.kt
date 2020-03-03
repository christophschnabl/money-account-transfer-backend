package xyz.schnabl

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.eclipse.jetty.http.HttpStatus
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import spark.Spark.awaitInitialization
import spark.Spark.stop
import xyz.schnabl.account.dto.AccountResponse
import xyz.schnabl.account.dto.AccountWithTransfersResponse
import java.util.UUID
import kotlin.test.assertEquals


class MoneyTransferApplicationIT {
    private val client = OkHttpClient()
    private val port = 4567
    private val url = "http://localhost:$port"

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            main()
            awaitInitialization()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            stop()
        }
    }


    /*
     * POST /ACCOUNTS
     */

    @Test
    fun `Given a negative balance in the request body When a POST accounts request is invoked Then a 400 Bad Request is returned`() {
        val body = requestBodyFromJson("/accounts/post/post-account-negative-balance.json")
        val expectedResponse = readJson("/accounts/post/post-account-negative-balance-response.json")

        val request = Request.Builder().url("$url/$ACCOUNTS_RESOURCE").post(body).build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.BAD_REQUEST_400)
        assertEquals(response.body?.string(), expectedResponse)
    }

    @Test
    fun `Given an empty request body When a POST accounts request is invoked Then a 400 Bad Request is returned`() {
        val body = requestBodyFromJson("/accounts/post/post-account-empty-body.json")
        val expectedResponse = readJson("/accounts/post/post-account-empty-body-response.json")

        val request = Request.Builder().url("$url/$ACCOUNTS_RESOURCE").post(body).build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.BAD_REQUEST_400)
        assertEquals(response.body?.string(), expectedResponse)
    }

    @Test
    fun `Given an invalid json request body When a POST accounts request is invoked Then a 400 Bad Request is returned`() {
        val body = requestBodyFromJson("/accounts/post/post-account-invalid-json-body.json")
        val expectedResponse = readJson("/accounts/post/post-account-invalid-json-body-response.json")

        val request = Request.Builder().url("$url/$ACCOUNTS_RESOURCE").post(body).build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.BAD_REQUEST_400)
        assertEquals(response.body?.string(), expectedResponse)
    }

    @Test
    fun `Given a correct request body When a POST accounts request is invoked Then a 201 Created is returned`() {
        val body = requestBodyFromJson("/accounts/post/post-account.json")

        val request = Request.Builder().url("$url/$ACCOUNTS_RESOURCE").post(body).build()
        val response = client.newCall(request).execute()

        val expected = Gson().fromJson(response.body?.string(), AccountResponse::class.java)

        assertEquals(response.code, HttpStatus.CREATED_201)
        assertEquals(expected.balance, 42)
    }


    /*
     * GET /ACCOUNTS
     */

    @Test
    fun `Given an UUID without an account as request param When a GET accounts request is invoked Then a 404 Not found is returned`() {
        val expectedResponse = readJson("/accounts/get/get-account-does-not-exist.json")

        val request =
            Request.Builder().url("$url/$ACCOUNTS_RESOURCE/4e865656-7eec-4058-8359-4e6853014c6f").get().build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.NOT_FOUND_404)
        assertEquals(response.body?.string(), expectedResponse)
    }

    @Test
    fun `Given an invalid UUID as request param When a GET accounts request is invoked Then a 400 Bad Request is returned`() {
        val expectedResponse = readJson("/accounts/get/get-account-invalid-uuid.json")

        val request = Request.Builder().url("$url/$ACCOUNTS_RESOURCE/976ed35682674ba789bb60431770c0ed").get().build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.BAD_REQUEST_400)
        assertEquals(response.body?.string(), expectedResponse)
    }

    @Test
    fun `Given an empty id request param When a GET accounts request is invoked Then a 404 Not found is returned`() {
        val request = Request.Builder().url("$url/$ACCOUNTS_RESOURCE/").get().build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.NOT_FOUND_404)
    }

    @Test
    fun `Given an existing account When a GET accounts request is invoked Then a 200 OK is returned`() {
        // create account here
        val body = requestBodyFromJson("/accounts/post/post-account.json")

        val createRequest = Request.Builder().url("$url/$ACCOUNTS_RESOURCE").post(body).build()
        val createResponse = client.newCall(createRequest).execute()
        val responseBody = createResponse.body?.string()

        assertEquals(createResponse.code, HttpStatus.CREATED_201)
        val uuid = Gson().fromJson(responseBody, AccountResponse::class.java).accountUuid

        val request = Request.Builder().url("$url/$ACCOUNTS_RESOURCE/$uuid").get().build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.OK_200)
        assertEquals(responseBody, response.body?.string())
    }


    /*
     * POST /TRANSFERS
     */

    @Test
    fun `Given an empty request body When a POST transfers request is invoked Then a 400 Bad Request is returned`() {
        val expectedResponse = readJson("/transfers/post/post-transfer-empty-body-response.json")

        val request = Request.Builder().url("$url/$TRANSFERS_RESOURCE").post("".toRequestBody()).build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.BAD_REQUEST_400)
        assertEquals(response.body?.string(), expectedResponse)
    }

    @Test
    fun `Given a negative balance in the request body When a POST transfers request is invoked Then a 400 Bad Request is returned`() {
        val body = requestBodyFromJson("/transfers/post/transfer-negative-amount.json")
        val expectedResponse = readJson("/transfers/post/transfer-negative-amount-response.json")

        val request = Request.Builder().url("$url/$TRANSFERS_RESOURCE").post(body).build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.BAD_REQUEST_400)
        assertEquals(response.body?.string(), expectedResponse)
    }

    @Test
    fun `Given a transfer to the same account in the request body When a POST transfers request is invoked Then a 400 Bad Request is returned`() {
        val body = requestBodyFromJson("/transfers/post/transfer-same-account.json")
        val expectedResponse = readJson("/transfers/post/transfer-same-account-response.json")

        val request = Request.Builder().url("$url/$TRANSFERS_RESOURCE").post(body).build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.BAD_REQUEST_400)
        assertEquals(response.body?.string(), expectedResponse)
    }

    @Test
    fun `Given a transfer with an account that does not exist in the request body When a POST transfers request is invoked Then a 404 Not Found is returned`() {
        val body = requestBodyFromJson("/transfers/post/does-not-exist.json")

        val request = Request.Builder().url("$url/$TRANSFERS_RESOURCE").post(body).build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.NOT_FOUND_404)
    }

    @Test
    fun `Given a transfer with one account having insufficient funds When a POST transfers request is invoked Then a 400 Bad Request is returned`() {
        val accountA = createAccount(0)
        val accountB = createAccount(10)
        val response = createTransfer(accountA.accountUuid, accountB.accountUuid, 4)

        val body = response.body?.string() ?: ""
        assert(body.contains("InsufficientBalanceException"))
        assertEquals(response.code, HttpStatus.BAD_REQUEST_400)
    }


    @Test
    fun `Given a valid transfer When a POST transfers request is invoked Then a 201 Created is returned`() {
        val accountA = createAccount(42)
        val accountB = createAccount(0)

        val response = createTransfer(accountA.accountUuid, accountB.accountUuid, 19)
        assertEquals(response.code, HttpStatus.CREATED_201)

        val newBalanceA = getAccount(accountA.accountUuid)
        val newBalanceB = getAccount(accountB.accountUuid)

        assertEquals(newBalanceA.balance, 42 - 19)
        assertEquals(newBalanceB.balance, 0 + 19)
    }


    /*
     * GET /TRANSFERS
     */

    @Test
    fun `Given a newly created account When a GET transfers request is invoked Then a 200 OK is returned`() {
        val account = createAccount(10)

        val request = Request.Builder().url("$url/$ACCOUNTS_RESOURCE/${account.accountUuid}/transfers").get().build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.OK_200)

        val accountWithTransfersResponse =
            Gson().fromJson(response.body?.string() ?: "", AccountWithTransfersResponse::class.java)

        assertEquals(accountWithTransfersResponse.accountUuid, account.accountUuid.toString())
        assert(accountWithTransfersResponse.transfers.isEmpty())
    }

    @Test
    fun `Given a account with transfers When a GET transfers request is invoked Then a 200 OK is returned`() {
        val account = createAccount(10)
        val accountB = createAccount(20)

        createTransfer(account.accountUuid, accountB.accountUuid, 4)
        createTransfer(accountB.accountUuid, account.accountUuid, 12)

        val request = Request.Builder().url("$url/$ACCOUNTS_RESOURCE/${account.accountUuid}/transfers").get().build()
        val response = client.newCall(request).execute()

        assertEquals(response.code, HttpStatus.OK_200)

        val accountWithTransfersResponse =
            Gson().fromJson(response.body?.string() ?: "", AccountWithTransfersResponse::class.java)

        assertEquals(accountWithTransfersResponse.accountUuid, account.accountUuid.toString())
        assertEquals(accountWithTransfersResponse.transfers.size, 2)
    }


    private fun createAccount(balance: Long): AccountResponse {
        val body = "{\"balance\": $balance}".toRequestBody("application/json".toMediaType())

        val createRequest = Request.Builder().url("$url/$ACCOUNTS_RESOURCE").post(body).build()
        val createResponse = client.newCall(createRequest).execute()
        return Gson().fromJson(createResponse.body?.string(), AccountResponse::class.java)
    }

    private fun createTransfer(from: UUID, to: UUID, amount: Long): Response {
        val body =
            "{\"from\": \"$from\", \"to\": \"$to\", \"amount\": $amount}".toRequestBody("application/json".toMediaType())

        val createRequest = Request.Builder().url("$url/$TRANSFERS_RESOURCE").post(body).build()
        return client.newCall(createRequest).execute()
    }

    private fun getAccount(uuid: UUID): AccountResponse {
        val request = Request.Builder().url("$url/$ACCOUNTS_RESOURCE/$uuid").get().build()
        return Gson().fromJson(client.newCall(request).execute().body?.string(), AccountResponse::class.java)
    }
}