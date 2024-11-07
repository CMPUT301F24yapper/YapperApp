package ca.yapper.yapperapp;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import ca.yapper.yapperapp.Activities.EntrantActivity;

@RunWith(AndroidJUnit4.class)
public class recycler_eventitemTest {

    @Rule
    public ActivityScenarioRule<EntrantActivity> activityRule =
            new ActivityScenarioRule<>(EntrantActivity.class);

    @Test
    public void testEventItemIsDisplayed() {
        // Check if the event name TextView is displayed
        Espresso.onView(withId(R.id.event_name))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Check if the event date TextView is displayed
        Espresso.onView(withId(R.id.event_date))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Check if the event location TextView is displayed
        Espresso.onView(withId(R.id.event_location))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testEventItemText() {
        // Check if the event name TextView has the correct text
        Espresso.onView(withId(R.id.event_name))
                .check(ViewAssertions.matches(ViewMatchers.withText("Event Name"))); // Placeholder text

        // Check if the event date TextView has the correct text
        Espresso.onView(withId(R.id.event_date))
                .check(ViewAssertions.matches(ViewMatchers.withText("dd/mm/yyyy"))); // Placeholder date

        // Check if the event location TextView has the correct text
        Espresso.onView(withId(R.id.event_location))
                .check(ViewAssertions.matches(ViewMatchers.withText("Location"))); // Placeholder location
    }
}
