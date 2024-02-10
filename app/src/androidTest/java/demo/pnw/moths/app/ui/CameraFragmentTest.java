package demo.pnw.moths.app.ui;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import demo.pnw.moths.app.MainActivity;
import demo.pnw.moths.app.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class CameraFragmentTest {
    @Rule
    public ActivityTestRule<MainActivity> activityActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);


    @Before
    public void init(){

        //db init is a singleton use it anywhere
        DatabaseManager dbManager = new DatabaseManager(InstrumentationRegistry.getInstrumentation().getTargetContext());
        //testMoth
        dbManager.insert(new MothObservation(0, "testMoth", "WA", "", "", "", "", 49.0, 49.0, false));
        onView(withId(R.id.navigation_notifications)).perform(click());
        //.replace(R.id.nav_host_fragment_container, ListFragment.class, null);//.replace(R.id.nav_host_fragment_container, ListFragment.class,null);
    }

    @Test
    public void TestAutoComplete(){
        //testing camera UI
        onView(withId(R.id.iconImage1)).check(matches((isDisplayed())));
        onView(withId(R.id.iconImage2)).check(matches((isDisplayed())));
        onView(withId(R.id.iconImage3)).check(matches((isDisplayed())));
        onView(withId(R.id.iconImage4)).check(matches((isDisplayed())));



    }

}