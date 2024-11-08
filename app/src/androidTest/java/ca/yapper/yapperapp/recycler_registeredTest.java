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
public class recycler_registeredTest {

    @Rule
    public ActivityScenarioRule<EntrantActivity> activityRule =
            new ActivityScenarioRule<>(EntrantActivity.class);

    @Test
    public void testRecyclerViewIsDisplayed() {
        Espresso.onView(withId(R.id.recyclerView_registered))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

}
