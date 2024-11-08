package ca.yapper.yapperapp;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Test;
import org.junit.runner.RunWith;

import ca.yapper.yapperapp.Activities.EntrantActivity;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class entrant_homepageTest {

    @Test
    public void testTabLayoutIsDisplayed() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            Espresso.onView(withId(R.id.tabLayout))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testTabRegisteredIsClickable() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            Espresso.onView(withText("Registered Events"))
                    .perform(click());
            Espresso.onView(withId(R.id.recyclerView_registered))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testTabJoinedIsClickable() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            Espresso.onView(withText("Joined Events"))
                    .perform(click());
            Espresso.onView(withId(R.id.recyclerView_joined))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testTabMissedOutIsClickable() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            Espresso.onView(withText("Missed Out"))
                    .perform(click());
            Espresso.onView(withId(R.id.recyclerView_missed))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }

    @Test
    public void testViewPagerIsDisplayed() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            Espresso.onView(withId(R.id.viewPager))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        }
    }
}
