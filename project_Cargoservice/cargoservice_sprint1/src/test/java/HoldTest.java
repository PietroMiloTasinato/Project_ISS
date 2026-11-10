import main.kotlin.*;

public class HoldTest extends HoldTestPlan {

    @Override
    IHold createHold() {
        return Hold.Companion.createHoldFromConfig(Hold.Companion.getDEFAULT_CONFIG_FILE());
    }

}
