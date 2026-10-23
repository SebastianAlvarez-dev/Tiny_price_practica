package com.example.practica_inetgracion

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.practica_inetgracion.data.model.ProductOffer
import com.example.practica_inetgracion.data.auth.SupabaseAuthRepository
import com.example.practica_inetgracion.data.repository.DealsRepositoryProvider
import com.example.practica_inetgracion.presentation.deals.AuthUiState
import com.example.practica_inetgracion.presentation.deals.AuthViewModel
import com.example.practica_inetgracion.presentation.deals.DealsEvent
import com.example.practica_inetgracion.presentation.deals.DealsUiState
import com.example.practica_inetgracion.presentation.deals.DealsViewModel
import com.example.practica_inetgracion.ui.theme.Practica_inetgracionTheme

class MainActivity : ComponentActivity() {
    private val authRepository by lazy { SupabaseAuthRepository() }
    private val authViewModel: AuthViewModel by viewModels { AuthViewModel.factory(authRepository) }
    private val viewModel: DealsViewModel by viewModels {
        DealsViewModel.factory(DealsRepositoryProvider.create { authRepository.validAccessToken() })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val authState by authViewModel.state.collectAsStateWithLifecycle()
            Practica_inetgracionTheme {
                if (BuildConfig.USE_REMOTE_DEALS && !authState.isAuthenticated) {
                    AuthScreen(authState, authViewModel::setEmail, authViewModel::setPassword, authViewModel::toggleMode, authViewModel::submit)
                } else {
                    DealsApp(state = state, onEvent = viewModel::onEvent, onSignOut = if (BuildConfig.USE_REMOTE_DEALS) authViewModel::signOut else null)
                }
            }
        }
    }
}

@Composable
fun DealsApp(state: DealsUiState, onEvent: (DealsEvent) -> Unit, onSignOut: (() -> Unit)? = null) {
    BackHandler(enabled = state.selectedOffer != null) { onEvent(DealsEvent.Back) }
    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
            if (state.selectedOffer != null) {
                OfferDetails(state.selectedOffer, onBack = { onEvent(DealsEvent.Back) })
            } else {
                DealsHome(state, onEvent, onSignOut)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DealsHome(state: DealsUiState, onEvent: (DealsEvent) -> Unit, onSignOut: (() -> Unit)?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("◒", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("TinyPrice", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Búsqueda web inteligente", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.weight(1f))
                onSignOut?.let { TextButton(onClick = it) { Text("Salir") } }
            }
            Spacer(Modifier.height(18.dp))
            Text("Encuentra la mejor compra.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Compara productos y descubre buenas ofertas.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(DealsEvent.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("¿Qué producto buscas?") },
                placeholder = { Text("Ej. Nintendo Switch OLED") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onEvent(DealsEvent.SubmitSearch) }),
            )
            Button(
                onClick = { onEvent(DealsEvent.SubmitSearch) },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Buscar ofertas") }
            if (!state.hasSearched) {
                Text("PRUEBA UNA BÚSQUEDA", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 22.dp, bottom = 8.dp))
                Suggestion("Nintendo Switch OLED") { onEvent(DealsEvent.UseSuggestion("Nintendo Switch OLED")) }
                Suggestion("Auriculares inalámbricos") { onEvent(DealsEvent.UseSuggestion("Auriculares inalámbricos")) }
            }
            if (state.isPreviewData) {
                Text("DEMO · OFERTAS DE EJEMPLO", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 14.dp))
            }
            if (state.isLoading) {
                Row(Modifier.fillMaxWidth().padding(28.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Buscando ofertas…")
                }
            }
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp)) }
            if (state.hasSearched && !state.isLoading && state.errorMessage == null) {
                Text(
                    if (state.isPreviewData) "${state.offers.size} ofertas de demostración"
                    else "${state.offers.size} resultados web encontrados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 18.dp),
                )
                if (state.offers.isEmpty()) Text("No encontramos resultados. Prueba otra búsqueda.", modifier = Modifier.padding(top = 10.dp))
            }
        }
        items(state.offers, key = ProductOffer::id) { offer ->
            OfferCard(offer, onClick = { onEvent(DealsEvent.OpenOffer(offer)) })
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun AuthScreen(
    state: AuthUiState,
    onEmail: (String) -> Unit,
    onPassword: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("TinyPrice", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(if (state.isRegistering) "Crea tu cuenta para buscar ofertas" else "Inicia sesión para buscar ofertas", modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        OutlinedTextField(state.email, onEmail, label = { Text("Correo electrónico") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.password, onPassword, label = { Text("Contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().padding(top = 10.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onSubmit() }))
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp)) }
        Button(onClick = onSubmit, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(if (state.isRegistering) "Crear cuenta" else "Iniciar sesión")
        }
        TextButton(onClick = onToggleMode) {
            Text(if (state.isRegistering) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate")
        }
    }
}

@Composable
private fun Suggestion(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text("⌕  $label") }
}

@Composable
private fun OfferCard(offer: ProductOffer, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(offer.store, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("★ ${offer.rating}", style = MaterialTheme.typography.labelLarge)
            }
            Text(offer.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                Text(offer.price, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                offer.originalPrice?.let { Text("  $it", style = MaterialTheme.typography.bodyMedium) }
            }
            Text("${offer.shipping} · ${offer.delivery}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            Text(offer.summary, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 10.dp))
            Text(offer.tags.joinToString("  ·  "), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 10.dp))
            Text("Ver detalles →", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun OfferDetails(offer: ProductOffer, onBack: () -> Unit) {
    val context = LocalContext.current
    val sourceUri = Uri.parse(offer.sourceUrl)
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("← Volver a ofertas") }
        Text(offer.store, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(offer.productName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(offer.price, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("★ ${offer.rating}  ·  ${offer.shipping}  ·  ${offer.delivery}")
        Text(offer.summary, style = MaterialTheme.typography.bodyLarge)
        Text("Fuente: ${offer.sourceUrl}", style = MaterialTheme.typography.bodySmall)
        if (sourceUri.scheme in listOf("http", "https") && !sourceUri.host.isNullOrBlank()) {
            Button(onClick = {
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, sourceUri)) }
            }) { Text("Abrir fuente web") }
        }
        Spacer(Modifier.height(4.dp))
        Text("Verifica el precio y la disponibilidad en la tienda antes de comprar.", style = MaterialTheme.typography.labelMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun DealsPreview() {
    Practica_inetgracionTheme { DealsApp(DealsUiState(), onEvent = {}) }
}
