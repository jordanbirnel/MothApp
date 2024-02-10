package demo.pnw.moths.app.ui;

import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import org.junit.runner.RunWith;

//import androidx.navigation.testing.TestNavHostController;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;


import junit.framework.TestCase;

import demo.pnw.moths.app.MainActivity;

@RunWith(AndroidJUnit4.class)
public class IDkeyFragmentTest extends TestCase {
    @Rule
    public ActivityTestRule<MainActivity> activityActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Before
    public void init(){
        //onView(withId(R.id.navigation_settings)).perform(click());
    }

    @Test
    public void TestAutoComplete(){
        onView(withId(demo.pnw.moths.app.R.id.imageMothButton0)).check(matches((isDisplayed())));
        onView(withId(1000)).check(matches((isDisplayed())));
        onView(withId(1000)).perform(click());
        }
}