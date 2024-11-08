package ca.yapper.yapperapp;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;

import org.junit.Test;
import org.junit.runner.RunWith;

import ca.yapper.yapperapp.Activities.EntrantActivity; // Ensure this is the correct package for your activity

import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class entrant_actlayoutTest {

    @Test
    public void testUIComponentsVisibility() {
        try (ActivityScenario<EntrantActivity> scenario = ActivityScenario.launch(EntrantActivity.class)) {
            Espresso.onView(withId(R.id.toolbar_logo))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            Espresso.onView(withId(R.id.fragment_container))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            Espresso.onView(withId(R.id.bottom_navigation))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                    .check(ViewAssertions.matches(ViewMatchers.isClickable()));
        }
    }
}
