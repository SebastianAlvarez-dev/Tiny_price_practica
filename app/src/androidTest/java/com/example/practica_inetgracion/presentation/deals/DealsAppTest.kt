package com.example.practica_inetgracion.presentation.deals

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.example.practica_inetgracion.DealsApp
import com.example.practica_inetgracion.ui.theme.Practica_inetgracionTheme
import org.junit.Rule
import org.junit.Test

class DealsAppTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun startScreenShowsSearchAndSuggestion() {
        composeRule.setContent {
            Practica_inetgracionTheme {
                DealsApp(state = DealsUiState(), onEvent = {})
            }
        }
        composeRule.onNodeWithText("Buscar ofertas").assertIsDisplayed()
        composeRule.onNodeWithText("Nintendo Switch OLED").performScrollTo().assertIsDisplayed()
    }
}
