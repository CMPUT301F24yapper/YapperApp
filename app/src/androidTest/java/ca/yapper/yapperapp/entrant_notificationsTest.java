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
public class entrant_notificationsTest {

    @Rule
    public ActivityScenarioRule<EntrantActivity> activityRule = new ActivityScenarioRule<>(EntrantActivity.class);

    /**
     * Test to check if clicking "Notification" in the navigation bar opens the notifications layout.
     */
    @Test
    public void testOpenNotificationsLayoutFromNavBar() {
        // Click the notifications icon in the navigation bar
        Espresso.onView(ViewMatchers.withId(R.id.nav_entrant_notifications)).perform(ViewActions.click());

        // Check if the notifications title is displayed
        Espresso.onView(ViewMatchers.withId(R.id.notification_title))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(ViewAssertions.matches(ViewMatchers.withText("Notifications")));

        // Check if the RecyclerView for notifications is displayed
        Espresso.onView(ViewMatchers.withId(R.id.notifications_recycler_view))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Optionally, check if the RecyclerView is scrollable or has items
        // Note: This will require a populated list in the RecyclerView
        Espresso.onView(ViewMatchers.withId(R.id.notifications_recycler_view))
                .check(ViewAssertions.matches(ViewMatchers.isAssignableFrom(androidx.recyclerview.widget.RecyclerView.class)));
    }
}
