package br.com.ruan.fit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ruan.fit.data.*
import br.com.ruan.fit.domain.Progress
import br.com.ruan.fit.media.PlayerState
import java.text.DateFormat
import java.util.Date

@Composable internal fun WorkoutPage(state: FitUiState, session: WorkoutSession?, model: FitViewModel, onRestStart: (Int) -> Unit, onRestStop: () -> Unit, onMediaPermission: () -> Unit) {
    val timer by model.timer.state.collectAsStateWithLifecycle()
    val player by model.media.state.collectAsStateWithLifecycle()
    var restSeconds by rememberSaveable { mutableStateOf("90") }
    if (session == null) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            PageHeading("HORA DE SE MOVER", "Treino", "Seu próximo passo começa em uma ficha.")
            EmptyState("▶", "Nenhum treino em andamento", "Escolha uma ficha na aba Fichas e toque em Iniciar.")
        }
        return
    }
    val links = state.planExercises.filter { it.planId == session.planId }.sortedBy { it.position }
    val completed = state.sets.count { it.sessionId == session.id && it.completed }
    val total = links.sumOf { it.targetSets }
    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f).padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                PageHeading("EM ANDAMENTO", "Hora de treinar", "Registre cada série e acompanhe sua evolução.")
                Surface(shape = RoundedCornerShape(22.dp), color = Ink) {
                    Column(Modifier.fillMaxWidth().padding(20.dp)) {
                        Text("TREINO ATUAL", color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                        Spacer(Modifier.height(7.dp))
                        Text(session.planName, color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(6.dp))
                        Text("Iniciado em ${dateTime(session.startedAt)}", color = Color(0xFFBBCAC1), style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(18.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("$completed / $total séries", color = Lime, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            TextButton(onClick = { model.finish(session.id) }, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) { Text("Finalizar  ↗") }
                        }
                        LinearProgressIndicator(progress = { if (total == 0) 0f else completed.toFloat() / total }, modifier = Modifier.fillMaxWidth().height(6.dp), color = Lime, trackColor = Color(0xFF38504A), gapSize = 0.dp, drawStopIndicator = {})
                    }
                }
                SectionHeading("Descanso", "RECUPERE O FÔLEGO")
                FitCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(Lime, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) { Text("◷", style = MaterialTheme.typography.titleLarge) }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(if (timer.running) "Contagem em andamento" else "Pronto para a próxima série", style = MaterialTheme.typography.titleMedium)
                            Text(if (timer.running) "Respire e recupere" else "Defina o tempo e comece", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Numeric("Segundos", restSeconds, { restSeconds = it }, Modifier.width(125.dp))
                        Button(onClick = { onRestStart(restSeconds.toIntOrNull() ?: 90) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(13.dp)) { Text(if (timer.running) "Reiniciar" else "Iniciar") }
                    }
                    if (timer.running) {
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${timer.remainingSeconds}s restantes", Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = Pine)
                            TextButton(onClick = onRestStop) { Text("Cancelar") }
                        }
                    }
                }
                SectionHeading("Exercícios", "${links.size} no plano")
            }
            if (links.isEmpty()) item { EmptyState("＋", "Sem exercícios nesta ficha", "Adicione exercícios na aba Fichas para registrar suas séries.") }
            items(links, key = { it.id }) { link ->
                val exercise = state.exercises.firstOrNull { it.id == link.exerciseId }
                if (exercise != null) FitCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(37.dp).background(CanvasColor, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) { Text("${link.position + 1}", color = Pine, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(11.dp))
                        Text(exercise.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(10.dp))
                    LabelPill("META  ${link.targetSets} × ${link.targetReps}  ·  ${link.targetLoad} kg", strong = true)
                    Spacer(Modifier.height(10.dp))
                    repeat(link.targetSets) { index ->
                        val number = index + 1
                        val saved = state.sets.firstOrNull { it.sessionId == session.id && it.planExerciseId == link.id && it.setNumber == number }
                        SetEditor(session.id, exercise, number, link, saved, state.sets, model)
                    }
                }
            }
        }
        MediaBar(player, model, onMediaPermission)
    }
}

@Composable private fun SetEditor(sessionId: Long, exercise: Exercise, number: Int, link: PlanExercise, saved: SetLog?, allSets: List<SetLog>, model: FitViewModel) {
    var load by remember(sessionId, exercise.id, number, saved?.id) { mutableStateOf((saved?.load ?: link.targetLoad).toString()) }
    var reps by remember(sessionId, exercise.id, number, saved?.id) { mutableStateOf((saved?.reps ?: link.targetReps).toString()) }
    var rpe by remember(sessionId, exercise.id, number, saved?.id) { mutableStateOf(saved?.rpe?.toString().orEmpty()) }
    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(color = Line)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("SÉRIE ${number.toString().padStart(2, '0')}", Modifier.weight(1f), color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            if (saved?.completed == true) LabelPill("✓  CONCLUÍDA", strong = true)
        }
        Spacer(Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Numeric("kg", load, { load = it }, Modifier.weight(1f))
            Numeric("reps", reps, { reps = it }, Modifier.weight(1f))
            Numeric("RPE", rpe, { rpe = it }, Modifier.weight(1f))
        }
        val parsedLoad = load.replace(',', '.').toDoubleOrNull()
        val parsedReps = reps.toIntOrNull()
        val parsedRpe = if (rpe.isBlank()) null else rpe.replace(',', '.').toDoubleOrNull()
        val valid = parsedLoad != null && parsedLoad >= 0 && parsedReps != null && parsedReps > 0 && (rpe.isBlank() || parsedRpe != null && parsedRpe in 1.0..10.0)
        TextButton(onClick = { model.saveSet(sessionId, link.id, exercise, number, parsedLoad ?: 0.0, parsedReps ?: 0, parsedRpe) }, enabled = valid, contentPadding = PaddingValues(horizontal = 0.dp)) { Text(if (saved == null) "Concluir série  →" else "Atualizar série  →") }
        if (saved != null && Progress.isNewRecord(saved, allSets.filter { it.id != saved.id })) Text("★  Novo recorde pessoal", color = Pine, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun MediaBar(player: PlayerState, model: FitViewModel, onMediaPermission: () -> Unit) {
    Surface(color = Color.White, shadowElevation = 4.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (player.artwork != null) Image(player.artwork.asImageBitmap(), contentDescription = "Capa do álbum", modifier = Modifier.size(42.dp))
                else Box(Modifier.size(42.dp).background(CanvasColor, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) { Text("♫", style = MaterialTheme.typography.titleLarge, color = Pine) }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (player.available) player.title else "Sua música", maxLines = 1, fontWeight = FontWeight.SemiBold)
                    Text(if (player.available) player.artist else "Nenhum player ativo", maxLines = 1, color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
                val hasMediaAccess by model.media.access.collectAsStateWithLifecycle()
                if (!hasMediaAccess) TextButton(onClick = onMediaPermission) { Text("Ativar") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { model.media.volume(false) }, modifier = Modifier.size(40.dp)) { Text("−", style = MaterialTheme.typography.titleLarge) }
                IconButton(onClick = model.media::previous, enabled = player.available, modifier = Modifier.size(40.dp)) { Text("⏮", fontSize = 19.sp) }
                FilledIconButton(onClick = model.media::playPause, enabled = player.available, modifier = Modifier.size(42.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Lime, contentColor = Ink)) { Text(if (player.playing) "Ⅱ" else "▶") }
                IconButton(onClick = model.media::next, enabled = player.available, modifier = Modifier.size(40.dp)) { Text("⏭", fontSize = 19.sp) }
                IconButton(onClick = { model.media.volume(true) }, modifier = Modifier.size(40.dp)) { Text("+", style = MaterialTheme.typography.titleLarge) }
            }
        }
    }
}

@Composable internal fun HistoryPage(state: FitUiState, selectedId: Long, onSelect: (Long) -> Unit) {
    val selected = state.sessions.firstOrNull { it.id == selectedId }
    val sessions = state.sessions.filter { it.endedAt != null }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            if (selected != null) TextButton(onClick = { onSelect(0) }, contentPadding = PaddingValues(0.dp)) { Text("‹  Todos os treinos") }
            PageHeading("SUA JORNADA", if (selected == null) "Histórico" else selected.planName, if (selected == null) "Cada treino conta. Veja sua trajetória." else "${dateTime(selected.startedAt)}  ·  ${duration(selected.startedAt, selected.endedAt)}")
            SectionHeading(if (selected == null) "Treinos concluídos" else "Séries registradas", if (selected == null) "${sessions.size} sessões" else null)
        }
        if (selected != null) {
            val logs = state.sets.filter { it.sessionId == selected.id && it.completed }
            if (logs.isEmpty()) item { EmptyState("◷", "Sem séries registradas", "Este treino foi concluído sem registros de séries.") }
            items(logs, key = { it.id }) { log -> FitCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(37.dp).background(CanvasColor, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) { Text("${log.setNumber}", fontWeight = FontWeight.Bold, color = Pine) }
                    Spacer(Modifier.width(11.dp))
                    Text(log.exerciseName, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(11.dp))
                LabelPill("${log.reps} reps  ·  ${log.load} kg${log.rpe?.let { "  ·  RPE $it" } ?: ""}", strong = true)
                if (Progress.isNewRecord(log, state.sets.filter { it.id != log.id })) { Spacer(Modifier.height(9.dp)); Text("★  Recorde pessoal", color = Pine, fontWeight = FontWeight.SemiBold) }
            } }
        } else {
            if (sessions.isEmpty()) item { EmptyState("◷", "Sua história começa agora", "Conclua um treino para acompanhar seu desempenho aqui.") }
            items(sessions, key = { it.id }) { session -> FitCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(session.planName, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(dateTime(session.startedAt), color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                    LabelPill(duration(session.startedAt, session.endedAt), strong = true)
                }
                Spacer(Modifier.height(7.dp))
                TextButton(onClick = { onSelect(session.id) }, contentPadding = PaddingValues(horizontal = 0.dp)) { Text("Ver séries  →") }
            } }
        }
    }
}

@Composable internal fun ProgressPage(state: FitUiState) {
    val logs = state.sets.filter { it.completed && it.reps > 0 }
    val records = Progress.records(logs)
    val exerciseIds = logs.map { it.exerciseId }.distinct()
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PageHeading("CADA REPETIÇÃO SOMA", "Progresso", "Acompanhe suas melhores marcas ao longo do tempo.")
            Surface(shape = RoundedCornerShape(22.dp), color = Ink) {
                Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("SUA EVOLUÇÃO", color = Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("${exerciseIds.size} exercícios", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        Text("com séries registradas", color = Color(0xFFBBCAC1), style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("↗", color = Lime, fontSize = 40.sp, fontWeight = FontWeight.Light)
                }
            }
            SectionHeading("Evolução de carga")
        }
        if (logs.isEmpty()) item { EmptyState("↗", "Ainda sem dados", "Registre séries concluídas durante um treino para ver sua evolução.") }
        items(exerciseIds) { exerciseId ->
            val exerciseLogs = logs.filter { it.exerciseId == exerciseId }.sortedBy { it.recordedAt }
            val name = state.exercises.firstOrNull { it.id == exerciseId }?.name ?: exerciseLogs.first().exerciseName
            val best = records[exerciseId]
            FitCard {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(name, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("Maior carga registrada", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                    LabelPill("${best?.load ?: 0.0} kg", strong = true)
                }
                Spacer(Modifier.height(8.dp))
                LoadChart(exerciseLogs)
                Text("${dateTime(exerciseLogs.first().recordedAt)}  →  ${dateTime(exerciseLogs.last().recordedAt)}", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable private fun LoadChart(logs: List<SetLog>) {
    val values = logs.groupBy { it.sessionId }.values.map { group -> group.maxOf { it.load } }
    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 0.0
    Canvas(Modifier.fillMaxWidth().height(130.dp).padding(vertical = 12.dp)) {
        val left = 12.dp.toPx(); val right = size.width - 12.dp.toPx()
        val top = 8.dp.toPx(); val bottom = size.height - 8.dp.toPx()
        drawLine(Line, androidx.compose.ui.geometry.Offset(left, bottom), androidx.compose.ui.geometry.Offset(right, bottom), 1.dp.toPx())
        val points = values.mapIndexed { index, value ->
            androidx.compose.ui.geometry.Offset(
                if (values.size == 1) (left + right) / 2 else left + (right - left) * index / (values.size - 1),
                if (max == min) (top + bottom) / 2 else bottom - (bottom - top) * ((value - min) / (max - min)).toFloat()
            )
        }
        val path = Path()
        points.forEachIndexed { index, point -> if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y) }
        drawPath(path, Pine, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
        points.forEach { drawCircle(Pine, radius = 4.dp.toPx(), center = it) }
    }
    Text("${min} kg — ${max} kg", style = MaterialTheme.typography.bodySmall)
}

@Composable private fun Numeric(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(value, onChange, modifier, label = { Text(label) }, singleLine = true, shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}
private fun dateTime(epoch: Long): String = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(epoch))
private fun duration(start: Long, end: Long?): String = if (end == null) "Em andamento" else "${((end - start) / 60000).coerceAtLeast(0)} min"
