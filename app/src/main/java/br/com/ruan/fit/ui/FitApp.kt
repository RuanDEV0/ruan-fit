package br.com.ruan.fit.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ruan.fit.data.*

private enum class Page(val label: String) { PLANS("Fichas"), LIBRARY("Exercícios"), WORKOUT("Treino"), HISTORY("Histórico"), PROGRESS("Progresso") }

@Composable
fun FitApp(model: FitViewModel, onRestStart: (Int) -> Unit, onRestStop: () -> Unit, onMediaPermission: () -> Unit) {
    val state by model.ui.collectAsStateWithLifecycle()
    val error by model.error.collectAsStateWithLifecycle()
    var pageName by rememberSaveable { mutableStateOf(Page.PLANS.name) }
    var selectedPlanId by rememberSaveable { mutableLongStateOf(0L) }
    var selectedHistoryId by rememberSaveable { mutableLongStateOf(0L) }
    val page = Page.valueOf(pageName)
    val active = state.sessions.firstOrNull { it.endedAt == null }

    Scaffold(
        containerColor = CanvasColor,
        topBar = {
            Surface(color = CanvasColor) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(39.dp).background(Ink, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("RF", color = Lime, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("RUAN FIT", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, letterSpacing = 0.3.sp)
                        Text("SEU ESPAÇO DE TREINO", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
                    }
                    if (active != null) LabelPill("● EM TREINO", strong = true)
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                Page.entries.forEach { item ->
                    NavigationBarItem(
                        selected = page == item,
                        onClick = { pageName = item.name; if (item == Page.PLANS) selectedPlanId = 0 },
                        icon = { NavGlyph(item.name.lowercase()) },
                        label = { Text(item.label, fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Ink, selectedTextColor = Ink, indicatorColor = Lime, unselectedIconColor = Muted, unselectedTextColor = Muted)
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (error != null) Surface(color = MaterialTheme.colorScheme.errorContainer) {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(error.orEmpty(), Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = model::clearError) { Text("Fechar") }
                }
            }
            when (page) {
                Page.PLANS -> if (selectedPlanId == 0L) PlansPage(state, model, onOpen = { selectedPlanId = it }, onStart = { model.start(it); pageName = Page.WORKOUT.name })
                    else PlanDetailPage(state, selectedPlanId, model, onBack = { selectedPlanId = 0 }, onStart = { model.start(it); pageName = Page.WORKOUT.name })
                Page.LIBRARY -> LibraryPage(state, model)
                Page.WORKOUT -> WorkoutPage(state, active, model, onRestStart, onRestStop, onMediaPermission)
                Page.HISTORY -> HistoryPage(state, selectedHistoryId, onSelect = { selectedHistoryId = it })
                Page.PROGRESS -> ProgressPage(state)
            }
        }
    }
}

@Composable private fun NumberField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(value, onChange, modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, shape = RoundedCornerShape(14.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}

@Composable private fun PlansPage(state: FitUiState, model: FitViewModel, onOpen: (Long) -> Unit, onStart: (WorkoutPlan) -> Unit) {
    var edit by remember { mutableStateOf<WorkoutPlan?>(null) }
    var creating by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PageHeading("ORGANIZE SUA ROTINA", "Suas fichas", "Escolha um plano e entre no ritmo.")
            Button(onClick = { creating = true }, shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp)) { Text("+  Nova ficha") }
            SectionHeading("Seus planos", "${state.plans.size} ${if (state.plans.size == 1) "ficha" else "fichas"}")
        }
        if (state.plans.isEmpty()) item { EmptyState("▦", "Comece pelo primeiro plano", "Crie uma ficha para organizar seus exercícios e iniciar um treino.") }
        items(state.plans, key = { it.id }) { plan ->
            val count = state.planExercises.count { it.planId == plan.id }
            FitCard {
                Row(verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(46.dp).background(Lime, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                        Text("${state.plans.indexOf(plan) + 1}", style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(plan.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(3.dp))
                        Text(plan.split.ifBlank { "Sua rotina de treino" }, color = Muted)
                    }
                }
                Spacer(Modifier.height(15.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    LabelPill("$count ${if (count == 1) "exercício" else "exercícios"}")
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Line)
                Spacer(Modifier.height(11.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onOpen(plan.id) }) { Text("Ver ficha") }
                    TextButton(onClick = { edit = plan }) { Text("Editar") }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = { onStart(plan) }, enabled = count > 0, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 14.dp)) { Text("Iniciar  ›") }
                }
                TextButton(onClick = { model.deletePlan(plan) }, modifier = Modifier.align(Alignment.End), colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Excluir ficha") }
            }
        }
    }
    if (creating || edit != null) PlanDialog(edit, onDismiss = { creating = false; edit = null }, onSave = { id, name, split -> model.savePlan(id, name, split); creating = false; edit = null })
}

@Composable private fun PlanDialog(plan: WorkoutPlan?, onDismiss: () -> Unit, onSave: (Long, String, String) -> Unit) {
    var name by remember(plan?.id) { mutableStateOf(plan?.name.orEmpty()) }
    var split by remember(plan?.id) { mutableStateOf(plan?.split.orEmpty()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (plan == null) "Nova ficha" else "Editar ficha") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(name, { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nome da ficha") }, shape = RoundedCornerShape(14.dp))
            OutlinedTextField(split, { split = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Divisão (ex: Push)") }, shape = RoundedCornerShape(14.dp))
        }
    }, confirmButton = { TextButton(onClick = { onSave(plan?.id ?: 0, name, split) }, enabled = name.isNotBlank()) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable private fun PlanDetailPage(state: FitUiState, planId: Long, model: FitViewModel, onBack: () -> Unit, onStart: (WorkoutPlan) -> Unit) {
    val plan = state.plans.firstOrNull { it.id == planId }
    val links = state.planExercises.filter { it.planId == planId }.sortedBy { it.position }
    var edit by remember { mutableStateOf<PlanExercise?>(null) }
    var adding by remember { mutableStateOf(false) }
    if (plan == null) { EmptyState("?", "Ficha não encontrada", "Volte à lista de fichas e escolha outro plano."); return }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) { Text("‹  Todas as fichas") }
            PageHeading("PLANO DE TREINO", plan.name, plan.split.ifBlank { "Seu treino, do seu jeito." })
            FitCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("PRONTO PARA TREINAR?", color = Pine, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(Modifier.height(5.dp))
                        Text("${links.size} exercícios na sequência", style = MaterialTheme.typography.titleMedium)
                    }
                    Button(onClick = { onStart(plan) }, enabled = links.isNotEmpty(), shape = RoundedCornerShape(13.dp)) { Text("Iniciar  ›") }
                }
            }
            SectionHeading("Exercícios", "${links.size} no plano")
            OutlinedButton(onClick = { adding = true }, enabled = state.exercises.isNotEmpty(), shape = RoundedCornerShape(13.dp)) { Text("+  Adicionar exercício") }
            if (state.exercises.isEmpty()) Text("Cadastre exercícios na Biblioteca para adicioná-los.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        if (links.isEmpty()) item { EmptyState("＋", "Ficha pronta para crescer", "Adicione exercícios e configure suas metas de séries, repetições e carga.") }
        items(links, key = { it.id }) { link ->
            val exercise = state.exercises.firstOrNull { it.id == link.exerciseId }
            FitCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).background(CanvasColor, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text("${link.position + 1}", fontWeight = FontWeight.Bold, color = Pine) }
                    Spacer(Modifier.width(11.dp))
                    Text(exercise?.name ?: "Exercício removido", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(12.dp))
                LabelPill("${link.targetSets} séries  ·  ${link.targetReps} reps  ·  ${link.targetLoad} kg", strong = true)
                Spacer(Modifier.height(8.dp))
                Row {
                    TextButton(onClick = { edit = link }) { Text("Editar metas") }
                    TextButton(onClick = { model.deleteLink(link) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Remover") }
                }
            }
        }
    }
    if (adding || edit != null) LinkDialog(state.exercises, edit, links.size, onDismiss = { adding = false; edit = null }, onSave = { id, exerciseId, position, sets, reps, load ->
        model.saveLink(id, planId, exerciseId, position, sets, reps, load); adding = false; edit = null
    })
}

@Composable private fun LinkDialog(exercises: List<Exercise>, link: PlanExercise?, nextPosition: Int, onDismiss: () -> Unit, onSave: (Long, Long, Int, Int, Int, Double) -> Unit) {
    var selected by remember(link?.id) { mutableLongStateOf(link?.exerciseId ?: exercises.firstOrNull()?.id ?: 0L) }
    var menu by remember { mutableStateOf(false) }
    var sets by remember(link?.id) { mutableStateOf((link?.targetSets ?: 3).toString()) }
    var reps by remember(link?.id) { mutableStateOf((link?.targetReps ?: 10).toString()) }
    var load by remember(link?.id) { mutableStateOf((link?.targetLoad ?: 0.0).toString()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (link == null) "Adicionar exercício" else "Editar prescrição") }, text = { Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box { OutlinedButton(onClick = { menu = true }) { Text(exercises.firstOrNull { it.id == selected }?.name ?: "Escolher exercício") }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) { exercises.forEach { exercise -> DropdownMenuItem(text = { Text(exercise.name) }, onClick = { selected = exercise.id; menu = false }) } } }
        NumberField("Séries", sets, { sets = it }); NumberField("Repetições", reps, { reps = it }); NumberField("Carga planejada (kg)", load, { load = it })
    } }, confirmButton = { TextButton(onClick = { onSave(link?.id ?: 0, selected, link?.position ?: nextPosition, sets.toIntOrNull() ?: 0, reps.toIntOrNull() ?: 0, load.replace(',', '.').toDoubleOrNull() ?: -1.0) }, enabled = selected != 0L) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable private fun LibraryPage(state: FitUiState, model: FitViewModel) {
    var edit by remember { mutableStateOf<Exercise?>(null) }
    var creating by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PageHeading("SEU REPERTÓRIO", "Exercícios", "Tudo o que você precisa para montar suas fichas.")
            Button(onClick = { creating = true }, shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp)) { Text("+  Novo exercício") }
            SectionHeading("Biblioteca", "${state.exercises.size} itens")
        }
        if (state.exercises.isEmpty()) item { EmptyState("◇", "Sua biblioteca começa aqui", "Cadastre os movimentos que fazem parte dos seus treinos.") }
        items(state.exercises, key = { it.id }) { exercise ->
            FitCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) { Text("✦", color = Pine, style = MaterialTheme.typography.titleLarge) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(3.dp))
                        Text(exercise.muscleGroup.ifBlank { "Grupo não informado" }, color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (exercise.instructions.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(exercise.instructions, color = Muted, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                }
                Spacer(Modifier.height(11.dp))
                HorizontalDivider(color = Line)
                Row {
                    if (!exercise.demoUrl.isNullOrBlank()) TextButton(onClick = { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(exercise.demoUrl))) } }) { Text("Ver vídeo") }
                    TextButton(onClick = { edit = exercise }) { Text("Editar") }
                    TextButton(onClick = { model.deleteExercise(exercise) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Excluir") }
                }
            }
        }
    }
    if (creating || edit != null) ExerciseDialog(edit, onDismiss = { creating = false; edit = null }, onSave = { id, name, group, instructions, url ->
        model.saveExercise(id, name, group, instructions, url); creating = false; edit = null
    })
}

@Composable private fun ExerciseDialog(exercise: Exercise?, onDismiss: () -> Unit, onSave: (Long, String, String, String, String) -> Unit) {
    var name by remember(exercise?.id) { mutableStateOf(exercise?.name.orEmpty()) }
    var group by remember(exercise?.id) { mutableStateOf(exercise?.muscleGroup.orEmpty()) }
    var instructions by remember(exercise?.id) { mutableStateOf(exercise?.instructions.orEmpty()) }
    var url by remember(exercise?.id) { mutableStateOf(exercise?.demoUrl.orEmpty()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (exercise == null) "Novo exercício" else "Editar exercício") }, text = { Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(name, { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nome") }, shape = RoundedCornerShape(14.dp))
        OutlinedTextField(group, { group = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Grupo muscular") }, shape = RoundedCornerShape(14.dp))
        OutlinedTextField(instructions, { instructions = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Execução") }, minLines = 2, shape = RoundedCornerShape(14.dp))
        OutlinedTextField(url, { url = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Link de vídeo/GIF (opcional)") }, shape = RoundedCornerShape(14.dp))
    } }, confirmButton = { TextButton(onClick = { onSave(exercise?.id ?: 0, name, group, instructions, url) }, enabled = name.isNotBlank()) { Text("Salvar") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}
