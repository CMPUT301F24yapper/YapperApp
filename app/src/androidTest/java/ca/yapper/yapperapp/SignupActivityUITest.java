package ca.yapper.yapperapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import ca.yapper.yapperapp.Activities.SignupActivity;

/**
 * UI Test class for SignupActivity.
 * Covers basic sign-up functionality.
 */
public class SignupActivityUITest {

    @Rule
    public ActivityScenarioRule<SignupActivity> activityRule =
            new ActivityScenarioRule<>(SignupActivity.class);

    /**
     * Test: Verify the signup button is displayed.
     */
    @Test
    public void testSignupButtonDisplayed() {
        onView(withId(R.id.signup_button)).perform(scrollTo()).check(matches(isDisplayed()));
    }


    @Test
    public void testInputFieldsDisplayed() {
        onView(withId(R.id.name_input)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.email_input)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.phone_input)).perform(scrollTo()).check(matches(isDisplayed()));
    }
}