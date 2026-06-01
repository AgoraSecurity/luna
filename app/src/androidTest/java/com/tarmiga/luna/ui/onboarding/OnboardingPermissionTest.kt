package com.tarmiga.luna.ui.onboarding

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tarmiga.luna.ui.theme.LunaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingPermissionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPermissionGrantedUI() {
        composeTestRule.setContent {
            LunaTheme {
                PermissionStepContent(
                    isPermissionGranted = true,
                    onEnableClick = {},
                    onFinished = {}
                )
            }
        }
        composeTestRule.setContent {
            PermissionStepContent(
                isPermissionGranted = true,
                onEnableClick = {},
                onFinished = {}
            )
        }

        // Status should be Done
        composeTestRule.onNodeWithText("Status: Done").assertIsDisplayed()
        
        // Finish button should be displayed
        composeTestRule.onNodeWithText("Finish").assertIsDisplayed()
        
        // Enable Notifications button should NOT be displayed
        composeTestRule.onNodeWithText("Enable Notifications").assertDoesNotExist()
        
        // Maybe Later button should NOT be displayed
        composeTestRule.onNodeWithText("Maybe Later").assertDoesNotExist()
    }

    @Test
    fun testPermissionPendingUI() {
        composeTestRule.setContent {
            PermissionStepContent(
                isPermissionGranted = false,
                onEnableClick = {},
                onFinished = {}
            )
        }

        // Status should be Permission pending
        composeTestRule.onNodeWithText("Status: Permission pending").assertIsDisplayed()
        
        // Enable Notifications button should be displayed
        composeTestRule.onNodeWithText("Enable Notifications").assertIsDisplayed()
        
        // Maybe Later button should be displayed
        composeTestRule.onNodeWithText("Maybe Later").assertIsDisplayed()
        
        // Finish button should NOT be displayed
        composeTestRule.onNodeWithText("Finish").assertDoesNotExist()
    }
}
