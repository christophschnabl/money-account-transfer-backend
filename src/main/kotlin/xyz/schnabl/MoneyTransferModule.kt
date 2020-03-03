package xyz.schnabl

import com.google.inject.AbstractModule
import xyz.schnabl.account.AccountServiceImpl
import xyz.schnabl.account.model.AccountService
import xyz.schnabl.event.EventDataStore
import xyz.schnabl.event.EventDataStoreMap
import xyz.schnabl.transfer.TransferServiceImpl
import xyz.schnabl.transfer.model.TransferService


class MoneyTransferModule : AbstractModule() {
    override fun configure() {
        bind(TransferService::class.java).to(TransferServiceImpl::class.java)
        bind(AccountService::class.java).to(AccountServiceImpl::class.java)
        bind(EventDataStore::class.java).to(EventDataStoreMap::class.java)
    }
}