package xyz.schnabl

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.inject.Guice
import com.google.inject.Injector
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.LoggerFactory
import spark.ExceptionHandler
import spark.Filter
import spark.Request
import spark.Response
import spark.Spark.after
import spark.Spark.before
import spark.Spark.exception
import spark.Spark.get
import spark.Spark.path
import spark.Spark.post
import xyz.schnabl.account.AccountController
import xyz.schnabl.account.AccountNotFoundException
import xyz.schnabl.account.InsufficientBalanceException
import xyz.schnabl.event.LockingException
import xyz.schnabl.transfer.TransferController


class MoneyTransferApplication {}

private val log = LoggerFactory.getLogger(MoneyTransferApplication::class.java)

private val gson = Gson()


fun main() {
    val injector: Injector = Guice.createInjector(MoneyTransferModule())

    val accountController = injector.getInstance(AccountController::class.java)
    val transferController = injector.getInstance(TransferController::class.java)

    before(Filter { request: Request, _: Response? ->
        log.info("${request.requestMethod()}, ${request.url()}, ${request.body()}")
    })

    after(Filter { _: Request, response: Response? ->
        response?.type("application/json")
    })

    path("/") {
        path(ACCOUNTS_RESOURCE) {
            post(NONE) { request, response ->
                response.status(HttpStatus.CREATED_201)
                response.header(HttpHeader.LOCATION.toString(), ACCOUNTS_RESOURCE)
                gson.toJson(accountController.createAccount(request))
            }

            path("/$UUID_PARAMETER") {
                get(NONE) { request, _ ->
                    gson.toJson(accountController.getAccount(request))
                }

                get("/$TRANSFERS_RESOURCE") { request, _ ->
                    gson.toJson(transferController.getTransfersForAccount(request))
                }
            }
        }

        path(TRANSFERS_RESOURCE) {
            post(NONE) { request, response ->
                response.status(HttpStatus.CREATED_201)
                response.header(
                    HttpHeader.LOCATION.toString(),
                    "$ACCOUNTS_RESOURCE/$UUID_PARAMETER/$TRANSFERS_RESOURCE"
                )
                gson.toJson(transferController.createTransfer(request))
            }
        }
    }


    // Exception Mapping

    // Non Recoverable/Retrieable Errors
    exception(IllegalArgumentException::class.java, getExceptionHandlerForStatus(HttpStatus.BAD_REQUEST_400))
    exception(InsufficientBalanceException::class.java, getExceptionHandlerForStatus(HttpStatus.BAD_REQUEST_400))
    exception(JsonSyntaxException::class.java, getExceptionHandlerForStatus(HttpStatus.BAD_REQUEST_400))
    exception(AccountNotFoundException::class.java, getExceptionHandlerForStatus(HttpStatus.NOT_FOUND_404))

    // Recoverable Errors
    exception(LockingException::class.java, getExceptionHandlerForStatus(HttpStatus.CONFLICT_409))
    exception(Exception::class.java, getExceptionHandlerForStatus(HttpStatus.INTERNAL_SERVER_ERROR_500))
}

private fun Exception.toJson(): String {
    return Gson().toJson(
        mapOf(
            "exception" to javaClass.name,
            "message" to message.toString()
        )
    )
}


private fun getExceptionHandlerForStatus(status: Int): ExceptionHandler<Exception> {
    return ExceptionHandler { exception: java.lang.Exception, _: Request?, response: Response ->
        response.type("application/json")
        response.status(status)
        response.body(exception.toJson())
    }
}
