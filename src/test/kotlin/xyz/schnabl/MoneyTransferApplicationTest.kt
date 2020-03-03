package xyz.schnabl

import org.junit.Test

class MoneyTransferApplicationTest {

    @Test
    fun `Resource Routes work`() {
        assert(TRANSFERS_RESOURCE.isNotEmpty())
        assert(ACCOUNTS_RESOURCE.isNotEmpty())
    }

    @Test
    fun `Main class works`() {
        val moneyTransferApplication = MoneyTransferApplication()
    }
}
