package xyz.schnabl.event

import com.google.common.collect.ImmutableList
import com.google.inject.Singleton
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Implements a simple EventStore based on a concurrentHashMap by using optimistic locking based on versioning
 * @property dataStoreMap : ConcurrentHashMap<UUID, MutableList<Event>>  internal data structure to save events for each aggregate
 */
@Singleton
class EventDataStoreMap : EventDataStore {
    private val dataStoreMap: ConcurrentHashMap<UUID, MutableList<Event>> = ConcurrentHashMap()

    @Synchronized
    override fun store(eventAggregates: List<EventAggregate>) {
        // First we check if the whole list of EventAggregates is possible
        eventAggregates.forEach {
            val highestPersistedVersion = dataStoreMap[it.aggregateUuid]?.size ?: -1  // -1 to fail for duplicate users
            if (it.baseVersion <= highestPersistedVersion) {
                throw LockingException("Persisted aggregate version is higher than this version")
            }
        }

        // Then we save those to the store
        eventAggregates.forEach {
            dataStoreMap[it.aggregateUuid]?.apply { addAll(it.events) } ?: dataStoreMap.put(
                it.aggregateUuid,
                it.events.toMutableList()
            )
        }
    }

    override fun store(eventAggregate: EventAggregate) {
        store(listOf(eventAggregate))
    }

    override fun get(aggregateUuid: UUID): List<Event> {
        return ImmutableList.copyOf(dataStoreMap[aggregateUuid].orEmpty())
    }
}