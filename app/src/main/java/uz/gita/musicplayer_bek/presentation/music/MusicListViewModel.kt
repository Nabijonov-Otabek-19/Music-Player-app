package uz.gita.musicplayer_bek.presentation.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import uz.gita.musicplayer_bek.utils.MyEventBus
import uz.gita.musicplayer_bek.utils.base.getMusicsCursor
import javax.inject.Inject

@HiltViewModel
class MusicListViewModel @Inject constructor(
    private val direction: MusicListDirectionImpl
) : ViewModel(), MusicListContract.ViewModel {

    override val container =
        container<MusicListContract.UIState, MusicListContract.SideEffect>(MusicListContract.UIState.Loading)

    init {
        intent {
            reduce {
                if (MyEventBus.cursor != null) MusicListContract.UIState.PreparedData
                else MusicListContract.UIState.Loading
            }
        }
    }

    override fun onEventDispatcher(intent: MusicListContract.Intent) {
        when (intent) {
            is MusicListContract.Intent.LoadMusics -> {
                intent.context.getMusicsCursor()
                    .onEach {
                        MyEventBus.cursor = it
                        intent { reduce { MusicListContract.UIState.PreparedData } }
                    }
                    .launchIn(viewModelScope)
            }

            /*MusicListContract.Intent.OpenAddPlayListScreen -> {
                viewModelScope.launch { direction.navigateToAddPlayListScreen() }
            }*/

            MusicListContract.Intent.OpenPlayListScreen ->{
                viewModelScope.launch { direction.navigateToPlayListScreen() }
            }

            MusicListContract.Intent.PlayMusic -> {
                intent { postSideEffect(MusicListContract.SideEffect.StartMusicService) }
            }

            MusicListContract.Intent.OpenPlayScreen -> {
                viewModelScope.launch { direction.navigateToPlayScreen() }
            }

            MusicListContract.Intent.RequestPermission -> {
                intent { postSideEffect(MusicListContract.SideEffect.OpenPermissionDialog) }
            }
        }
    }
}