package xyz.schnabl.event


import com.google.common.collect.ImmutableList
import java.util.UUID

/**
 * Defines an aggregate and basic operations for aggregates
 * @property aggregateUuid : UUID  Unique Identifier for aggregates
 * @property versionNumber : Long  Current version of an aggregate needed for data storage
 * @property events : MutableList<Event>   List of all events for this aggregate
 */
abstract class Aggregate(
    private var aggregateUuid: UUID,
    private var versionNumber: Long = 0,
    private val events: MutableList<Event> = mutableListOf()
) {
    protected abstract fun apply(event: Event)

    /**
     * Applies all provided events to the current aggregate
     * @param events : List<Event>  List of all events to be applied
     */
    fun applyAll(events: List<Event>) {
        versionNumber += events.size
        events.forEach {
            apply(it)
        }
    }

    /**
     * Applies an event to the current aggregate
     * @param event : Event Event to be applied
     */
    fun applyAndAdd(event: Event) {
        apply(event)
        events.add(event)
    }

    fun getVersionNumber(): Long {
        return versionNumber + events.size
    }

    fun getAggregateUuid(): UUID {
        return aggregateUuid
    }

    fun getEvents(): List<Event> {
        return ImmutableList.copyOf(events)
    }
}