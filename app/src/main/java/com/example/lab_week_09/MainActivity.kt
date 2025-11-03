package com.example.lab_week_09

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lab_week_09.ui.theme.LAB_WEEK_09Theme
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.util.copy
import com.example.lab_week_09.ui.theme.OnBackgroundItemText
import com.example.lab_week_09.ui.theme.OnBackgroundTitleText
import com.example.lab_week_09.ui.theme.PrimaryTextButton
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LAB_WEEK_09Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    App(
                        navController = navController
                    )
                }
            }
        }
    }
}

data class Student(
    var name: String
)


//@Preview(showBackground = true)

@Composable
fun Home(
    navigateFromHomeToResult: (String) -> Unit
) {
    val listData = remember { mutableStateListOf(
        Student("Tanu"),
        Student("Tina"),
        Student("Tono")
    )}

    var inputField = remember { mutableStateOf(Student("")) }

    // DIUBAH: Siapkan Moshi dan Adapter
    // Kita gunakan 'remember' agar objek ini tidak dibuat ulang
    // di setiap recomposition
    val moshi = remember { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
    val listType = remember { Types.newParameterizedType(List::class.java, Student::class.java) }
    val adapter: JsonAdapter<List<Student>> = remember { moshi.adapter(listType) }


    HomeContent(
        listData,
        inputField.value, // Kirim .value-nya
        { input -> inputField.value = inputField.value.copy(name = input) },
        {
            if (inputField.value.name.isNotBlank()) {
                listData.add(inputField.value.copy())
                inputField.value = Student("")
            }
        },
        // DIUBAH: Konversi list ke JSON saat navigasi
        {
            // 1. Ambil snapshot list saat ini
            val currentList = listData.toList()

            // 2. Konversi ke string JSON
            val jsonString = adapter.toJson(currentList)

            // 3. Kirim JSON string ke fungsi navigasi
            navigateFromHomeToResult(jsonString)
        }
    )
}



@Composable
fun HomeContent(
    listData: SnapshotStateList<Student>,
    inputField: Student,
    onInputValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
    navigateFromHomeToResult: () -> Unit
) {
    LazyColumn {
        item {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnBackgroundTitleText(text = stringResource(
                    id = R.string.enter_item)
                )

                Text(text = stringResource(
                    id = R.string.enter_item)
                )
                TextField(
                    value = inputField.name,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),

                    onValueChange = {
                        onInputValueChange(it)
                    }
                )

                PrimaryTextButton(text = stringResource(
                    id = R.string.button_click)
                ) {
                    onButtonClick()
                }
                PrimaryTextButton(text = stringResource(id =
                    R.string.button_navigate)) {
                    navigateFromHomeToResult()
                }
            }
        }

        items(listData) { item ->
            Column(
                modifier = Modifier.padding(vertical = 4.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnBackgroundItemText(text = item.name)
            }
        }
    }
}

@Composable
fun App(navController: NavHostController) {
    //Here, we use NavHost to create a navigation graph
    //We pass the navController as a parameter
    //We also set the startDestination to "home"
    //This means that the app will start with the Home composable
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            Home { navController.navigate(
                "resultContent/?listData=$it")
            }
        }
        composable(
            "resultContent/?listData={listData}",
            arguments = listOf(navArgument("listData") {
                type = NavType.StringType }
            )
        ) {
            ResultContent(
                it.arguments?.getString("listData").orEmpty()
            )
        }
    }
}

@Composable
fun ResultContent(listData: String) { // 'listData' sekarang adalah String JSON

    // DIUBAH: Siapkan Moshi dan Adapter (sama seperti di Home)
    val moshi = remember { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
    val listType = remember { Types.newParameterizedType(List::class.java, Student::class.java) }
    val adapter: JsonAdapter<List<Student>> = remember { moshi.adapter(listType) }

    // DIUBAH: Parse JSON kembali menjadi List<Student>
    // Kita 'remember' hasil parse, dan hanya parse ulang jika string JSON-nya berubah
    val studentList = remember(listData) {
        try {
            // Coba parse JSON. Ini bisa gagal jika string-nya rusak
            adapter.fromJson(listData)
        } catch (e: Exception) {
            // Jika gagal, kembalikan list kosong
            null
        }
    }

    // DIUBAH: Tampilkan hasilnya menggunakan LazyColumn
    if (studentList != null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                OnBackgroundTitleText(text = "Result List (from JSON)")
            }
            items(studentList) { student ->
                // Tampilkan setiap item list
                OnBackgroundItemText(text = student.name)
            }
        }
    } else {
        // Tampilkan pesan error jika JSON tidak bisa di-parse
        Column(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnBackgroundItemText(text = "Error: Could not parse list data.")
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewHome() {
//    Home(listOf("Tanu", "Tina", "Tono"))
//}


//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    LAB_WEEK_09Theme {
//        Greeting("Android")
//    }
//}