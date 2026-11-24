package org.uyarvu.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UyarvuFunctionalIntegrationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun onboardingToDashboard_andAllTabsFunctionalFlow() {
        // 1. Onboarding screen appears on fresh launch
        composeRule.onNodeWithText("Welcome to UYARVU").assertIsDisplayed()
        composeRule.onNodeWithText("Preferred name").performTextInput("Kavya")
        composeRule.onNodeWithText("Start Learning").performClick()

        // 2. Personalized Today dashboard loads with student greeting
        composeRule.onNodeWithText("Hello, Kavya").assertIsDisplayed()

        // 3. Navigate to Study, add a subject, and log Present
        composeRule.onNodeWithText("Study").performClick()
        composeRule.onNodeWithText("Course Code (e.g. CS301)").performTextInput("CS301")
        composeRule.onNodeWithText("Subject Name").performTextInput("Data Structures")
        composeRule.onNodeWithText("Save Subject").performClick()
        composeRule.onNodeWithText("Present").performClick()
        composeRule.onNodeWithText("Conducted: 1 · Attended: 1 (100.0%)").assertIsDisplayed()

        // 4. Navigate to Grow and Guide to verify non-empty functional modules
        composeRule.onNodeWithText("Grow").performClick()
        composeRule.onNodeWithText("Completed Academic Tasks").assertIsDisplayed()

        composeRule.onNodeWithText("Guide").performClick()
        composeRule.onNodeWithText("How to Study Effectively (Active Recall)").assertIsDisplayed()
    }
}
