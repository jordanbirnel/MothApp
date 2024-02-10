package demo.pnw.moths.app.ui;

import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;


import demo.pnw.moths.app.MainActivity;
import demo.pnw.moths.app.R;

@RunWith(AndroidJUnit4.class)
public class ListFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> activityActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);


    @Before
    public void init(){

        //db init is a singleton use it anywhere
        DatabaseManager dbManager = new DatabaseManager(InstrumentationRegistry.getInstrumentation().getTargetContext());

        //insert testMoth data into DB to display in the list view
        dbManager.insert(new MothObservation(0, "testMoth", "WA", "", "", "", "", 49.0, 49.0, false));
        dbManager.insert(new MothObservation(0, "testMoth2", "WA", "", "", "", "", 49.0, 50.0, false));

        //navigate to the list view
        onView(withId(R.id.navigation_settings)).perform(click());
    }

    @Test
    public void TestAutoComplete(){

        //check that base scrollView displays
        onView(withId(R.id.momentScroll)).check(matches((isDisplayed())));

        //check if upload and delete buttons for testMoth are being loaded
        onView(withId(100)).check(matches((not(doesNotExist()))));
        onView(withId(101)).check(matches((not(doesNotExist()))));
        //check if upload and delete buttons for testMoth2 are being loaded
        onView(withId(200)).check(matches(not(doesNotExist())));
        onView(withId(201)).check(matches(not(doesNotExist())));

    }
}