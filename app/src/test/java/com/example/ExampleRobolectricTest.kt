package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.MainViewModel
import com.example.ui.PersonLendingSummary
import com.example.ui.ledger.AddLedgerEntrySheet
import com.example.ui.lending.AddNewPersonLendingSheet
import com.example.ui.lending.QuickAddLendingSheet
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureTimeMillis

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Unspent", appName)
  }

  @Test
  fun `profile startup steps`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    val dbBuildTime = measureTimeMillis {
      AppDatabase.getDatabase(context)
    }
    println("TIMING: AppDatabase.getDatabase took: ${dbBuildTime}ms")

    val repoInitTime = measureTimeMillis {
      AppRepository(context)
    }
    println("TIMING: AppRepository init took: ${repoInitTime}ms")

    val vmInitTime = measureTimeMillis {
      MainViewModel(context as android.app.Application)
    }
    println("TIMING: MainViewModel init took: ${vmInitTime}ms")

    val controller = Robolectric.buildActivity(MainActivity::class.java)
    val createTime = measureTimeMillis {
      controller.create()
    }
    println("TIMING: Activity.create() took: ${createTime}ms")

    val startTime = measureTimeMillis {
      controller.start()
    }
    println("TIMING: Activity.start() took: ${startTime}ms")

    val resumeTime = measureTimeMillis {
      controller.resume()
    }
    println("TIMING: Activity.resume() took: ${resumeTime}ms")
  }

  @Test
  fun `quick add sheet opens repeatedly without crash`() {
    val summary = PersonLendingSummary(
      personId = 1L,
      personName = "Alice",
      netAmount = 250.0,
      openEntriesCount = 1,
      transactions = emptyList()
    )

    var isOpen by mutableStateOf(false)

    composeTestRule.setContent {
      MyApplicationTheme {
        if (isOpen) {
          QuickAddLendingSheet(
            personSummary = summary,
            onDismiss = { isOpen = false },
            onAdd = { _, _, _ -> },
            onViewLedger = {}
          )
        }
      }
    }

    // Rapidly toggle sheet open and closed 5 times to test timing and FocusRequester stability
    for (i in 1..5) {
      isOpen = true
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("quick_add_amount_input").assertIsDisplayed()

      isOpen = false
      composeTestRule.waitForIdle()
    }
  }

  @Test
  fun `add new person sheet opens repeatedly without crash`() {
    var isOpen by mutableStateOf(false)

    composeTestRule.setContent {
      MyApplicationTheme {
        if (isOpen) {
          AddNewPersonLendingSheet(
            existingSummaries = emptyList(),
            onDismiss = { isOpen = false },
            onSelectExisting = {},
            onAddNew = { _, _, _, _ -> }
          )
        }
      }
    }

    for (i in 1..5) {
      isOpen = true
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("lending_person_input").assertIsDisplayed()

      isOpen = false
      composeTestRule.waitForIdle()
    }
  }

  @Test
  fun `add ledger entry sheet opens repeatedly without crash`() {
    var isOpen by mutableStateOf(false)

    composeTestRule.setContent {
      MyApplicationTheme {
        if (isOpen) {
          AddLedgerEntrySheet(
            onDismiss = { isOpen = false },
            onAdd = { _, _, _ -> }
          )
        }
      }
    }

    for (i in 1..5) {
      isOpen = true
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("entry_amount_input").assertIsDisplayed()

      isOpen = false
      composeTestRule.waitForIdle()
    }
  }
}

