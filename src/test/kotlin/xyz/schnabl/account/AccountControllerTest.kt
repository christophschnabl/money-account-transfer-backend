package xyz.schnabl.account

import com.google.gson.JsonSyntaxException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import spark.Request
import xyz.schnabl.account.model.Account
import xyz.schnabl.account.model.AccountService
import java.util.UUID


class AccountControllerTest {

    private class RequestStub(private val body: String = "", private val req_param: String? = null) : Request() {
        override fun body(): String {
            return body
        }

        override fun params(param: String?): String? {
            return req_param
        }
    }

    private val accountService = mock<AccountService> {
        on { getAccount(any()) } doReturn Account(UUID.randomUUID())
        on { processAccountCreation(any()) } doReturn Account(UUID.randomUUID())
    }

    private val accountController = AccountController(accountService)

    @Test(expected = IllegalArgumentException::class)
    fun `Given an empty request body When an account is created Then an Exception is thrown`() {
        val request = RequestStub()
        accountController.createAccount(request)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `Given an invalid request body When an account is created Then an Exception is thrown`() {
        val request = RequestStub(": 10}")
        accountController.createAccount(request)
    }

    @Test
    fun `Given a valid request body Then an account is created`() {
        val request = RequestStub("{balance: 10}")
        val response = accountController.createAccount(request)
        assert(response.accountUuid.toString().isNotEmpty())
    }

    @Test(expected = NullPointerException::class)
    fun `Given an request with no params When an account is queried Then an Exception is thrown`() {
        val request = RequestStub()
        accountController.getAccount(request)
    }

    @Test
    fun `Given an request with a valid param When an account is queried Then an Account is returned`() {
        val request = RequestStub(req_param = UUID.randomUUID().toString())
        accountController.getAccount(request)
    }

}