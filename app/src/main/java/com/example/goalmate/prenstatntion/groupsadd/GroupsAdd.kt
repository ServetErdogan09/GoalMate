package com.example.goalmate.prenstatntion.groupsadd


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.goalmate.R
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.example.goalmate.extrensions.GroupCreationState
import com.example.goalmate.viewmodel.GroupsAddViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsAdd(
    navController: NavController,
    viewModel: GroupsAddViewModel = viewModel(),
) {
    var groupName by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Günlük") }
    var participationType by remember { mutableStateOf("Onay") }
    var isPrivate by remember { mutableStateOf(false) }
    var participantNumber by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Spor") }
    var groupDescription by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val textColor = colorResource(R.color.yazirengi)

    val context = LocalContext.current
    
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val groupCreationState by viewModel.groupCreationState.collectAsState()


    LaunchedEffect(groupCreationState) {
        when(groupCreationState){
            is GroupCreationState.Success ->{
                snackbarHostState.showSnackbar(
                    (groupCreationState as GroupCreationState.Success).message
                        ?: "$groupName Grup başarıyla oluşturuldu!"
                )
            }

            is GroupCreationState.Failure -> {
                snackbarHostState.showSnackbar(
                    (groupCreationState as GroupCreationState.Failure).message
                )
            }
            GroupCreationState.Loading -> {


            }
            GroupCreationState.NoInternet -> {
                snackbarHostState.showSnackbar("İnternet bağlantınızı kontrol edin")
            }

        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colorResource(R.color.arkaplan),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.graphicsLayer {
                            alpha = 1f - scrollBehavior.state.collapsedFraction
                            translationY = -50f * scrollBehavior.state.collapsedFraction
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Geri dön",
                            tint = colorResource(id = R.color.yazirengi)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth(),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Grup İsmi
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Grup İsmi") },
                placeholder = { Text("Örn: Sabah Koşu Grubu") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(R.color.kutubordrengi),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Kategori
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { 
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(R.color.kutubordrengi),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = colorResource(R.color.gri)
                ) {
                    listOf("Spor", "Eğitim", "Sanat", "Teknoloji", "Seyahat","Diğer").forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = textColor
                            )
                        )
                    }
                }
            }

            // Grup Ayarları Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colorResource(R.color.arkaplan)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Alışkanlık Sıklığı
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Alışkanlık Sıklığı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.yazirengi)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Günlük", "Haftalık", "Aylık").forEach { option ->
                                ElevatedFilterChip(
                                    selected = frequency == option,
                                    onClick = { frequency = option },
                                    label = { Text(option) },
                                    colors = FilterChipDefaults.elevatedFilterChipColors(
                                        selectedContainerColor = colorResource(R.color.kutubordrengi),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Divider(thickness = 1.dp)

                    // Gizlilik Ayarı
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Gizlilik Ayarı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ElevatedFilterChip(
                                selected = !isPrivate,
                                onClick = { isPrivate = false },
                                label = { Text("🌎 Açık Grup") },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = colorResource(R.color.kutubordrengi),
                                    selectedLabelColor = Color.White
                                )
                            )
                            ElevatedFilterChip(
                                selected = isPrivate,
                                onClick = { isPrivate = true },
                                label = { Text("🔒 Özel Grup") },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = colorResource(R.color.kutubordrengi),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Divider(thickness = 1.dp)

                    // Katılım Türü
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Katılım Türü",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "👥 Herkes" to "Herkes",
                                "✉️ Davetle" to "Davetle",
                                "✅ Onay" to "Onay"
                            ).forEach { (label, value) ->
                                ElevatedFilterChip(
                                    selected = participationType == value,
                                    onClick = { participationType = value },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.elevatedFilterChipColors(
                                        selectedContainerColor = colorResource(R.color.kutubordrengi),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Katılımcı Sayısı Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colorResource(R.color.arkaplan)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Katılımcı Sayısı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = participantNumber,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty()) {
                                    participantNumber = ""
                                } else {
                                    val number = newValue.toIntOrNull()
                                    if (number != null && number <= 30) {
                                        participantNumber = number.toString()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.kutubordrengi),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            placeholder = { Text("Katılımcı sayısını girin (2-30)") },
                            trailingIcon = { Text("kişi") }
                        )
                    }
                }
            }

            // Grup Açıklaması
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colorResource(R.color.arkaplan)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Grup Açıklaması",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    OutlinedTextField(
                        value = groupDescription,
                        onValueChange = { groupDescription = it },
                        placeholder = { Text("Bu grupta neler yapılacak?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 6,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(R.color.kutubordrengi),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            // Oluştur Butonu
            Button(
                onClick = { 
                    when {
                        groupName.isBlank() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Lütfen bir grup ismi girin",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        (participantNumber.toIntOrNull() ?: 0) < 2 -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Grup en az 2 kişi olmalıdır",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        else -> {
                            viewModel.createGroup(
                                groupName = groupName,
                                category = selectedCategory,
                                frequency = frequency,
                                isPrivate = isPrivate,
                                participationType = participationType,
                                participantNumber = participantNumber.toInt(),
                                description = groupDescription,
                                context = context
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.kutubordrengi)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Text(
                    text = "Grubu Oluştur",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

