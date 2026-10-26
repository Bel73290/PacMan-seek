package com.cachecache.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cachecache.game.model.LobbyPlayer
import com.cachecache.game.model.LobbyState
import com.cachecache.game.ui.theme.*

private enum class PlayStep { CHOICE, JOIN }

/**
 * Dialogue ouvert par le bouton "Jouer" : créer une partie (génère un code à partager)
 * ou rejoindre une partie existante avec son code, puis salon d'attente en temps réel.
 */
@Composable
fun PlayModeDialog(
    lobby: LobbyState?,
    codeLength: Int,
    onDismiss: () -> Unit,
    onCreateGame: () -> Unit,
    onJoinGame: (String) -> Unit,
    onLaunchGame: () -> Unit,
    onClearError: () -> Unit,
    onReady: () -> Unit,
    onInviteFriends: () -> Unit,
    onSaveSettings: (Int, Int) -> Unit,
    hidingSeconds: Int,
    seekingMinutes: Int
) {
    var step by remember { mutableStateOf(PlayStep.CHOICE) }
    var joinCode by remember { mutableStateOf("") }
    val inLobby = lobby?.code != null
    val isLoading = lobby?.isLoading == true

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NavyIntermediate),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!inLobby && step == PlayStep.JOIN) {
                        IconButton(onClick = {
                            step = PlayStep.CHOICE
                            onClearError()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = TextSecondary)
                        }
                    }
                    Text(
                        text = when {
                            inLobby -> "Salon de la partie"
                            step == PlayStep.JOIN -> "Rejoindre une partie"
                            else -> "Jouer"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                when {
                    inLobby -> LobbyContent(
                        lobby = lobby!!,
                        onLaunchGame = onLaunchGame,
                        onReady = onReady,
                        onInviteFriends = onInviteFriends,
                        onSaveSettings = onSaveSettings,
                        hidingSeconds = hidingSeconds,
                        seekingMinutes = seekingMinutes
                    )

                    step == PlayStep.CHOICE -> {
                        PlayPillButton(
                            text = "Créer une partie",
                            primary = true,
                            loading = isLoading,
                            enabled = !isLoading,
                            onClick = onCreateGame
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PlayPillButton(
                            text = "Rejoindre avec un code",
                            primary = false,
                            enabled = !isLoading,
                            onClick = {
                                onClearError()
                                step = PlayStep.JOIN
                            }
                        )
                    }

                    else -> {
                        val isComplete = joinCode.length == codeLength
                        OutlinedTextField(
                            value = joinCode,
                            onValueChange = { input ->
                                joinCode = input.uppercase()
                                    .filter { it.isLetterOrDigit() }
                                    .take(codeLength)
                                onClearError()
                            },
                            placeholder = {
                                Text(
                                    "Code de la partie",
                                    color = TextMuted,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            singleLine = true,
                            enabled = !isLoading,
                            textStyle = LocalTextStyle.current.copy(
                                color = TextPrimary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 6.sp,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                autoCorrectEnabled = false,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(onGo = { if (isComplete) onJoinGame(joinCode) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PacmanYellow,
                                unfocusedBorderColor = BorderSubtle,
                                cursorColor = PacmanYellow,
                                focusedContainerColor = CardSurface,
                                unfocusedContainerColor = CardSurface,
                                disabledContainerColor = CardSurface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PlayPillButton(
                            text = "Rejoindre",
                            primary = true,
                            loading = isLoading,
                            enabled = isComplete && !isLoading,
                            onClick = { onJoinGame(joinCode) }
                        )
                    }
                }

                lobby?.error?.let { error ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = error,
                        color = GhostRed,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun LobbyContent(
    lobby: LobbyState,
    onLaunchGame: () -> Unit,
    onReady: () -> Unit,
    onInviteFriends: () -> Unit,
    onSaveSettings: (Int, Int) -> Unit,
    hidingSeconds: Int,
    seekingMinutes: Int
) {
    val clipboard = LocalClipboardManager.current
    val code = lobby.code.orEmpty()

    Text(
        text = if (lobby.isHost) "Partage ce code avec tes amis" else "Code de la partie",
        color = TextSecondary,
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(12.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface, RoundedCornerShape(16.dp))
            .border(1.dp, PacmanYellow.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = code,
            color = PacmanYellow,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 6.sp
        )
    }
    if (lobby.isHost) {
        TextButton(onClick = { clipboard.setText(AnnotatedString(code)) }) {
            Text("Copier le code", color = TextSecondary)
        }
    } else {
        Spacer(modifier = Modifier.height(12.dp))
    }

    Text(
        text = "Joueurs (${lobby.players.size})",
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(lobby.players, key = { it.id }) { player -> LobbyPlayerRow(player) }
    }

    Spacer(modifier = Modifier.height(16.dp))
    OutlinedButton(onClick=onInviteFriends,modifier=Modifier.fillMaxWidth()) { Text("Inviter mes amis") }
    if (!lobby.isHost) {
        Button(onClick=onReady) { Text(if(lobby.players.firstOrNull { it.isMe }?.ready == true) "Je ne suis plus prêt" else "Je suis prêt") }
    }
    if (lobby.isHost) {
        Text("Cache : ${hidingSeconds / 60} min • Chasse : $seekingMinutes min", color=TextSecondary)
        Row {
            TextButton(onClick={onSaveSettings((hidingSeconds-60).coerceAtLeast(60),seekingMinutes)}){Text("Cache −")}
            TextButton(onClick={onSaveSettings((hidingSeconds+60).coerceAtMost(600),seekingMinutes)}){Text("Cache +")}
            TextButton(onClick={onSaveSettings(hidingSeconds,(seekingMinutes-1).coerceAtLeast(1))}){Text("Chasse −")}
            TextButton(onClick={onSaveSettings(hidingSeconds,(seekingMinutes+1).coerceAtMost(60))}){Text("Chasse +")}
        }

        PlayPillButton(
            text = "Lancer la partie",
            primary = true,
            enabled = lobby.players.size >= 2 && lobby.players.all { it.ready },
            onClick = onLaunchGame
        )
        if (lobby.players.size < 2) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("En attente d'au moins un autre joueur…", color = TextMuted, fontSize = 13.sp)
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                color = PacmanYellow,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("En attente du lancement par l'hôte…", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun LobbyPlayerRow(player: LobbyPlayer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (player.isMe) SelectedSurface else CardSurface,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(if (player.isHost) PacmanYellow else GhostCyan, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (player.isMe) "${player.name} (toi)" else player.name,
            color = TextPrimary,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Text(if(player.isHost) "Hôte · prêt" else if(player.ready) "Prêt" else "En attente", color = if(player.ready) GhostCyan else TextMuted, fontSize = 12.sp)

    }
}

@Composable
private fun PlayPillButton(
    text: String,
    primary: Boolean,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(if (primary) Modifier else Modifier.border(1.2.dp, BorderSubtle, RoundedCornerShape(26.dp))),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) PacmanYellow else Color(0xFF0D1C38),
            contentColor = if (primary) DarkContrast else TextPrimary,
            disabledContainerColor = if (primary) PacmanYellow.copy(alpha = 0.3f) else Color(0xFF0D1C38).copy(alpha = 0.5f),
            disabledContentColor = if (primary) DarkContrast.copy(alpha = 0.6f) else TextMuted
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = DarkContrast,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}
