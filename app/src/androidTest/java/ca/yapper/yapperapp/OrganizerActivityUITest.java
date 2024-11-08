package ca.yapper.yapperapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.action.ViewActions;
//import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import ca.yapper.yapperapp.Activities.OrganizerActivity;

/**
 * UI test class for the OrganizerActivity.
 * This class contains test cases to validate UI elements, navigation, and interactions
 * within the OrganizerActivity.
 */
public class OrganizerActivityUITest {

    /**
     * Rule to launch the OrganizerActivity before each test.
     */
    @Rule
    public ActivityScenarioRule<OrganizerActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerActivity.class);

    /**
     * Verifies that the OrganizerActivity launches successfully and the bottom navigation bar is displayed.
     */
    @Test
    public void testOrganizerActivityLaunch() {
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));
    }

    /**
     * Tests navigation to the Home fragment and verifies that the title for "My Events" is displayed.
     */
    @Test
    public void testNavigationToHomeFragment() {
        onView(withId(R.id.nav_organizer_home)).perform(click());
        onView(withId(R.id.my_events_title)).check(matches(isDisplayed()));
    }

    /**
     * Tests navigation to the Create Event fragment and verifies that the title for "New Event" is displayed.
     */
    @Test
    public void testNavigationToCreateEventFragment() {
        onView(withId(R.id.nav_organizer_createevent)).perform(click());
        onView(withId(R.id.new_event_title)).check(matches(isDisplayed()));
    }

    /**
     * Tests navigation to the Profile fragment and verifies that the name edit field is displayed.
     */
    @Test
    public void testNavigationToProfileFragment() {
        onView(withId(R.id.nav_organizer_profile)).perform(click());
        onView(withId(R.id.edit_name)).check(matches(isDisplayed()));
    }

    /**
     * Verifies that the toolbar logo is displayed in the OrganizerActivity.
     */
    @Test
    public void testToolbarLogoDisplayed() {
        onView(withId(R.id.toolbar_logo)).check(matches(isDisplayed()));
    }

    /**
     * Verifies that all necessary UI elements in the Create Event fragment are displayed.
     */
    @Test
    public void testCreateEventUIElementsDisplayed() {
        onView(withId(R.id.nav_organizer_createevent)).perform(click());
        onView(withId(R.id.event_name_input)).check(matches(isDisplayed()));
        onView(withId(R.id.date_input)).check(matches(isDisplayed()));
        onView(withId(R.id.save_event_button)).check(matches(isDisplayed()));
    }

    /**
     * Tests toggling the geolocation switch in the Create Event fragment and verifies its state.
     */
    @Test
    public void testToggleGeolocationSwitch() {
        onView(withId(R.id.nav_organizer_createevent)).perform(click());
        onView(withId(R.id.geo_location_toggle)).perform(click());
        onView(withId(R.id.geo_location_toggle)).check(matches(ViewMatchers.isChecked()));
    }

    /**
     * Verifies that all necessary UI elements in the Profile fragment are displayed.
     */
    @Test
    public void testProfileUIElementsDisplayed() {
        onView(withId(R.id.nav_organizer_profile)).perform(click());
        onView(withId(R.id.edit_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()));
    }

    /**
     * Tests that the "Switch Roles" menu is displayed when performing a long click on the Profile navigation button.
     */
    @Test
    public void testSwitchRolesMenuDisplayed() {
        onView(withId(R.id.nav_organizer_profile)).perform(ViewActions.longClick());
        onView(withText("Switch to Entrant")).check(matches(isDisplayed()));
    }

    /**
     * Tests backstack behavior by navigating between the Profile and Create Event fragments.
     */
    @Test
    public void testBackstackBehavior() {
        onView(withId(R.id.nav_organizer_profile)).perform(click());
        onView(withId(R.id.nav_organizer_createevent)).perform(click());
        onView(withId(R.id.nav_organizer_profile)).check(matches(isDisplayed())).check(matches(isDisplayed()));
    }
}