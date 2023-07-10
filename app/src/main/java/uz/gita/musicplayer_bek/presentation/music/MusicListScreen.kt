package uz.gita.musicplayer_bek.presentation.music

import android.Manifest
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.hilt.getViewModel
import org.orbitmvi.orbit.compose.*
import uz.gita.musicplayer_bek.MainActivity
import uz.gita.musicplayer_bek.R
import uz.gita.musicplayer_bek.data.model.CommandEnum
import uz.gita.musicplayer_bek.navigation.AppScreen
import uz.gita.musicplayer_bek.ui.component.CurrentMusicItemComponent
import uz.gita.musicplayer_bek.ui.component.LoadingComponent
import uz.gita.musicplayer_bek.ui.component.MusicItemComponent
import uz.gita.musicplayer_bek.ui.theme.Light_Red
import uz.gita.musicplayer_bek.ui.theme.MusicPlayerTheme
import uz.gita.musicplayer_bek.utils.MyEventBus
import uz.gita.musicplayer_bek.utils.base.checkPermissions
import uz.gita.musicplayer_bek.utils.base.getMusicDataByPosition
import uz.gita.musicplayer_bek.utils.base.startMusicService

class MusicListScreen : AppScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val activity = LocalContext.current as MainActivity
        val viewModel: MusicListContract.ViewModel = getViewModel<MusicListViewModel>()
        val uiState = viewModel.collectAsState()

        viewModel.collectSideEffect { sideEffect ->
            when (sideEffect) {
                MusicListContract.SideEffect.StartMusicService -> {
                    startMusicService(activity, CommandEnum.PLAY)
                }

                MusicListContract.SideEffect.OpenPermissionDialog -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        activity.checkPermissions(
                            arrayListOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.POST_NOTIFICATIONS,
                                Manifest.permission.READ_MEDIA_AUDIO,
                                Manifest.permission.READ_PHONE_STATE
                            )
                        ) {
                            viewModel.onEventDispatcher(MusicListContract.Intent.LoadMusics(activity))
                        }
                    } else {
                        activity.checkPermissions(
                            arrayListOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_PHONE_STATE
                            )
                        ) {
                            viewModel.onEventDispatcher(MusicListContract.Intent.LoadMusics(activity))
                        }
                    }
                }
            }
        }

        LifecycleEffect(
            onStarted = { viewModel.onEventDispatcher(MusicListContract.Intent.LoadMusics(activity)) }
        )

        MusicPlayerTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    topBar = { TopBar(viewModel::onEventDispatcher) }
                ) {
                    MusicListContent(
                        uiState = uiState,
                        eventListener = viewModel::onEventDispatcher,
                        modifier = Modifier.padding(it)
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicListContent(
    uiState: State<MusicListContract.UIState>,
    eventListener: (MusicListContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current as MainActivity

    Surface(modifier = modifier.fillMaxSize()) {
        Box {
            when (uiState.value) {
                MusicListContract.UIState.Loading -> {
                    LoadingComponent()
                    eventListener.invoke(MusicListContract.Intent.RequestPermission)
                }

                MusicListContract.UIState.PreparedData -> {
                    LazyColumn {
                        for (pos in 0 until MyEventBus.cursor!!.count) {
                            item {
                                MusicItemComponent(
                                    musicData = MyEventBus.cursor!!.getMusicDataByPosition(pos),
                                    onClick = {
                                        MyEventBus.selectMusicPos = pos
                                        eventListener.invoke(MusicListContract.Intent.PlayMusic)
                                        eventListener.invoke(MusicListContract.Intent.OpenPlayScreen)
                                    },
                                    onLongClick = {}
                                )
                            }
                        }
                    }

                    if (MyEventBus.selectMusicPos != -1 ||
                        MyEventBus.selectMusicPos >= MyEventBus.cursor!!.count
                    ) {
                        val data = MyEventBus.currentMusicData.collectAsState()
                        CurrentMusicItemComponent(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            musicData = data.value!!,
                            onClick = { eventListener.invoke(MusicListContract.Intent.OpenPlayScreen) },
                            onClickManage = { startMusicService(context, CommandEnum.MANAGE) })
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(eventListener: (MusicListContract.Intent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Light_Red)
            .height(56.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .width(0.dp)
                .padding(start = 16.dp)
                .weight(1f),
            fontSize = 22.sp,
            color = Color.Black,
            text = "Music List"
        )

        Image(
            modifier = Modifier
                .padding(end = 16.dp)
                .clickable { eventListener.invoke(MusicListContract.Intent.OpenPlayListScreen) },
            painter = painterResource(id = R.drawable.ic_fav),
            contentDescription = null
        )
    }
}