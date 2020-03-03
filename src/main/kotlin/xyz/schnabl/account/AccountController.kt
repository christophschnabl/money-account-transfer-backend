package xyz.schnabl.account

import com.google.gson.Gson
import com.google.inject.Inject
import com.google.inject.Singleton
import spark.Request
import xyz.schnabl.UUID_PARAMETER
import xyz.schnabl.account.dto.AccountResponse
import xyz.schnabl.account.dto.toResponse
import xyz.schnabl.account.model.AccountService
import xyz.schnabl.account.model.CreateAccountCommand
import java.util.UUID


/**
 * Responsible for accepting account specific requests (create, get) and returning DTOs able to be serialized
 * @property accountService : AccountService  Service to interact with accounts
 */
@Singleton
class AccountController @Inject constructor(private val accountService: AccountService) {

    /**
     * Returns account for uuid passed as request parameter
     * @param request : Request containing the uuid as parameter
     * @return AccountResponse
     */
    fun getAccount(request: Request): AccountResponse {
        val account = accountService.getAccount(UUID.fromString(request.params(UUID_PARAMETER)))
        return account.toResponse()
    }


    /**
     * Creates an account with an initial balance
     * @param request : Request Request containing the initial balance as parameter
     * @return AccountResponse
     */
    fun createAccount(request: Request): AccountResponse {
        if (request.body() == "") throw IllegalArgumentException("Request body must not be empty for account creation")

        val createAccountCommand = Gson().fromJson(request.body(), CreateAccountCommand::class.java)
        return accountService.processAccountCreation(createAccountCommand).toResponse()
    }
}