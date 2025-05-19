package com.example.doancuoiky.Activity.Dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.doancuoiky.R
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// Retrofit API Interface
interface GeminiApi {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// Data classes for API request and response
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)

// Composable
@Composable
@Preview
fun TopBar() {
    var showChatDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<String>() } // Store chat history

    ConstraintLayout(
        modifier = Modifier
            .padding(top = 48.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        val (name, settings, chat) = createRefs()

        Image(
            painter = painterResource(R.drawable.settings_icon),
            contentDescription = null,
            modifier = Modifier
                .constrainAs(settings) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
                .clickable { /* Handle settings click */ }
        )

        Column(
            modifier = Modifier
                .constrainAs(name) {
                    top.linkTo(parent.top)
                    start.linkTo(settings.end)
                    end.linkTo(chat.start)
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append("Kebab Ngon")
                    }
                },
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = "Fantatic",
                color = Color.Black,
                fontSize = 14.sp
            )
        }

        Image(
            painter = painterResource(R.drawable.chat_icon), // Ensure this exists
            contentDescription = "Chat",
            modifier = Modifier
                .constrainAs(chat) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
                .clickable { showChatDialog = true }
        )
    }

    // Chat Dialog
    if (showChatDialog) {
        ChatDialog(
            messages = messages,
            onDismiss = { showChatDialog = false },
            onSendMessage = { message ->
                coroutineScope.launch {
                    try {
                        messages.add("You: $message")
                        val response = callGeminiApi(message)
                        messages.add("Gemini: $response")
                    } catch (e: Exception) {
                        messages.add("Error: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
fun ChatDialog(messages: MutableList<String>, onDismiss: () -> Unit, onSendMessage: (String) -> Unit) {
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chat with Gemini",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                // Chat history
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    reverseLayout = true // New messages at the bottom
                ) {
                    items(messages.reversed()) { msg ->
                        val isUserMessage = msg.startsWith("You:")
                        MessageBubble(
                            message = msg,
                            isUserMessage = isUserMessage
                        )
                    }
                }

                // Loading indicator
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Input field with embedded send button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        placeholder = { Text("Type a message...") },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (message.isNotBlank()) {
                                isLoading = true
                                onSendMessage(message)
                                message = ""
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send), // Add a send icon to res/drawable
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
fun MessageBubble(message: String, isUserMessage: Boolean) {
    val backgroundColor = if (isUserMessage) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
    val textColor = if (isUserMessage) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.secondary
    val alignment = if (isUserMessage) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = alignment
    ) {
        Text(
            text = message.removePrefix(if (isUserMessage) "You: " else "Gemini: "),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .padding(12.dp)
                .widthIn(max = 280.dp),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// Gemini API call
suspend fun callGeminiApi(message: String): String {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(GeminiApi::class.java)
    val request = GeminiRequest(
        contents = listOf(
            Content(
                parts = listOf(Part(text = message)) // Fixed: Correctly create parts list
            )
        )
    )

    val apiKey = "AIzaSyC1P462feXa26vUkLNRF3_FP_q-XtO7tKI"
    val response = api.generateContent(apiKey, request)
    return response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
        ?: "No response from Gemini"
}

@Preview
@Composable
fun ChatDialogPreview() {
    val messages = remember { mutableStateListOf("You: Hello", "Gemini: Hi there!") }
    ChatDialog(messages = messages, onDismiss = {}, onSendMessage = {})
}