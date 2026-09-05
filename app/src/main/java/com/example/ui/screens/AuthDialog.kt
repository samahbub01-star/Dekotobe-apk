package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.User
import com.example.ui.theme.*

@Composable
fun AuthDialog(
    currentUser: User?,
    onDismiss: () -> Unit,
    onSignIn: (String) -> Unit,
    onSignUp: (String, String, String, String) -> Unit,
    onSignOut: () -> Unit,
    onSwitchToUser: (User) -> Unit,
    allUsers: List<User>
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signupUsername by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupChannel by remember { mutableStateOf("") }
    var signupRole by remember { mutableStateOf("creator") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = ZincSurface,
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_dialog")
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (currentUser != null) "Dekotube Account" else if (isSignUpMode) "Create Channel" else "Sign In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZincTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = ZincTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentUser != null) {
                    // Profile view when signed in
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(currentUser.avatarColor))
                                .border(2.dp, ZincBorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.channelName.take(1).uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentUser.channelName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZincTextPrimary
                        )
                        Text(
                            text = "@${currentUser.username} • ${currentUser.email}",
                            fontSize = 12.sp,
                            color = ZincTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = if (currentUser.role == "admin") SleekRedAccent.copy(alpha = 0.15f) else SleekIndigo.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentUser.role == "admin") Icons.Default.Shield else Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (currentUser.role == "admin") SleekRedAccent else SleekIndigo,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = currentUser.role.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentUser.role == "admin") SleekRedAccent else SleekIndigo
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Switch Accounts quick selector
                        Text("Switch Demo Accounts", fontSize = 12.sp, color = ZincTextSecondary, modifier = Modifier.align(Alignment.Start))
                        Spacer(modifier = Modifier.height(6.dp))
                        allUsers.forEach { user ->
                            Surface(
                                color = if (user.id == currentUser.id) ZincElevated else ZincBg,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (user.id == currentUser.id) SleekRedAccent else ZincBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onSwitchToUser(user) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(user.avatarColor)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(user.channelName.take(1).uppercase(), fontSize = 12.sp, color = Color.White)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(user.channelName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ZincTextPrimary)
                                        Text("${user.role} • ${user.subscribers} subs", fontSize = 11.sp, color = ZincTextSecondary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onSignOut,
                            colors = ButtonDefaults.buttonColors(containerColor = ZincElevated),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign Out", color = SleekRedAccent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else if (!isSignUpMode) {
                    // Sign In Form
                    OutlinedTextField(
                        value = usernameOrEmail,
                        onValueChange = { usernameOrEmail = it },
                        label = { Text("Username or Email") },
                        placeholder = { Text("e.g. admin or alex_tech") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signin_username_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekRedAccent,
                            unfocusedBorderColor = ZincBorder,
                            focusedContainerColor = ZincElevated,
                            unfocusedContainerColor = ZincElevated,
                            focusedTextColor = ZincTextPrimary,
                            unfocusedTextColor = ZincTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("Enter password") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekRedAccent,
                            unfocusedBorderColor = ZincBorder,
                            focusedContainerColor = ZincElevated,
                            unfocusedContainerColor = ZincElevated,
                            focusedTextColor = ZincTextPrimary,
                            unfocusedTextColor = ZincTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (usernameOrEmail.isNotBlank()) {
                                onSignIn(usernameOrEmail.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("signin_submit_button")
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Demo Credentials Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSignIn("admin")
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder)
                        ) {
                            Text("Demo Admin", fontSize = 11.sp, color = SleekRedAccent, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                onSignIn("alex_tech")
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder)
                        ) {
                            Text("Demo Creator", fontSize = 11.sp, color = SleekIndigo, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have a channel?", fontSize = 12.sp, color = ZincTextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Create one",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekIndigo,
                            modifier = Modifier.clickable { isSignUpMode = true }
                        )
                    }
                } else {
                    // Sign Up Form
                    OutlinedTextField(
                        value = signupUsername,
                        onValueChange = { signupUsername = it },
                        label = { Text("Username") },
                        placeholder = { Text("channel_handle") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekRedAccent,
                            unfocusedBorderColor = ZincBorder,
                            focusedContainerColor = ZincElevated,
                            unfocusedContainerColor = ZincElevated,
                            focusedTextColor = ZincTextPrimary,
                            unfocusedTextColor = ZincTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = signupChannel,
                        onValueChange = { signupChannel = it },
                        label = { Text("Channel Name") },
                        placeholder = { Text("My Awesome Channel") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekRedAccent,
                            unfocusedBorderColor = ZincBorder,
                            focusedContainerColor = ZincElevated,
                            unfocusedContainerColor = ZincElevated,
                            focusedTextColor = ZincTextPrimary,
                            unfocusedTextColor = ZincTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = signupEmail,
                        onValueChange = { signupEmail = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("creator@dekotube.io") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekRedAccent,
                            unfocusedBorderColor = ZincBorder,
                            focusedContainerColor = ZincElevated,
                            unfocusedContainerColor = ZincElevated,
                            focusedTextColor = ZincTextPrimary,
                            unfocusedTextColor = ZincTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Role", fontSize = 12.sp, color = ZincTextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = signupRole == "creator",
                            onClick = { signupRole = "creator" },
                            label = { Text("Content Creator") }
                        )
                        FilterChip(
                            selected = signupRole == "admin",
                            onClick = { signupRole = "admin" },
                            label = { Text("Platform Admin") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (signupUsername.isNotBlank()) {
                                onSignUp(
                                    signupUsername.trim(),
                                    signupEmail.ifBlank { "$signupUsername@dekotube.io" },
                                    signupChannel.ifBlank { signupUsername },
                                    signupRole
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("Create Channel & Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Already have a channel?", fontSize = 12.sp, color = ZincTextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sign in",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekIndigo,
                            modifier = Modifier.clickable { isSignUpMode = false }
                        )
                    }
                }
            }
        }
    }
}

