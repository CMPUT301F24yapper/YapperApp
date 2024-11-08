package ca.yapper.yapperapp;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import ca.yapper.yapperapp.Activities.OrganizerActivity;

public class OrganizerActivityTest {
    // Step 2: Set up ActivityScenarioRule to launch OrganizerActivity before each test
    @Rule
    public ActivityScenarioRule<OrganizerActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerActivity.class);

    @Test
    public void testActivityLaunches() {
        // Verify that the activity is displayed by checking for an element within it
        onView(withId(R.id.organizer_activity)).check(matches(isDisplayed()));
    }
}
