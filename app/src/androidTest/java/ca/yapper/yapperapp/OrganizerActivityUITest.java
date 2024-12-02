package ca.yapper.yapperapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import ca.yapper.yapperapp.Activities.OrganizerActivity;

/**
 * Simplified UI Test class for OrganizerActivity.
 * Covers basic functionality across Home, Create/Edit Event, and Profile pages.
 */
public class OrganizerActivityUITest {

    @Rule
    public ActivityScenarioRule<OrganizerActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerActivity.class);

    /**
     * Test: Home Page - Verify title is displayed.
     */
    @Test
    public void testHomePageTitleDisplayed() {
        onView(withId(R.id.nav_organizer_home)).perform(click());
        onView(withId(R.id.my_events_title)).check(matches(isDisplayed()));
    }

    /**
     * Test: Create/Edit Event Page - Verify title and save button are displayed.
     */
    @Test
    public void testCreateEditEventPage() {
        onView(withId(R.id.nav_organizer_createevent)).perform(click());
        onView(withId(R.id.new_event_title)).check(matches(isDisplayed()));
        onView(withId(R.id.save_event_button)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /**
     * Test: Create/Edit Event Page - Enter event name and save.
     */

}