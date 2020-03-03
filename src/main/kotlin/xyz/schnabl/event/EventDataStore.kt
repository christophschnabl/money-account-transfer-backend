package xyz.schnabl.event

import java.util.UUID

/**
 *  Defines access and storage methods to persist aggregate events
 */
interface EventDataStore {
    /**
     * Stores an EventAggregate
     * @param eventAggregate : EventAggregate  EventAggregate
     */
    fun store(eventAggregate: EventAggregate)

    /**
     * Stores a list of aggregates that are processed sequentially
     * @param eventAggregates : List<EventAggregate>  List of EventAggregates
     */
    fun store(eventAggregates: List<EventAggregate>)

    /**
     * Gets a stored aggregate by UUID
     * @param aggregateUuid : UUID  UUID of the wanted aggregate
     */
    fun get(aggregateUuid: UUID): List<Event>
}

/**
 * Represents an entry that is stored in an EventDataStore
 * @property aggregateUuid : UUID  UUID of this aggregate
 * @property events : List<Event>  All events of this aggregate
 * @property baseVersion : Long    The version of this aggregate
 */
data class EventAggregate(
    val aggregateUuid: UUID,
    val events: List<Event>,
    val baseVersion: Long
);