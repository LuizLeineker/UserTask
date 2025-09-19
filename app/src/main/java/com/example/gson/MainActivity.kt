package com.example.gson

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gson.ui.theme.GsonTheme
import retrofit2.await
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Navigation()
        }
    }
}

// NAV
@Composable
fun Navigation(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "lista_usuarios"  // rota inicial
    ){
        // ROTAS
        // "url" para cada tela
        composable("lista_usuarios"){
            Principal(navController)
        }

        composable("detalhes/{id}/{username}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            val username = backStackEntry.arguments?.getString("username")
            Detalhes(id, username, navController)
        }

        composable("tarefas/{userId}/{username}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            val username = backStackEntry.arguments?.getString("username")
            Tarefas(userId, username)
        }


    }
}


// MAIN
@Composable
fun Principal(navController: NavController){
    // LISTAS DO USER
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    // TRATAMENTO DE ERRO
    var ex by remember { mutableStateOf<String?>(null) }

    // acesso a API
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.getUsers().await()
            users = response
        } catch (e: Exception) {
            ex = "Infelizmente não foi possivel acessar a pagina!"
        }
    }

    // CONTEUDO DA PAGINA
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        Spacer(Modifier.height(10.dp))

        Text(
            text = "Usuários & Tasks!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF6824CB),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Dados Cadastrados: ", fontSize = 25.sp)

        // VERIFICICAR ERRO
        if(ex != null){
            Text(text = ex!!)
        }else{
            LazyColumn {
                items(users) { user ->
                    Text(text = "${user.name}..",
                         modifier = Modifier.fillMaxWidth().padding(8.dp).
                         clickable{
                             navController.navigate("detalhes/${user.id}/${user.name}")
                         }
                    )
                }
            }
        }

        // cont user
        Text(text = "Total de Cadastros: ${users.size}", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

    }

}


//DADOS USUARIOS
@Composable
fun Detalhes(id: Int?, username: String?, navController: NavController){
    var user by remember { mutableStateOf<User?>(null) }

    // Tratamento de Excecao
    var ex by remember { mutableStateOf<String?>(null) }

    // acesso a API
    LaunchedEffect(id) {
        try {
            if (id != null) {
                val response = RetrofitInstance.api.getUserById(id).await()
                user = response
            }
        } catch (e: Exception) {
            ex = "Infelizmente não foi possivel carregar usuário!"
        }
    }

    //CONTEUDO
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp))
    {
        Spacer(Modifier.height(16.dp))
        // DADOS RELACIONADOS AO USER
        when {
            ex != null -> Text(ex!!)
            user != null -> {
                Text("ID: ${user!!.id}")
                Text("Nome: ${user!!.name}")
                Text("Email: ${user!!.email}")
                Text("Username: ${user!!.username}")
            }
            else -> Text("Carregando...")
        }

        Spacer(Modifier.height(16.dp))

        // BOTÃO PARA IR AS TASKS
        Button(
            onClick = {
                if (id != null && username != null) {
                    navController.navigate("tarefas/$id/$username")
                }
            }
        ) {
            Text("Abrir Tarefas do Usuário")
        }
    }
}

// DADOS TASKS
@Composable
fun Tarefas(userId: Int?, username: String?){
    // LISTA
    var tarefas by remember { mutableStateOf<List<Tasks>>(emptyList()) }
    // EXCEPTION
    var ex by remember { mutableStateOf<String?>(null) }
    // CONT
    var contador by remember { mutableStateOf(0) }

    val context = LocalContext.current

    // acesso a API
    LaunchedEffect(userId) {
        try {
            if (userId != null) {
                val response = RetrofitInstance.api.getTasks(userId).await()
                tarefas = response
            }
        } catch (e: Exception) {
            ex = "Infelizmente não foi possivel carregar as tasks!"
        }
    }

    LaunchedEffect(tarefas) {
        contador = tarefas.count { it.completed }
    }

    // Conteudo
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Tarefas de ${username}:",
            fontSize = 20.sp,
            color = Color(0xFF6824CB)
        )

        Spacer(Modifier.height(8.dp))

        if (ex != null) {
            Text(text = ex!!)
        } else {
        // Tarefas de acordo com o user
            when {
                else -> {
                    LazyColumn {
                        items(tarefas) { tarefa ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                .pointerInput(tarefas) {
                                    detectTapGestures(
                                        onLongPress = {
                                            tarefas = tarefas.toMutableList().also { it.remove(tarefa) }
                                            Toast.makeText(context, "Tarefa removida!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                            horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // TITULO DA TAREFA
                                Text(text = tarefa.title)
                                Text(text = if (tarefa.completed) "✅" else "❌")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Tasks: ${tarefas.size}", fontSize = 30.sp)
            Text(text = "Completas: ${contador}!", fontSize = 25.sp, color = Color(0xFF420CAD))
            Spacer(modifier = Modifier.height(16.dp))


        }

    }
}

