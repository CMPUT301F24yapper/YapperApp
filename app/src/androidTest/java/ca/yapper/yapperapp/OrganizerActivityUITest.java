package ca.yapper.yapperapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import ca.yapper.yapperapp.Activities.OrganizerActivity;

public class OrganizerActivityUITest {

    @Rule
    public ActivityScenarioRule<OrganizerActivity> activityRule =
            new ActivityScenarioRule<>(OrganizerActivity.class);

    @Test
    public void testOrganizerActivityLaunch() {
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToHomeFragment() {
        onView(withId(R.id.nav_organizer_home)).perform(click());
        onView(withId(R.id.my_events_title)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToCreateEventFragment() {
        onView(withId(R.id.nav_organizer_createevent)).perform(click());
        onView(withId(R.id.new_event_title)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationToProfileFragment() {
        onView(withId(R.id.nav_organizer_profile)).perform(click());
        onView(withId(R.id.edit_name)).check(matches(isDisplayed()));
    }

    @Test
    public void testToolbarLogoDisplayed() {
        onView(withId(R.id.toolbar_logo)).check(matches(isDisplayed()));
    }
}
