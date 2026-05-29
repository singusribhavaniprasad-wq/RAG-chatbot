package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ChatEntry
import com.example.data.Document
import com.example.data.RAGRepository
import kotlinx.coroutines.launch
import java.text.DecimalFormat

// --- Elegant Color Palette ---
val DeepNavy = Color(0xFFFEF7FF) // Main screen background (light lavender/pinkish-white)
val CardSlate = Color(0xFFF3EDF7) // Main card and input container color (light purple-grey)
val BorderSlate = Color(0xFFCAC4D0) // Border stroke line color (refined Material 3 divider)
val TextLight = Color(0xFF1D1B20) // Primary high-contrast title & body text (almost black/deep plum)
val TextMuted = Color(0xFF49454F) // Description, hint & secondary text (medium grey/purple)
val AccentCyan = Color(0xFF6750A4) // Primary brand accent (Royal Purple)
val AccentGreen = Color(0xFF2E7D32) // Positive action, success indicator (refined dark green)
val AccentOrange = Color(0xFFED6C02) // Warning state / incomplete state (vibrant orange contrast)
val AccentRed = Color(0xFFB3261E) // Error, delete state, hallucination (Material 3 red)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RAGDashboard(viewModel: RAGViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val apiKeyState by viewModel.apiKey.collectAsStateWithLifecycle()
    var isEditingKey by remember { mutableStateOf(false) }
    var tempKey by remember { mutableStateOf(apiKeyState) }

    val ragState by viewModel.ragState.collectAsStateWithLifecycle()
    val isEmbedding by viewModel.isEmbedding.collectAsStateWithLifecycle()
    val progress by viewModel.embeddingProgress.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "R",
                                color = DeepNavy,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = "RAG Architect",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentGreen)
                                )
                                Text(
                                    text = "ENGINE ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardSlate)
                            .clickable { isEditingKey = !isEditingKey }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Key Status",
                            tint = if (apiKeyState.trim().isNotEmpty()) AccentGreen else AccentOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (apiKeyState.trim().isNotEmpty()) "API KEY LOADED" else "NO API KEY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (apiKeyState.trim().isNotEmpty()) AccentGreen else AccentOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavy)
            )
        },
        containerColor = DeepNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // API Key Drawer configuration
            AnimatedVisibility(visible = isEditingKey) {
                Surface(
                    color = CardSlate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    border = BorderStroke(1.dp, BorderSlate)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Configure Gemini API Key Safely",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextLight
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BorderSlate)
                        Text(
                            text = "Enter a custom key below if you wish to override the injected secret. This key executes API Calls entirely server-side.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = tempKey,
                                onValueChange = { tempKey = it },
                                label = { Text("Gemini API Key") },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextLight),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("api_key_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentCyan,
                                    unfocusedBorderColor = BorderSlate,
                                    focusedLabelColor = AccentCyan,
                                    unfocusedLabelColor = TextMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateApiKey(tempKey)
                                    isEditingKey = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                                modifier = Modifier.testTag("save_key_button")
                            ) {
                                Text("Save", color = DeepNavy)
                            }
                        }
                    }
                }
            }

            // Global State banners
            AnimatedVisibility(
                visible = isEmbedding,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                LinearProgressIndicator(
                    progress = { if (progress.second > 0) progress.first.toFloat() / progress.second else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = AccentCyan,
                    trackColor = BorderSlate,
                )
            }

            // Tab Rows
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DeepNavy,
                contentColor = AccentCyan,
                divider = { HorizontalDivider(color = BorderSlate) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("RAG Chat", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (selectedTab == 0) AccentCyan else TextMuted) },
                    icon = { Icon(Icons.Default.Send, contentDescription = "", modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Documents", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (selectedTab == 1) AccentCyan else TextMuted) },
                    icon = { Icon(Icons.Default.List, contentDescription = "", modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Semantic Seek", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (selectedTab == 2) AccentCyan else TextMuted) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "", modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Evaluation", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (selectedTab == 3) AccentCyan else TextMuted) },
                    icon = { Icon(Icons.Default.Info, contentDescription = "", modifier = Modifier.size(18.dp)) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DeepNavy)
            ) {
                when (selectedTab) {
                    0 -> ChatTab(viewModel)
                    1 -> DocumentsTab(viewModel)
                    2 -> DirectSearchTab(viewModel)
                    3 -> EvaluationTab(viewModel)
                }
            }
        }
    }
}

// ==========================================
// 1. CHAT WORKSPACE TAB
// ==========================================
@Composable
fun ChatTab(viewModel: RAGViewModel) {
    val chatEntries by viewModel.chatEntries.collectAsStateWithLifecycle()
    val ragState by viewModel.ragState.collectAsStateWithLifecycle()
    val searchMode by viewModel.searchMode.collectAsStateWithLifecycle()
    val lastGroundedDocs by viewModel.lastRetrievedDocs.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    var inputQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // To auto scroll on new message
    LaunchedEffect(chatEntries.size, ragState) {
        if (chatEntries.isNotEmpty()) {
            listState.animateScrollToItem(chatEntries.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle Settings Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSlate)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "RAG Mode: ", fontSize = 12.sp, color = TextMuted)
                Text(
                    text = if (searchMode == SearchMode.LOCAL_TFIDF) "TF-IDF Vector Space (Offline)" else "Dense API Embeddings (Cloud)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BorderSlate)
                    .clickable {
                        viewModel.toggleSearchMode(
                            if (searchMode == SearchMode.LOCAL_TFIDF) SearchMode.CLOUD_DENSE else SearchMode.LOCAL_TFIDF
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "",
                    tint = TextLight,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Toggle", fontSize = 10.sp, color = TextLight, fontWeight = FontWeight.Bold)
            }
        }

        // Messages Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (chatEntries.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CardSlate,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "",
                                tint = AccentCyan,
                                modifier = Modifier
                                    .padding(20.dp)
                                    .size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Query the Knowledge Base",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Type any developmental, architectural, or debugging question about Jetpack Compose and Gemini. DocuRAG will retrieve 3 matching documents to answer query.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                items(chatEntries) { entry ->
                    MessageRow(entry = entry)
                }
            }

            // Loading steps
            if (ragState != RAGState.Idle) {
                item {
                    val stepText = when (ragState) {
                        RAGState.Retrieving -> "Searching vector space for matching documentation..."
                        RAGState.Generating -> "Retrieval complete. Synthesizing grounded response using model..."
                        RAGState.Evaluating -> "Evaluating faithfulness and auto-generating Q&A tags..."
                        else -> "Processing..."
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSlate.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, BorderSlate.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = AccentCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stepText,
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Active retrieved groundings display
        val lastWarning by viewModel.denseWarning.collectAsStateWithLifecycle()
        if (lastGroundedDocs.isNotEmpty() || lastWarning != null) {
            Surface(
                color = CardSlate.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT TURN GROUNDING BLOCKS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "",
                            tint = AccentCyan,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    if (lastWarning != null) {
                        Text(
                            text = lastWarning!!,
                            color = AccentOrange,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        items(lastGroundedDocs) { (doc, score) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                                border = BorderStroke(1.dp, BorderSlate)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (score > 0.45f) AccentGreen else AccentCyan)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = doc.title,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = TextLight,
                                        modifier = Modifier.widthIn(max = 140.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = " (${(score * 100).toInt()}% match)",
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Query entry bar
        Surface(
            color = CardSlate,
            border = BorderStroke(1.dp, BorderSlate)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("Ask about state, DB, testing or plugins...", color = TextMuted) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextLight),
                    maxLines = 3,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputQuery.trim().isNotEmpty() && ragState == RAGState.Idle) {
                                viewModel.askQuestion(inputQuery)
                                inputQuery = ""
                                keyboardController?.hide()
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = BorderSlate,
                        focusedContainerColor = DeepNavy,
                        unfocusedContainerColor = DeepNavy
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputQuery.trim().isNotEmpty() && ragState == RAGState.Idle) {
                            viewModel.askQuestion(inputQuery)
                            inputQuery = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputQuery.trim().isNotEmpty()) AccentCyan else BorderSlate)
                        .testTag("send_query_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Query",
                        tint = if (inputQuery.trim().isNotEmpty()) DeepNavy else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun MessageRow(entry: ChatEntry) {
    val isUser = entry.role == "user"
    val align = if (isUser) Alignment.End else Alignment.Start
    val bg = if (isUser) AccentCyan else CardSlate
    val textColor = if (isUser) DeepNavy else TextLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = align
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Surface(
                    shape = CircleShape,
                    color = AccentCyan.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "",
                        tint = AccentCyan,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(bg)
                    .padding(12.dp)
            ) {
                Text(
                    text = entry.content,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Render metrics logs if model response has evaluations populated
                if (!isUser && entry.isEvaluated) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = BorderSlate.copy(alpha = 0.5f)
                    )
                    
                    // Comma tags
                    if (!entry.qnaTags.isNullOrEmpty()) {
                        Text(
                            text = "Tags auto-extracted:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            entry.qnaTags.split(",").forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BorderSlate)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = tag.trim(), fontSize = 9.sp, color = TextLight)
                                }
                            }
                        }
                    }

                    // Score badges for precision, mrr, faithfulness, relevance, hallucination
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ScoreBadge(label = "Faithful", score = entry.faithfulnessScore)
                        ScoreBadge(label = "Relevance", score = entry.relevanceScore)
                        ScoreBadge(
                            label = "Hallucination", 
                            score = entry.hallucinationRate, 
                            negative = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBadge(label: String, score: Float, negative: Boolean = false) {
    val formatted = DecimalFormat("#.##").format(score)
    val color = if (negative) {
        if (score > 0.3f) AccentRed else AccentGreen
    } else {
        if (score > 0.7f) AccentGreen else if (score > 0.40f) AccentOrange else AccentRed
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(DeepNavy)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$label: $formatted", fontSize = 8.sp, color = TextLight)
    }
}


// ==========================================
// 2. KNOWLEDGE BASE / DOCUMENTS TAB
// ==========================================
@Composable
fun DocumentsTab(viewModel: RAGViewModel) {
    val documents by viewModel.allDocuments.collectAsStateWithLifecycle()
    var searchKeyword by remember { mutableStateOf("") }
    var showIngestDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Stats bar & Actions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            border = BorderStroke(1.dp, BorderSlate)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "STRUCTURED KNOWLEDGE BASE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                        Text(text = "${documents.size} Total Documents loaded", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextLight)
                        val embeddedCount = documents.count { !it.embeddingJson.isNullOrEmpty() }
                        Text(text = "$embeddedCount with active Dense vectors", fontSize = 11.sp, color = TextMuted)
                    }
                    Button(
                        onClick = { viewModel.startBulkEmbedding() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.testTag("bulk_embed_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "", tint = DeepNavy, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compute Vectors", color = DeepNavy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Search Keyword Field & Add button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchKeyword,
                onValueChange = { searchKeyword = it },
                placeholder = { Text("Filter documents by metadata/text...", color = TextMuted) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextLight),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("doc_search_field"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "", tint = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = BorderSlate,
                    focusedContainerColor = CardSlate,
                    unfocusedContainerColor = CardSlate
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { showIngestDialog = true },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AccentCyan)
                    .testTag("add_doc_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Document", tint = DeepNavy)
            }
        }

        // Dynamic Document Ingestion Dialog Form
        if (showIngestDialog) {
            IngestDocumentDialog(
                viewModel = viewModel,
                onDismiss = { showIngestDialog = false }
            )
        }

        // Filter and list documents
        val filteredDocs = remember(documents, searchKeyword) {
            if (searchKeyword.isBlank()) documents
            else {
                documents.filter { doc ->
                    doc.title.contains(searchKeyword, ignoreCase = true) ||
                    doc.content.contains(searchKeyword, ignoreCase = true) ||
                    doc.category.contains(searchKeyword, ignoreCase = true) ||
                    doc.tags.contains(searchKeyword, ignoreCase = true)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredDocs) { doc ->
                DocumentCard(doc = doc, onDeleteClick = { viewModel.deleteDocument(doc.id) })
            }
        }
    }
}

@Composable
fun DocumentCard(doc: Document, onDeleteClick: () -> Unit) {
    val isEmbedded = !doc.embeddingJson.isNullOrEmpty()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.category.uppercase(),
                        color = AccentCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = doc.title,
                        color = TextLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Vector Status badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isEmbedded) AccentGreen.copy(alpha = 0.2f) else AccentOrange.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isEmbedded) "DENSE VECTOR" else "RAW STRING",
                            color = if (isEmbedded) AccentGreen else AccentOrange,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AccentRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = doc.content,
                color = TextLight.copy(alpha = 0.85f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Sub terms tags list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                doc.tags.split(",").take(5).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DeepNavy)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = tag.trim(), fontSize = 9.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun IngestDocumentDialog(
    viewModel: RAGViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Compose Layout") }
    var content by remember { mutableStateOf("") }
    var inProgress by remember { mutableStateOf(false) }

    val categories = listOf("Compose State", "Compose Performance", "Compose Layout", "Room DB", "Retrofit", "Testing Practice")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Ingest New Document", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        containerColor = CardSlate,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "The content will automatically be tokenized, tags generated via the Content Tagging System, and vector embedding computed.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextLight),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ingest_title_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = BorderSlate
                    )
                )

                // Category selector
                Column {
                    Text("Select Category:", fontSize = 11.sp, color = TextMuted)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (category == cat) AccentCyan else DeepNavy)
                                    .clickable { category = cat }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (category == cat) DeepNavy else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content / Troubleshooting Q&A") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextLight),
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ingest_content_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = BorderSlate
                    )
                )
                if (inProgress) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extracting semantic tags & embeddings...", fontSize = 11.sp, color = AccentCyan)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isNotEmpty() && content.trim().isNotEmpty()) {
                        inProgress = true
                        viewModel.ingestCustomDocument(title, category, content) { success ->
                            inProgress = false
                            if (success) onDismiss()
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                modifier = Modifier.testTag("ingest_confirm_button")
            ) {
                Text("Ingest", color = DeepNavy)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

// ==========================================
// 3. SEMANTIC SEEK SEARCH TEST TOOL
// ==========================================
@Composable
fun DirectSearchTab(viewModel: RAGViewModel) {
    val documents by viewModel.allDocuments.collectAsStateWithLifecycle()
    val searchMode by viewModel.searchMode.collectAsStateWithLifecycle()
    val apiKeyState by viewModel.apiKey.collectAsStateWithLifecycle()

    var queryText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pair<Document, Float>>>(emptyList()) }
    var inProgress by remember { mutableStateOf(false) }
    var currentSeekWarning by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val repository = RAGRepository(LocalContext.current)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = "SEMANTIC RETRIEVAL WORKSPACE",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = AccentCyan
        )
        Text(
            text = "Test and evaluate your semantic vector lookup values. Query the exact indexing algorithm and see similarity percentages below.",
            color = TextMuted,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Query entry card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardSlate),
            border = BorderStroke(1.dp, BorderSlate),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = { Text("Enter a concept e.g. state flow recompose...", color = TextMuted) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextLight),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seek_search_query_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = BorderSlate,
                        focusedContainerColor = DeepNavy,
                        unfocusedContainerColor = DeepNavy
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Method select toggles
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepNavy)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (searchMode == SearchMode.LOCAL_TFIDF) AccentCyan else Color.Transparent)
                                .clickable { viewModel.toggleSearchMode(SearchMode.LOCAL_TFIDF) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "TF-IDF Vector",
                                color = if (searchMode == SearchMode.LOCAL_TFIDF) DeepNavy else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (searchMode == SearchMode.CLOUD_DENSE) AccentCyan else Color.Transparent)
                                .clickable { viewModel.toggleSearchMode(SearchMode.CLOUD_DENSE) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Dense API Vector",
                                color = if (searchMode == SearchMode.CLOUD_DENSE) DeepNavy else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (queryText.trim().isNotEmpty()) {
                                inProgress = true
                                currentSeekWarning = null
                                scope.launch {
                                    if (searchMode == SearchMode.LOCAL_TFIDF) {
                                        searchResults = repository.retrieveLocalTfIdf(queryText, topK = 4)
                                    } else {
                                        val parseResult = repository.retrieveDenseVector(apiKeyState, queryText, topK = 4)
                                        searchResults = parseResult.first
                                        currentSeekWarning = parseResult.second
                                    }
                                    inProgress = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !inProgress && queryText.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                        modifier = Modifier.testTag("seek_test_lookup_button")
                    ) {
                        Text("Lookup", color = DeepNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (inProgress) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentCyan)
            }
        } else {
            if (currentSeekWarning != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentOrange.copy(alpha = 0.15f))
                        .border(1.dp, AccentOrange, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = currentSeekWarning!!,
                        color = AccentOrange,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No vector retrieval queries run yet.", color = TextMuted, fontSize = 12.sp)
                }
            } else {
                Text(
                    text = "RETIEVAL RESULTS (PRECISION SEARCH)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = AccentCyan,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchResults) { (doc, score) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CardSlate),
                            border = BorderStroke(1.dp, BorderSlate)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = doc.category.uppercase(), fontSize = 9.sp, color = AccentCyan)
                                        Text(text = doc.title, fontWeight = FontWeight.Bold, color = TextLight, fontSize = 13.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (score > 0.45f) AccentGreen.copy(alpha = 0.2f) else AccentCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${(score * 100).toInt()}% match",
                                            color = if (score > 0.45f) AccentGreen else AccentCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = doc.content,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = TextLight.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Vector Dimension Tags: ${doc.tags}",
                                    fontSize = 9.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. METRICS / EVALUATION DASHBOARD TAB
// ==========================================
@Composable
fun EvaluationTab(viewModel: RAGViewModel) {
    val logs by viewModel.evaluatedEntries.collectAsStateWithLifecycle()

    var docAggregateCount by remember { mutableStateOf(0) }
    val docsState by viewModel.allDocuments.collectAsStateWithLifecycle()

    LaunchedEffect(docsState.size) {
        docAggregateCount = docsState.size
    }

    // Filter models
    val modelLogs = remember(logs) {
        logs.filter { it.role == "model" && it.isEvaluated }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "RAG RETRIEVAL & QUALITY EVALUATION",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = AccentCyan
        )

        if (modelLogs.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, BorderSlate),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "",
                        tint = AccentOrange,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "RAG Metrics Log is Empty",
                        color = TextLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Start chat conversations to compute real-time evaluation scores. Every model turn calculates prompt grounded correctness, hallucinations, and search accuracy metrics.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Aggregate Math
            val aggregateSize = modelLogs.size.toFloat()
            val avgPrecision = modelLogs.map { it.precisionAtK }.sum() / aggregateSize
            val avgRecall = modelLogs.map { it.recallAtK }.sum() / aggregateSize
            val avgMRR = modelLogs.map { it.mrrScore }.sum() / aggregateSize
            val avgFaithful = modelLogs.map { it.faithfulnessScore }.sum() / aggregateSize
            val avgRelevance = modelLogs.map { it.relevanceScore }.sum() / aggregateSize
            val avgHallucination = modelLogs.map { it.hallucinationRate }.sum() / aggregateSize

            // Layout Gauges Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RETRIEVAL SYSTEM QUALITY",
                        fontSize = 10.sp,
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        CircularScoreGauge(score = avgPrecision, label = "Precision@K", color = AccentCyan)
                        CircularScoreGauge(score = avgRecall, label = "Recall@K", color = AccentGreen)
                        CircularScoreGauge(score = avgMRR, label = "Mean MRR", color = AccentOrange)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GENERATED ANSWER GROUNDING",
                        fontSize = 10.sp,
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        CircularScoreGauge(score = avgFaithful, label = "Faithfulness", color = AccentGreen)
                        CircularScoreGauge(score = avgRelevance, label = "Relevance", color = AccentCyan)
                        CircularScoreGauge(
                            score = avgHallucination, 
                            label = "Hallucination Rate", 
                            color = AccentRed,
                            isNegativeMetric = true
                        )
                    }
                }
            }

            // Historical evaluation list
            Text(
                text = "HISTORICAL RUN ANALYTICS LOG",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = AccentCyan
            )

            modelLogs.sortedByDescending { it.timestamp }.forEach { run ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    border = BorderStroke(1.dp, BorderSlate)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RUN #${run.id}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCyan,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Session: ${run.sessionId}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Answer: ${run.content}",
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom metrics breakdown tag logs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "R: ${(run.relevanceScore * 100).toInt()}%", fontSize = 9.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                                Text(text = "|", fontSize = 9.sp, color = BorderSlate)
                                Text(text = "F: ${(run.faithfulnessScore * 100).toInt()}%", fontSize = 9.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
                                Text(text = "|", fontSize = 9.sp, color = BorderSlate)
                                Text(
                                    text = "H: ${(run.hallucinationRate * 100).toInt()}%", 
                                    fontSize = 9.sp, 
                                    color = if (run.hallucinationRate > 0.1f) AccentRed else AccentGreen, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (!run.qnaTags.isNullOrEmpty()) {
                                Text(
                                    text = run.qnaTags, 
                                    color = TextMuted, 
                                    fontSize = 9.sp, 
                                    maxLines = 1, 
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 140.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Global DB clear reset action
        Button(
            onClick = { viewModel.resetAllData() },
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AccentRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .testTag("reset_db_button"),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Power Clean Workspace (Reset Database)", color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Custom Jetpack Compose Canvas Gauge (Renders gorgeous circle rings)
@Composable
fun CircularScoreGauge(
    score: Float, 
    label: String, 
    color: Color,
    isNegativeMetric: Boolean = false
) {
    val rawPercentage = (score * 100).toInt().coerceIn(0, 100)
    
    // Invert rating display color if it is a negative metric like Hallucination
    val displayColor = if (isNegativeMetric) {
        if (score > 0.3f) AccentRed else AccentGreen
    } else {
        color
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(48.dp)) {
                // Background Track ring
                drawCircle(
                    color = BorderSlate,
                    radius = size.minDimension / 2,
                    style = Stroke(width = 4.dp.toPx())
                )
                
                // Foreground Progress ring
                drawArc(
                    color = displayColor,
                    startAngle = -90f,
                    sweepAngle = score * 360f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${rawPercentage}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )
    }
}
