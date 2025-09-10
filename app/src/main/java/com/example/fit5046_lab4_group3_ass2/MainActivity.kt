package com.example.fit5046_lab4_group3_ass2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FIT5046Lab4Group3ass2Theme {
                Home()
                //ProfileSetup()
            }
        }
    }
}

@Composable
fun GeneralCard(modifier: Modifier = Modifier, title:String, mainText:String, smallText:String, progress:Float = 0f) {
    Card(modifier = Modifier.padding(bottom = 10.dp)) {
        Spacer(modifier = Modifier.height(10.dp))
        if (title != "")
            BoldText(text = title)
        if (mainText != "")
            BoldText(text = mainText)
        if (smallText != "")
            SmallText(text = smallText)
        if (progress != 0f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun BoldText(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun SmallText(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun Home(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "Good morning!",
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Lets track your eco impact today",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        GeneralCard(title = "EcoPoints", mainText = "2,450", smallText = "🔥 7-day streak")
        GeneralCard(title = "Electricity", mainText = "8.4 kWh", smallText = "Today's usage", progress = 0.8f)
        GeneralCard(title = "Plastic Saved", mainText = "2.1 kg", smallText = "This week = 7 bottles", progress = 0.5f)

        BoldText(text = "Quick Actions")
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(onClick = {}, modifier = Modifier.weight(1f)) {
                Text(
                    text = "Add Appliance"
                )
            }
            Button(onClick = {}, modifier = Modifier.weight(1f)) {
                Text(
                    text = "Log Purchase"
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(onClick = {}, modifier = Modifier.weight(1f)) {
                Text(
                    text = "View Tips"
                )
            }
            Button(onClick = {}, modifier = Modifier.weight(1f)) {
                Text(
                    text = "View Stats"
                )
            }
        }
        BoldText(text = "Recent Activity")
        GeneralCard(title = "Plastic bottle avoided", mainText = "2 hours ago", smallText = "+50 pts")
        GeneralCard(title = "Washing machine usage logged", mainText = "5 hours ago", smallText = "+25 pts")
        GeneralCard(title = "Today's Eco Tip", mainText = "", smallText = "Unplug devices when not in use")

    }
}

@Composable
fun ProfileSetup(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "Set Up Your Profile",
            modifier = Modifier
                .fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Step 1",
            modifier = Modifier
                .fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(10.dp))
        Card {
            Spacer(modifier = Modifier.height(10.dp))
            BoldText(text = "Household Information")
            SmallText(text="How many people live in your household?")
            OutlinedTextField(
                value = "",
                label = {SmallText(text="Enter number of people")},
                onValueChange = {},
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            SmallText(text="What type of home do you live in?")

            val radioOptions = listOf("Apartment", "Detached House", "Townhouse", "Other")
            val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
            // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
            Column(modifier.selectableGroup()) {
                radioOptions.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (text == selectedOption),
                                onClick = { onOptionSelected(text) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == selectedOption),
                            onClick = null // null recommended for accessibility with screen readers
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}