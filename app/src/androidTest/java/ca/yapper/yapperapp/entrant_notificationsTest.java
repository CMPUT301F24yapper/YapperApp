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
public class entrant_notificationsTest {

    @Test
    public void testNotificationTitleIsDisplayed() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Verify that the notification title is displayed
            Espresso.onView(withId(R.id.notification_title))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                    .check(ViewAssertions.matches(withText("Notifications")));
        }
    }

    @Test
    public void testRecyclerViewIsDisplayed() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Verify that the RecyclerView for notifications is displayed
            Espresso.onView(withId(R.id.notifications_recycler_view))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testRecyclerViewHandlesEmptyState() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Assuming that the adapter handles empty state internally
            // We could check that the RecyclerView is displayed but has no items
            Espresso.onView(withId(R.id.notifications_recycler_view))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
            // Here, you can also check the adapter's item count if needed, assuming you have access to it.
        }
    }
}
