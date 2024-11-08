package ca.yapper.yapperapp; // Ensure this is the correct package for your tests

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Test;
import org.junit.runner.RunWith;

import ca.yapper.yapperapp.Activities.EntrantActivity; // Adjust the import as necessary

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class entrant_homepageTest {

    @Test
    public void testTabLayoutIsDisplayed() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Verify that the TabLayout is displayed
            Espresso.onView(withId(R.id.tabLayout))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testTabRegisteredIsClickable() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Click on the "Registered Events" tab
            Espresso.onView(withText("Registered Events"))
                    .perform(click());
            // Verify that the tab is displayed (you could add more checks for content)
            Espresso.onView(withId(R.id.tabLayout))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testTabJoinedIsClickable() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Click on the "Joined Events" tab
            Espresso.onView(withText("Joined Events"))
                    .perform(click());
            // Verify that the tab is displayed (you could add more checks for content)
            Espresso.onView(withId(R.id.tabLayout))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testTabMissedOutIsClickable() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Click on the "Missed Out" tab
            Espresso.onView(withText("Missed Out"))
                    .perform(click());
            // Verify that the tab is displayed (you could add more checks for content)
            Espresso.onView(withId(R.id.tabLayout))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testViewPagerIsDisplayed() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            // Verify that the ViewPager2 is displayed
            Espresso.onView(withId(R.id.viewPager))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }
}
