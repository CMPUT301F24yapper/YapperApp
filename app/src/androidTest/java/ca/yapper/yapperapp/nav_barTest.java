package ca.yapper.yapperapp;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.yapper.yapperapp.Activities.EntrantActivity;

@RunWith(AndroidJUnit4.class)
public class nav_barTest {

    @Rule
    public ActivityScenarioRule<EntrantActivity> activityRule = new ActivityScenarioRule<>(EntrantActivity.class);

    /**
     * Test for navigating to the Home screen from the navigation bar.
     */
    @Test
    public void testNavigateToHome() {
        // Click the Home icon in the navigation bar
        Espresso.onView(ViewMatchers.withId(R.id.nav_entrant_home)).perform(ViewActions.click());

        // Verify that the Home screen is displayed by checking a view unique to that screen
        Espresso.onView(ViewMatchers.withId(R.id.tabLayout)) // Assume home_layout is a unique ID in Home screen
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }


    /**
     * Test for navigating to the Notifications screen from the navigation bar.
     */
    @Test
    public void testNavigateToNotifications() {
        // Click the Notifications icon in the navigation bar
        Espresso.onView(ViewMatchers.withId(R.id.nav_entrant_notifications)).perform(ViewActions.click());

        // Verify that the Notifications screen is displayed by checking the title or a unique view
        Espresso.onView(ViewMatchers.withId(R.id.notification_title)) // Assume notification_title is unique to Notifications screen
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test for navigating to the Profile screen from the navigation bar.
     */
    @Test
    public void testNavigateToProfile() {
        // Click the Profile icon in the navigation bar
        Espresso.onView(ViewMatchers.withId(R.id.nav_entrant_profile)).perform(ViewActions.click());

        // Verify that the Profile screen is displayed by checking a unique view in the Profile screen
        Espresso.onView(ViewMatchers.withId(R.id.upper_layout)) // Assume upper_layout is a unique ID in Profile screen
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
