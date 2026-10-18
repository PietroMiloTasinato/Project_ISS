import main.kotlin.IHold;
import main.kotlin.IPoint;
import main.kotlin.ISlot;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static de.regnis.q.sequence.core.QSequenceAssert.assertNotNull;
import static org.junit.Assert.assertNull;

abstract class HoldTestPlan {

	IHold hold;

	abstract ISlot createSlot(String id);

	abstract IHold createHold();

	@Before
	public void setUp() {
		hold = createHold();
	}

	@Test
	public void getting_a_slot_while_all_hold_slots_are_free_should_return_a_non_null_value() {
		ISlot freeSlot = hold.getFreeSlot();
		assertNotNull(freeSlot, "There are no slots available");
	}

	@Test
	public void getting_a_slot_while_they_are_all_occupied_should_return_a_null_value() {
		for (Map.Entry<IPoint, ISlot> entry : hold.getHoldMap().entrySet()) {
			ISlot newSlot = createSlot(entry.getValue().getId());
			assertNotNull(newSlot.getContainer());
			entry.setValue(newSlot);
		}

		assertNull(hold.getFreeSlot());
	}
}