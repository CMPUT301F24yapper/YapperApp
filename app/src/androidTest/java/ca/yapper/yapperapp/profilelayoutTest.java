package ca.yapper.yapperapp;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.yapper.yapperapp.Activities.EntrantActivity;

@RunWith(AndroidJUnit4.class)
public class profilelayoutTest {

    @Rule
    public ActivityScenarioRule<EntrantActivity> activityRule = new ActivityScenarioRule<>(EntrantActivity.class);

    /**
     * Test to check if clicking "Profile" in the navigation bar opens the profile layout.
     */
    @Test
    public void testOpenProfileLayoutFromNavBar() {
        // Click the profile icon in the navigation bar
        Espresso.onView(ViewMatchers.withId(R.id.nav_entrant_profile)).perform(ViewActions.click());

        // Check if the profile layout is displayed
        Espresso.onView(ViewMatchers.withId(R.id.upper_layout))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test to check visibility of profile picture and associated buttons (Change and Remove picture).
     */
    @Test
    public void testProfilePictureElementsVisible() {
        // Ensure profile layout is open
        Espresso.onView(ViewMatchers.withId(R.id.nav_entrant_profile)).perform(ViewActions.click());

        // Check if profile image is displayed
        Espresso.onView(ViewMatchers.withId(R.id.profile_image))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Check if 'Change Profile Picture' button is displayed
        Espresso.onView(ViewMatchers.withId(R.id.change_picture))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Check if 'Remove Profile Picture' button is displayed
        Espresso.onView(ViewMatchers.withId(R.id.remove_picture))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    /**
     * Test to verify the display and text in Name, Email, and Phone input fields.
     */
    @Test
    public void testInputFieldsDisplayAndText() {
        // Ensure profile layout is open
        Espresso.onView(ViewMatchers.withId(R.id.nav_entrant_profile)).perform(ViewActions.click());

        // Check if the name field is displayed
        Espresso.onView(ViewMatchers.withId(R.id.edit_name))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Check if the email field is displayed
        Espresso.onView(ViewMatchers.withId(R.id.edit_email))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Check if the phone number field is displayed and has a hint "Optional"
        Espresso.onView(ViewMatchers.withId(R.id.edit_phone))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .check(ViewAssertions.matches(ViewMatchers.withHint("Optional")));
    }

    /**
     * Test to check the functionality of the "Pause all" notifications switch.
     */
    @Test
    public void testNotificationsSwitch() {
        // Ensure profile layout is open
        Espresso.onView(ViewMatchers.withId(R.id.nav_entrant_profile)).perform(ViewActions.click());

        // Check if the notifications switch is displayed and perform a toggle action
        Espresso.onView(ViewMatchers.withId(R.id.switch_notifications))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .perform(ViewActions.click());

        // Optionally verify switch state (on/off) if required
    }

}
