import main.kotlin.*;

import java.io.File;

public class HoldTest extends HoldTestPlan {

    @Override
    IHold createHold() {
        return Hold.Companion.createHoldFromConfig(Hold.Companion.getDEFAULT_CONFIG_FILE());
    }

    @Override
    IContainer createContainer() {
        return new Container();
    }
}
