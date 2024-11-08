package ca.yapper.yapperapp; // Ensure this is the correct package for your tests

import android.view.View;
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
public class entrant_notificationitemTest {

    @Test
    public void testEventNameIsDisplayed() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Assuming this layout is part of the RecyclerView, we need to check it in the context of the RecyclerView
            Espresso.onView(withId(R.id.event_name))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                    .check(ViewAssertions.matches(withText("Event Name"))); // You can replace "Event Name" with actual test data
        }
    }

    @Test
    public void testNotificationStatusIsDisplayed() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Check if the notification status TextView is displayed
            Espresso.onView(withId(R.id.notification_status))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                    .check(ViewAssertions.matches(withText("You were selected!"))); // Replace with actual test data if needed
        }
    }
}
