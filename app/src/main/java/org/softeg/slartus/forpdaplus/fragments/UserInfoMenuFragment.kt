package org.softeg.slartus.forpdaplus.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.softeg.slartus.forpdaplus.*
import org.softeg.slartus.forpdaplus.common.AppLog
import org.softeg.slartus.forpdaplus.core.entities.UserInfo
import org.softeg.slartus.forpdaplus.core.repositories.QmsCountRepository
import org.softeg.slartus.forpdaplus.core.repositories.UserInfoRepository
import org.softeg.slartus.forpdaplus.fragments.profile.ProfileFragment
import org.softeg.slartus.forpdaplus.listfragments.mentions.MentionsListFragment.Companion.newFragment
import org.softeg.slartus.forpdaplus.listfragments.next.UserReputationFragment.Companion.showActivity
import org.softeg.slartus.forpdaplus.listtemplates.QmsContactsBrickInfo
import javax.inject.Inject

@AndroidEntryPoint
class UserInfoMenuFragment : Fragment() {
    private val viewModel: UserInfoMenuViewModel by viewModels()
    private var userInfo: UserInfo? = null

    private var guestMenuItem: MenuItem? = null
    private var userMenuItem: MenuItem? = null
    private var qmsMenuItem: MenuItem? = null
    private var mentionsMenuItem: MenuItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            UserInfoMenuViewModel.ViewState.Initialize -> {

                            }
                            is UserInfoMenuViewModel.ViewState.Success -> {
                                userInfo = uiState.userInfo
                                refreshUserMenu()
                            }
                            is UserInfoMenuViewModel.ViewState.Error -> {
                                AppLog.e(activity, uiState.exception)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.user, menu)
        guestMenuItem = menu.findItem(R.id.guest_item)
        userMenuItem = menu.findItem(R.id.user_item)
        qmsMenuItem = menu.findItem(R.id.qms_item)
        mentionsMenuItem = menu.findItem(R.id.mentions_item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        refreshUserMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.qms_item -> {
                val brickInfo = QmsContactsBrickInfo()
                MainActivity.addTab(brickInfo.title, brickInfo.name, brickInfo.createFragment())
                return true
            }
            R.id.mentions_item -> {
                MainActivity.addTab(
                    "Упоминания", "https://" + App.Host + "/forum/index.php?act=mentions",
                    newFragment()
                )
                return true
            }
            R.id.profile_item -> {
                userInfo?.let {
                    ProfileFragment.showProfile(it.id, Client.getInstance().user)
                }

                return true
            }
            R.id.reputation_item -> {
                userInfo?.let {
                    showActivity(it.id, false)
                }
                return true
            }
            R.id.logout_item -> {
                LoginDialog.logout(requireActivity())
                return true
            }
            R.id.login_item -> {
                LoginDialog.showDialog(requireActivity())
                return true
            }
            R.id.registration_item -> {
                val marketIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://" + App.Host + "/forum/index.php?act=Reg&CODE=00")
                )
                startActivity(marketIntent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun refreshUserMenu() {
        userInfo.let { userInfo ->
            val logged = userInfo?.logined == true
            guestMenuItem?.isVisible = !logged
            userMenuItem?.isVisible = logged
            if (userInfo?.logined == true) {
                val qmsCount = userInfo.qmsCount ?: 0
                userMenuItem?.title = userInfo.name
                userMenuItem?.setIcon(getUserIconRes(userInfo))
                val qmsTitle =
                    if (qmsCount > 0) "QMS ($qmsCount)" else "QMS"
                qmsMenuItem?.title = qmsTitle
                val mentionsCount = userInfo.mentionsCount ?: 0
                mentionsMenuItem?.title =
                    "Упоминания " + if (mentionsCount > 0) "($mentionsCount)" else ""
            }
        }
    }

    private fun getUserIconRes(userInfo: UserInfo): Int {
        val logged = Client.getInstance().logined
        return if (logged) {
            if (userInfo.qmsCount ?: 0 > 0 || userInfo.mentionsCount ?: 0 > 0)
                R.drawable.message_text
            else
                R.drawable.account
        } else {
            R.drawable.account_outline
        }
    }

    override fun onDestroy() {
        guestMenuItem = null
        userMenuItem = null
        qmsMenuItem = null
        mentionsMenuItem = null
        super.onDestroy()
    }

    companion object {
        const val TAG = "UserInfoMenuFragment"
    }
}

@HiltViewModel
class UserInfoMenuViewModel @Inject constructor(
    private val userInfoRepository: UserInfoRepository,
    private val qmsCountRepository: QmsCountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ViewState>(ViewState.Initialize)
    val uiState: StateFlow<ViewState> = _uiState

    private val errorHandler = CoroutineExceptionHandler { _, ex ->
        _uiState.value = ViewState.Error(ex)
    }

    init {
        viewModelScope.launch {
            launch(SupervisorJob() + errorHandler) {
                userInfoRepository.userInfo
                    .distinctUntilChanged()
                    .collect {
                        _uiState.value = ViewState.Success(it)
                    }
            }
            launch(SupervisorJob() + errorHandler) {
                qmsCountRepository.count
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collect {
                        userInfoRepository.setQmsCount(it)
                    }
            }
        }
    }

    sealed class ViewState {
        object Initialize : ViewState()

        data class Success(val userInfo: UserInfo) : ViewState()
        data class Error(val exception: Throwable) : ViewState()
    }
}
