package br.com.frazo.jana_c

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.notesapp.R
import br.com.frazo.jana_c.ui.theme.NotesAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesAppTheme {
                // A surface container using the 'background' color from the theme
                Content()
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Content() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        var count by remember {
            mutableStateOf(0)
        }

        val listState = rememberLazyListState()
        val expandedFabState = remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0
            }
        }

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            val (contentRef, buttonsRef, countRef) = createRefs()
            AnimatedContent(targetState = count, Modifier.constrainAs(countRef){
                start.linkTo(parent.start)
                top.linkTo(parent.top)
                end.linkTo(parent.end)
                bottom.linkTo(contentRef.top)
            }.padding(vertical = 10.dp)) {
                Greeting(name = "hello $it")
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(contentRef) {
                        start.linkTo(parent.start)
                        top.linkTo(countRef.bottom)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }.padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (index in 0 until 100) {
                    item {
                        NoteCard(
                            title = "This is a title ${index + count}",
                            text = "This is a note to your future self"
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .wrapContentSize()
                    .constrainAs(buttonsRef) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
            ) {
                ExtendedFloatingActionButton(
                    text = { Text(text = "Add") },
                    icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = "") },
                    onClick = { count++ },
                    expanded = expandedFabState.value
                )
            }
        }
    }
}

@Composable
fun NoteCard(title: String, text: String) {
    Card(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {

            val (titleRowRef, textRef) = createRefs()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(titleRowRef) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Justify,
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    painter = painterResource(id = R.drawable.baseline_lightbulb_24),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

            Text(
                modifier = Modifier
                    .constrainAs(textRef) {
                        top.linkTo(titleRowRef.bottom)
                        start.linkTo(parent.start)
                    }
                    .padding(top = 8.dp),
                textAlign = TextAlign.Justify,
                text = text,
                style = MaterialTheme.typography.bodySmall
            )

        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NotesAppTheme(darkTheme = true) {
        Content()
    }
}


