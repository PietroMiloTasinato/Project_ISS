import main.kotlin.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static de.regnis.q.sequence.core.QSequenceAssert.assertNotNull;
import static org.junit.Assert.assertNull;

abstract class HoldTestPlan {

	IHold hold;

	abstract IHold createHold();
	abstract IContainer createContainer();

	@Before
	public void setUp() {
		hold = createHold();
	}

	@Test
	public void getting_a_slot_while_all_hold_slots_are_free_should_return_a_non_null_value() {
		ISlot freeSlot = hold.getFreeSlot();
		Assert.assertNotNull("There are no slots available", freeSlot);
	}

	@Test
	public void getting_a_slot_while_they_are_all_occupied_should_return_a_null_value() {
		for (int i = 0; i < hold.getSlotNumber(); i++) {
			ISlot slot = hold.getFreeSlot();
			Assert.assertNotNull(slot);
			hold.codeAssigned(slot.getId(), i);
		}

		assertNull(hold.getFreeSlot());
	}
}