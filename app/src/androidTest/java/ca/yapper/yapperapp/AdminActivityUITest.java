package ca.yapper.yapperapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import ca.yapper.yapperapp.Activities.AdminActivity;

public class AdminActivityUITest {

    @Rule
    public ActivityScenarioRule<AdminActivity> activityRule =
            new ActivityScenarioRule<>(AdminActivity.class);

    /**
     * Test: Verify Admin Home Fragment is loaded by default.
     */
    @Test
    public void testAdminHomeFragmentLoadedByDefault() {
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
        // Add additional checks if specific views from AdminHomeFragment should be visible
    }

    /**
     * Test: Verify Bottom Navigation Items Exist.
     */
    @Test
    public void testBottomNavigationItemsExist() {
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_admin_home)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_admin_search)).check(matches(isDisplayed()));
        onView(withId(R.id.nav_admin_profile)).check(matches(isDisplayed()));
    }

    /**
     * Test: Verify Navigation to Search Fragment.
     */
    @Test
    public void testNavigateToSearchFragment() {
        onView(withId(R.id.nav_admin_search)).perform(click());
        // Add checks for specific views in AdminSearchFragment
        onView(withId(R.id.search_bar)).check(matches(isDisplayed()));
    }

    /**
     * Test: Verify Navigation to Profile Fragment.
     */
    @Test
    public void testNavigateToProfileFragment() {
        onView(withId(R.id.nav_admin_profile)).perform(click());
        // Add checks for specific views in ProfileFragment
        onView(withId(R.id.profile_image)).check(matches(isDisplayed()));
    }

    @Test
    public void testBottomNavigationDisplayed() {
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminHomeFragmentDefaultView() {
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
        // Specific elements from AdminHomeFragment
        onView(withId(R.id.total_events_label)).check(matches(isDisplayed()));
        onView(withId(R.id.total_events)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchFragmentElements() {
        onView(withId(R.id.nav_admin_search)).perform(click());
        onView(withId(R.id.search_bar)).check(matches(isDisplayed()));
    }

    @Test
    public void testLongPressOnProfileNavigation() {
        onView(withId(R.id.nav_admin_profile)).perform(longClick());
        onView(withText("Switch to Entrant")).check(matches(isDisplayed()));
        onView(withText("Switch to Organizer")).check(matches(isDisplayed()));
    }

    @Test
    public void testBiggestEventsSectionInHomeFragment() {
        onView(withId(R.id.nav_admin_home)).perform(click());
        onView(withId(R.id.biggest_events_label)).check(matches(isDisplayed()));
        onView(withId(R.id.events_list_container)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigateThroughAllFragments() {
        onView(withId(R.id.nav_admin_home)).perform(click());
        onView(withId(R.id.total_events_label)).check(matches(isDisplayed()));

        onView(withId(R.id.nav_admin_search)).perform(click());
        onView(withId(R.id.search_bar)).check(matches(isDisplayed()));

        onView(withId(R.id.nav_admin_profile)).perform(click());
        onView(withId(R.id.profile_image)).check(matches(isDisplayed()));
    }

    @Test
    public void testProfileLongPressMenuOptions() {
        onView(withId(R.id.nav_admin_profile)).perform(longClick());
        onView(withText("Switch to Entrant")).perform(click());
        onView(withId(R.id.nav_entrant_home)).check(matches(isDisplayed())); // Ensure EntrantActivity loaded
    }

}