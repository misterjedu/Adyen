package com.adyen.android.assignment

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.adyen.android.assignment.ui.MainActivity
import com.adyen.android.assignment.util.OkHttpProvider
import com.jakewharton.espresso.OkHttp3IdlingResource
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

//    @get:Rule
//    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
//        android.Manifest.permission.ACCESS_FINE_LOCATION,
//        android.Manifest.permission.ACCESS_NETWORK_STATE
//    )
//
//    @Test
//    fun should_displayMap_when_permissionsAreGranted() {
//        ActivityScenario.launch(MainActivity::class.java)
//
//        onView(withId(R.id.map))
//            .check(matches(isDisplayed()))
//    }


    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java, true, false)

    private val mockWebServer = MockWebServer()

    @Before
    fun setup() {
        mockWebServer.start(8080)
        IdlingRegistry.getInstance().register(
            OkHttp3IdlingResource.create(
                "okhttp",
                OkHttpProvider.getOkHttpClient()
            )
        )
    }

    @Test
    fun testSuccessfulResponse200() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    .setResponseCode(200)
                    .setBody(FileReader.readStringFromFile("success_response.json"))
            }
        }

        activityRule.launchActivity(null)

        onView(withId(R.id.activity_main_loading_layout))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.activity_main_venue_recycler))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

    }

    @Test
    fun testNotSuccessfulResponse300() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    .setResponseCode(300)
                    .setBody(FileReader.readStringFromFile("300_response.json"))
            }
        }
        activityRule.launchActivity(null)

        onView(withId(R.id.activity_main_loading_layout))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.activity_main_venue_recycler))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.activity_main_offline_layout))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.activity_main_offline_text))
            .check(matches(withText(R.string.redirection_error)))
    }


    @Test
    fun testNotSuccessfulResponse400() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    .setResponseCode(400)
                    .setBody(FileReader.readStringFromFile("400_response.json"))
            }
        }
        activityRule.launchActivity(null)

        onView(withId(R.id.activity_main_loading_layout))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.activity_main_venue_recycler))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.activity_main_offline_layout))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.activity_main_offline_text))
            .check(matches(withText(R.string.bad_request)))
    }


    @Test
    fun testNotSuccessfulResponse500() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    .setResponseCode(500)
                    .setBody(FileReader.readStringFromFile("500_response.json"))
            }
        }
        activityRule.launchActivity(null)

        onView(withId(R.id.activity_main_loading_layout))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.activity_main_venue_recycler))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.activity_main_offline_layout))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.activity_main_offline_text))
            .check(matches(withText(R.string.server_error)))
    }


    @Test
    fun testFailedCannotConnect() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse()
                    .throttleBody(1024, 10, TimeUnit.SECONDS)
            }
        }

        activityRule.launchActivity(null)

        onView(withId(R.id.activity_main_loading_layout))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.activity_main_venue_recycler))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withId(R.id.activity_main_offline_layout))
            .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.activity_main_offline_text))
            .check(matches(withText(R.string.seems_you_are_offline)))
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }


}