package ca.yapper.yapperapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import ca.yapper.yapperapp.Activities.OrganizerActivity;

/**
 * UI test class for the CreateEditEventFragment.
 * This class contains test cases to validate UI elements and interactions.
 */
public class CreateEditEventFragmentTest {

    @Rule
    public ActivityScenarioRule<OrganizerActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerActivity.class);

    /**
     * Verifies that the CreateEditEventFragment UI elements are displayed.
     */
    @Test
    public void testCreateEditEventUIElementsDisplayed() {
        // Navigate to CreateEditEventFragment
        onView(withId(R.id.nav_organizer_createevent)).perform(click());

        // Verify UI elements with scrollTo()
        onView(withId(R.id.new_event_title)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.poster_image)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.choose_poster_button)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.event_name_input)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.event_description)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.date_button)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.date_textview)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.time_button)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.time_textview)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.geo_location_toggle)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.save_event_button)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
    }

    /**
     * Tests entering event details and saving.
     */
    @Test
    public void testEnterEventDetailsAndSave() {
        // Navigate to CreateEditEventFragment
        onView(withId(R.id.nav_organizer_createevent)).perform(click());

        // Scroll to and enter details
        onView(withId(R.id.event_name_input)).perform(ViewActions.scrollTo(), typeText("Test Event"));
        onView(withId(R.id.event_description)).perform(ViewActions.scrollTo(), typeText("This is a test event description."));

        // Interact with other UI elements
        onView(withId(R.id.date_button)).perform(ViewActions.scrollTo(), click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.time_button)).perform(ViewActions.scrollTo(), click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.geo_location_toggle)).perform(ViewActions.scrollTo(), click());

        // Save the event
        onView(withId(R.id.save_event_button)).perform(ViewActions.scrollTo(), click());
    }

    /**
     * Tests toggling the geolocation switch.
     */
    @Test
    public void testGeoLocationToggleIsVisibleAndInteractable() {
        // Navigate to CreateEditEventFragment
        onView(withId(R.id.nav_organizer_createevent)).perform(click());

        // Scroll to the geo_location_toggle
        onView(withId(R.id.geo_location_toggle)).perform(ViewActions.scrollTo());

        // Verify it is displayed
        onView(withId(R.id.geo_location_toggle)).check(matches(isDisplayed()));

        // Interact with the toggle
        onView(withId(R.id.geo_location_toggle)).perform(click());
        onView(withId(R.id.geo_location_toggle)).check(matches(ViewMatchers.isChecked()));
    }

}