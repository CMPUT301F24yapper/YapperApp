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
public class recycler_joinedTest {

    @Rule
    public ActivityScenarioRule<EntrantActivity> activityRule =
            new ActivityScenarioRule<>(EntrantActivity.class);

    @Test
    public void testRecyclerViewIsDisplayed() {
        // Check if the RecyclerView for joined events is displayed
        Espresso.onView(withId(R.id.recyclerView_joined))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testRecyclerViewHasNoItemsInitially() {
        // Check if the RecyclerView is empty initially (assuming the initial data is not populated)
        Espresso.onView(withId(R.id.recyclerView_joined))
                .check(ViewAssertions.matches(ViewMatchers.hasChildCount(0)));
    }
}
