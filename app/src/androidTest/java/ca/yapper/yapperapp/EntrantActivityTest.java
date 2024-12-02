package ca.yapper.yapperapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import ca.yapper.yapperapp.Activities.EntrantActivity;

/**
 * UI Test cases for EntrantActivity.
 */
public class EntrantActivityTest {

    @Rule
    public ActivityScenarioRule<EntrantActivity> activityRule =
            new ActivityScenarioRule<>(EntrantActivity.class);


    /**
     * Test: Verify QR Scanner fragment displays correctly.
     */
    @Test
    public void testQRScannerFragmentVisibility() {
        onView(withId(R.id.nav_entrant_qrscanner)).perform(click());
        onView(withId(R.id.barcode_view)).check(matches(isDisplayed()));
    }

    /**
     * Test: Verify tab switching in Notifications.
     */
    @Test
    public void testTabSwitching() {
        // Navigate to Entrant Home
        onView(withId(R.id.nav_entrant_home)).perform(click());

        // Verify the "Registered Events" tab is displayed and click on it
        onView(withText("Registered Events")).perform(click());
        onView(withId(R.id.viewPager)).check(matches(isDisplayed()));

        // Switch to "Joined Events" tab and verify it's displayed
        onView(withText("Joined Events")).perform(click());
        onView(withId(R.id.viewPager)).check(matches(isDisplayed()));

        // Switch to "Missed Out" tab and verify it's displayed
        onView(withText("Missed Out")).perform(click());
        onView(withId(R.id.viewPager)).check(matches(isDisplayed()));
    }


    /**
     * Test: Verify Event Card is displayed in Notifications.
     */
    @Test
    public void testEventCardVisibility() {
        onView(withId(R.id.nav_entrant_notifications)).perform(click());
        onView(withId(R.id.notifications_recycler_view)).check(matches(isDisplayed()));
    }


    /**
     * Test: Verify Toolbar logo is always displayed.
     */
    @Test
    public void testToolbarLogoVisibility() {
        onView(withId(R.id.toolbar_logo)).check(matches(isDisplayed()));
    }
}