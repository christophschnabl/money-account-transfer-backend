package xyz.schnabl.account.model

/**
 *  Specifies the command to create an account
 *  @property balance : Long    the accounts initial balance
 */
data class CreateAccountCommand(
    val balance: Long
)