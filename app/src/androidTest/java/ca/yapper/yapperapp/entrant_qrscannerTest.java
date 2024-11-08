package ca.yapper.yapperapp; // Ensure this is the correct package for your tests

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Test;
import org.junit.runner.RunWith;

import ca.yapper.yapperapp.Activities.EntrantActivity; // Adjust the import as necessary

import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class entrant_qrscannerTest {

    @Test
    public void testBarcodeViewIsDisplayed() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Check if the BarcodeView is displayed
            Espresso.onView(withId(R.id.barcode_view))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testViewfinderViewIsDisplayed() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Check if the ViewfinderView is displayed
            Espresso.onView(withId(R.id.viewfinder))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testScanTextIsDisplayedCorrectly() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Check if the TextView with scanning instruction is displayed and has the correct text
            Espresso.onView(withId(R.id.scan_text))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                    .check(ViewAssertions.matches(withText("Scanning For QR Codes")));
        }
    }
}
