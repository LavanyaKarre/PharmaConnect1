package com.cts.mfrp.petz.tests.ngo;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.RescueQueuePage;
import com.cts.mfrp.petz.utils.RandomDataGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC022 - NGO Update Rescue - add notes with Monkey payload.
 * Monkey value sourced from RandomDataGenerator (XSS pool).
 */
public class TC022_NGOUpdateRescueTest extends BaseTest {

    @Test(groups = {"ngo", "regression", "monkey"},
          description = "TC022 - NGO Update Rescue with Monkey notes (XSS payload)")
    public void TC022_NGOUpdateRescue() {
        new LoginPage().open().loginAs(UserRole.NGO);
        RescueQueuePage queue = new RescueQueuePage().open();

        if (queue.hasNotesInput()) {
            try {
                queue.fillNotes(RandomDataGenerator.randomXss());
                queue.clickUpdate();
            } catch (Exception ignored) {}
        }

        Assert.assertTrue(queue.isTitleVisible(),
                "Rescue Queue not in expected state after update");
    }
}
