package dev.auguste.agni_api.core.entities

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.properties.Delegates

data class DumbValueObject(val num: Int, val txt: String)

class DumbEntity(
    id: UUID = UUID.randomUUID(),
    name: String,
    dumbValueObject: DumbValueObject,
    dumbListValueObject: MutableSet<DumbValueObject> = mutableSetOf()): Entity(id = id) {

    var name: String by Delegates.observable(name) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }

    var dumbValueObjectList: MutableSet<DumbValueObject> by Delegates.observable(dumbListValueObject) { _, old, new ->
        println("$old -> $new")
        if (old.toSet() != new.toSet())
            this.markHasChanged()
    }

    var dumbValueObject: DumbValueObject? by Delegates.observable(dumbValueObject) { _, old, new ->
        if (old != new)
            this.markHasChanged()
    }
}

class EntityTest {

    @Test
    @DisplayName("Should initialize correctly")
    fun shouldInitializeCorrectly() {
        val dumbValueObject = DumbValueObject(1, "blue")
        val dumbEntity = DumbEntity(name = "New Dumb", dumbValueObject = dumbValueObject, dumbListValueObject =  mutableSetOf(dumbValueObject))

        Assertions.assertNotNull(dumbEntity.dumbValueObjectList)
        Assertions.assertEquals("New Dumb", dumbEntity.name)
        Assertions.assertEquals(dumbValueObject, dumbEntity.dumbValueObject)
        Assertions.assertEquals(listOf(dumbValueObject), dumbEntity.dumbValueObjectList)
    }

    @Test
    @DisplayName("Should mark change when value is updated")
    fun shouldMarkChangedWhenValueIsUpdated() {
        var dumbValueObject = DumbValueObject(1, "blue")
        val dumbEntity = DumbEntity(name = "New Dumb", dumbValueObject = dumbValueObject, dumbListValueObject =  mutableSetOf(dumbValueObject))

        dumbEntity.name = "Update name"
        Assertions.assertTrue(dumbEntity.hasChanged())

        dumbEntity.resetChangeState()
        Assertions.assertFalse(dumbEntity.hasChanged())

        dumbValueObject = DumbValueObject(1, "red")
        dumbEntity.dumbValueObject = dumbValueObject
        Assertions.assertTrue(dumbEntity.hasChanged())

        dumbEntity.resetChangeState()
    }
}