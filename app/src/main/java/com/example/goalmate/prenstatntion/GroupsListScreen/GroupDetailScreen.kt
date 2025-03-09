package com.example.goalmate.prenstatntion.GroupsListScreen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.goalmate.R
import com.example.goalmate.extrensions.GroupDetailState
import com.example.goalmate.viewmodel.GroupsAddViewModel

@Composable
fun GroupDetailScreen(
    groupId: String,
    navController: NavController,
    groupsAddViewModel: GroupsAddViewModel
) {

    val groupDetailState = groupsAddViewModel.groupDetailState.collectAsState().value

    LaunchedEffect(groupId) {
        groupsAddViewModel.getGroupById(groupId)
    }

    Column {
        IconButton(onClick = {
            navController.popBackStack()
        }) {
            Icon(painter = painterResource(R.drawable.back),
                contentDescription = "back"
            )
        }

        when (groupDetailState) {
            is GroupDetailState.Loading -> {
                Text(text = "Loading...")
            }
            is GroupDetailState.Success -> {
                // Display the group details
                val group = groupDetailState.group
               Log.e("Success","Success : $group")
            }
            is GroupDetailState.Error -> {
                // Display the error message
                Text(text = "Error: ${groupDetailState.message}")
            }
        }
    }
}