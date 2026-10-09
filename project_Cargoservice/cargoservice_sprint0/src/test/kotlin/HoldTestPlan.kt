package test.kotlin

import main.kotlin.IContainer
import main.kotlin.IHold
import main.kotlin.ISlot
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

abstract class HoldTestPlan {

    abstract var hold: IHold
    abstract fun createSlot() : ISlot
    abstract fun createHold(): IHold

    @Before
    fun setUp(){
        hold = createHold()
    }

    @Test
    fun `getting a slot while all hold's slots are free, should return a non null value`(){
        val freeSlot = hold.getFreeSlot()
        assertNotNull(freeSlot, "There are no slots available")
    }

    @Test
    fun `getting a slot while they are all occupied should return a null value`(){
        val newMap = hold.holdMap.mapValues { (point, slot) ->
            createSlot()
        }
        val slot = hold.getFreeSlot()
        hold.holdMap.forEach { (point, slot) ->
            assertNotNull(slot.container, "All slots were expected to be not null, but ${slot.id} is null")
        }
        assertNull(slot, "There is a free slot with this id: ${slot!!.id}")
    }

}