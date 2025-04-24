package com.example.goalmate.prenstatntion.groupsadd


import android.os.Build
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
import com.example.goalmate.viewmodel.MotivationQuoteViewModel
import com.example.goalmate.viewmodel.RegisterViewModel
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsAdd(
    navController: NavController,
    viewModel: GroupsAddViewModel = viewModel(),
    registerViewModel: RegisterViewModel = viewModel(),
    motivationQuoteViewModel: MotivationQuoteViewModel

) {
    var groupName by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Günlük") }
    var isPrivate by remember { mutableStateOf(false) }
    var participationType by remember { mutableStateOf("Herkes") }
    var minParticipantNumber by remember { mutableStateOf("2") }
    var maxParticipantNumber by remember { mutableStateOf("") }
    var startDelay by remember { mutableStateOf("1") }
    var selectedCategory by remember { mutableStateOf("Sağlık") }
    var groupDescription by remember { mutableStateOf("") }
    var habitHours by remember { mutableStateOf("") }
    var habitMinutes by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val groupCreationState by viewModel.groupCreationState.collectAsState()
    val joinedGroupsCount by registerViewModel.joinedGroupsCount.collectAsState()
    val maxAllowedGroups by registerViewModel.maxAllowedGroups.collectAsState()

    // Snackbar yönetimi
    LaunchedEffect(groupCreationState) {
        scope.launch {
            try {
                when(groupCreationState) {
                    is GroupCreationState.Success -> {
                        val message = (groupCreationState as GroupCreationState.Success).message
                            ?: "$groupName Grup başarıyla oluşturuldu!"
                        snackbarHostState.showSnackbar(message)
                    }
                    is GroupCreationState.Failure -> {
                        snackbarHostState.showSnackbar(
                            (groupCreationState as GroupCreationState.Failure).message
                        )
                    }
                    GroupCreationState.NoInternet -> {
                        snackbarHostState.showSnackbar("İnternet bağlantınızı kontrol edin")
                    }
                    GroupCreationState.Loading -> { }
                }
            } catch (e: Exception) {
                Log.e("GroupsAdd", "Snackbar gösterme hatası", e)
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
                        onClick = {
                            scope.launch {
                                try {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                } catch (e: Exception) {
                                    Log.e("GroupsAdd", "Snackbar kapatma hatası", e)
                                }
                                navController.popBackStack()
                            }
                        },
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
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
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
                    listOf("Sağlık", "Kişisel Gelişim", "Sosyal İlişkiler", "Finans","Kariyer" , "Teknoloji", "Çevre","Diğer").forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = colorResource(R.color.yazirengi)
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


                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Alışkanlık Süresi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.yazirengi)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = habitHours,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty()) {
                                        habitHours = ""
                                    } else {

                                        if (newValue.length == 1 && newValue[0].isDigit() && newValue[0].toString().toInt() <= 2) {
                                            habitHours = newValue
                                        }

                                        else if (newValue.length == 2) {
                                            val firstDigit = habitHours.first().toString().toInt()
                                            val secondDigit = newValue.last().toString().toInt()
                                            if ((firstDigit == 2 && secondDigit <= 3) || (firstDigit < 2)) {
                                                habitHours = newValue
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                label = { Text("Saat") },
                                placeholder = { Text("00-23") },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorResource(R.color.kutubordrengi),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Text(":", color = colorResource(R.color.yazirengi))

                            OutlinedTextField(
                                value = habitMinutes,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty()) {
                                        habitMinutes = ""
                                    } else {
                                        // İlk karakterin 0-5 olmasını kontrol et
                                        if (newValue.length == 1 && newValue[0].isDigit() && newValue[0].toString().toInt() <= 5) {
                                            habitMinutes = newValue
                                        }
                                        // İkinci karakterin 0-9 olmasını kontrol et
                                        else if (newValue.length == 2 && newValue.last().isDigit()) {
                                            habitMinutes = newValue
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                label = { Text("Dakika") },
                                placeholder = { Text("00-59") },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorResource(R.color.kutubordrengi),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    HorizontalDivider(thickness = 1.dp)

                    // Gizlilik Ayarı
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Gizlilik Ayarı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.yazirengi)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ElevatedFilterChip(
                                selected = !isPrivate,
                                onClick = {
                                    isPrivate = false
                                    participationType = "Herkes"
                                    Log.d("GroupsAdd", "Açık grup seçildi: isPrivate = $isPrivate")
                                },
                                label = { Text("🌎 Açık Grup") },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = colorResource(R.color.kutubordrengi),
                                    selectedLabelColor = Color.White
                                )
                            )
                            ElevatedFilterChip(
                                selected = isPrivate,
                                onClick = {
                                    isPrivate = true
                                    participationType = "Onay"
                                    Log.d("GroupsAdd", "Özel grup seçildi: isPrivate = $isPrivate")
                                },
                                label = { Text("🔒 Özel Grup") },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = colorResource(R.color.kutubordrengi),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(thickness = 1.dp)

                    // Katılım Türü
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Katılım Türü",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.yazirengi)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isPrivate) {
                                // Herkese açık grup için sadece "Herkes" seçeneği aktif
                                ElevatedFilterChip(
                                    selected = true,
                                    onClick = { participationType = "Herkes" },
                                    label = { Text("👥 Herkes") },
                                    colors = FilterChipDefaults.elevatedFilterChipColors(
                                        selectedContainerColor = colorResource(R.color.kutubordrengi),
                                        selectedLabelColor = Color.White
                                    )
                                )
                                // Diğer seçenekler deaktif
                                ElevatedFilterChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text("✉️ Davetle") },
                                    enabled = false,
                                    colors = FilterChipDefaults.elevatedFilterChipColors(
                                        selectedContainerColor = colorResource(R.color.kutubordrengi),
                                        selectedLabelColor = Color.White,
                                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                                        disabledLabelColor = Color.Gray
                                    )
                                )
                                ElevatedFilterChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text("✅ Onay") },
                                    enabled = false,
                                    colors = FilterChipDefaults.elevatedFilterChipColors(
                                        selectedContainerColor = colorResource(R.color.kutubordrengi),
                                        selectedLabelColor = Color.White,
                                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                                        disabledLabelColor = Color.Gray
                                    )
                                )
                            } else {
                                // Özel grup için "Herkes" seçeneği deaktif
                                ElevatedFilterChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text("👥 Herkes") },
                                    enabled = false,
                                    colors = FilterChipDefaults.elevatedFilterChipColors(
                                        selectedContainerColor = colorResource(R.color.kutubordrengi),
                                        selectedLabelColor = Color.White,
                                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                                        disabledLabelColor = Color.Gray
                                    )
                                )
                                // Diğer seçenekler aktif
                                ElevatedFilterChip(
                                    selected = participationType == "Davetle",
                                    onClick = { participationType = "Davetle" },
                                    label = { Text("✉️ Davetle") },
                                    colors = FilterChipDefaults.elevatedFilterChipColors(
                                        selectedContainerColor = colorResource(R.color.kutubordrengi),
                                        selectedLabelColor = Color.White
                                    )
                                )
                                ElevatedFilterChip(
                                    selected = participationType == "Onay",
                                    onClick = { participationType = "Onay" },
                                    label = { Text("✅ Onay") },
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
                        color = colorResource(R.color.yazirengi)
                    )
                    
                    // Maximum katılımcı sayısı
                    Column {
                        Text(
                            text = "Maximum Katılımcı Sayısı",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(R.color.yazirengi)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = maxParticipantNumber,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty()) {
                                        maxParticipantNumber = ""
                                    } else {
                                        val number = newValue.toIntOrNull()
                                        if (number != null) {
                                            if (number > 15) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Maximum katılımcı sayısı 15'ten büyük olamaz",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                maxParticipantNumber = "15"
                                            } else if (number <= 0) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Maximum katılımcı sayısı 1'den küçük olamaz",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                                maxParticipantNumber = "1"
                                            } else {
                                                maxParticipantNumber = number.toString()
                                            }
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
                                placeholder = { Text("Maximum katılımcı sayısı (1-15)") },
                                trailingIcon = { Text("kişi") }
                            )
                        }
                    }

                    // Grup başlangıç koşulu
                    Column {
                        Text(
                            text = "Grup Başlangıç Koşulu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(R.color.yazirengi)
                        )
                        val maxParticipants = maxParticipantNumber.toIntOrNull() ?: 0
                        val minStartParticipants = when {
                            maxParticipants <= 3 -> 2
                            maxParticipants > 3 -> maxParticipants / 2
                            else -> 2
                        }
                        Text(
                            text = if (maxParticipants > 0) {
                                "Grup, en az $minStartParticipants kişi katıldığında otomatik olarak başlayacaktır."
                            } else {
                                "Lütfen maksimum katılımcı sayısını belirleyin."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colorResource(R.color.pastelkirmizi),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            // Grup Başlangıç Zamanı Card
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
                        text = "Grup Başlangıç Zamanı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.yazirengi)
                    )
                    
                    Text(
                        text = "Grup faaliyetlerinin kaç gün sonra başlayacağını belirleyin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(R.color.yazirengi)
                    )
                    
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = startDelay,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Başlangıç Gecikmesi") },
                            trailingIcon = {
                                Row {
                                    Icon(
                                        Icons.Filled.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text("gün")
                                }
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorResource(R.color.kutubordrengi),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            placeholder = { Text("Başlangıç süresi seçin") }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            containerColor = colorResource(R.color.gri)
                        ) {
                            (1..5).forEach { day ->
                                DropdownMenuItem(
                                    text = { Text("$day gün sonra") },
                                    onClick = {
                                        startDelay = day.toString()
                                        expanded = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = colorResource(R.color.yazirengi)
                                    )
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = "Not: Grup minimum katılımcı sayısına ulaştığında, seçilen gecikme süresinden bağımsız olarak otomatik olarak başlayacaktır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorResource(R.color.pastelkirmizi),
                        modifier = Modifier.padding(top = 4.dp)
                    )
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
                        color = colorResource(R.color.yazirengi)
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
                        !registerViewModel.canJoinMoreGroups() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Maksimum grup limitine ulaştınız ($joinedGroupsCount/$maxAllowedGroups). " +
                                            "Yeni bir grup oluşturmak için önce bir gruptan ayrılmalısınız.",
                                    duration = SnackbarDuration.Long
                                )
                            }
                        }
                        groupName.isBlank() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Lütfen bir grup ismi girin",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        maxParticipantNumber.isBlank() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Lütfen maximum katılımcı sayısını belirtin",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        maxParticipantNumber.toInt() > 15 -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Maximum katılımcı sayısı 15'ten büyük olamaz",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        maxParticipantNumber.toInt() < 2 -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Grup oluşturulabilmesi için en az 2 katılımcı gerekmektedir.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            return@Button
                        }
                        startDelay.toIntOrNull() == null || startDelay.toInt() < 1 || startDelay.toInt() > 5 -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Grup başlangıç gecikmesi 1-5 gün arasında olmalıdır",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        groupDescription.isBlank() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Lütfen grup açıklamasını boş bırakmayın. Açıklama, grup hakkında bilgi vermek için önemlidir.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        habitHours.isBlank() && habitMinutes.isBlank() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Lütfen alışkanlık süresini belirtin",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        joinedGroupsCount >= maxAllowedGroups -> {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Maksimum grup limitine ulaştınız ($joinedGroupsCount/$maxAllowedGroups)",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        else -> {
                            // Grup oluşturma işlemi öncesi validation kontrolleri
                            when {
                                groupName.isBlank() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Lütfen grup adını boş bırakmayın",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }
                                maxParticipantNumber.isBlank() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Lütfen maksimum katılımcı sayısını belirtin",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }
                                maxParticipantNumber.toInt() > 15 -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Maximum katılımcı sayısı 15'ten büyük olamaz",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }
                                maxParticipantNumber.toInt() < 2 -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Grup oluşturulabilmesi için en az 2 katılımcı gerekmektedir.",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }
                                startDelay.toIntOrNull() == null || startDelay.toInt() < 1 || startDelay.toInt() > 5 -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Grup başlangıç gecikmesi 1-5 gün arasında olmalıdır",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                groupDescription.isBlank() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Lütfen grup açıklamasını boş bırakmayın",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }
                                habitHours.isBlank() && habitMinutes.isBlank() -> {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Lütfen alışkanlık süresini belirtin",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }
                            }

                            // Alışkanlık süresini hesapla
                            val totalMinutes = (habitHours.toIntOrNull() ?: 0) * 60 + (habitMinutes.toIntOrNull() ?: 0)
                            if (totalMinutes == 0) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Alışkanlık süresi 0 olamaz",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            // Grup oluşturma işlemi
                            scope.launch {
                                try {
                                    Log.d("GroupsAdd", "Grup oluşturma başlıyor:")
                                    Log.d("GroupsAdd", "Grup Adı: $groupName")
                                    Log.d("GroupsAdd", "Kategori: $selectedCategory")
                                    Log.d("GroupsAdd", "Sıklık: $frequency")
                                    Log.d("GroupsAdd", "Özel/Açık: $isPrivate")
                                    Log.d("GroupsAdd", "Katılım Türü: $participationType")
                                    Log.d("GroupsAdd", "Max Katılımcı: $maxParticipantNumber")
                                    Log.d("GroupsAdd", "Başlangıç Gecikmesi: $startDelay gün")
                                    
                                    val groupId = viewModel.createGroup(
                                        groupName = groupName,
                                        category = selectedCategory,
                                        frequency = frequency,
                                        isPrivate = isPrivate,
                                        participationType = participationType,
                                        maxParticipantNumber = maxParticipantNumber.toInt(),
                                        startDelay = startDelay.toInt(),
                                        habitDuration = totalMinutes.toString(),
                                        description = groupDescription,
                                        context = context
                                    )
                                    
                                    if (groupId != null) {
                                        Log.d("GroupsAdd", "Grup başarıyla oluşturuldu:")
                                        Log.d("GroupsAdd", "Grup ID: $groupId")
                                        Log.d("GroupsAdd", "Grup Türü: ${if (isPrivate) "Özel" else "Açık"}")
                                        Log.d("GroupsAdd", "Katılım Türü: $participationType")
                                        
                                        // Motivasyon sözünü kaydet
                                        motivationQuoteViewModel.saveQuoteForGroup(groupId = groupId, category = selectedCategory)
                                        
                                        // Eğer özel grupsa, grup kodu oluştur
                                        if (isPrivate) {
                                            registerViewModel.createGroupCode(groupId)
                                        }

                                        coroutineScope.launch {
                                            delay(1000)
                                            navController.popBackStack() // group oluşturulduktan sonra önceki sayfaya geri gelecek

                                        }

                                    }
                                } catch (e: Exception) {
                                    Log.e("GroupsAdd", "Grup oluşturma hatası", e)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Grup oluşturulurken bir hata oluştu: ${e.message}",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = registerViewModel.canJoinMoreGroups(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.kutubordrengi),
                    disabledContainerColor = Color.Gray
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Text(
                    text = "Grubu Oluştur ($joinedGroupsCount/$maxAllowedGroups)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

        }
    }
}

