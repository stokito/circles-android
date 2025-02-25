package org.futo.circles.feature.people.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.R
import org.futo.circles.core.base.NetworkObserver
import org.futo.circles.core.extensions.loadProfileIcon
import org.futo.circles.core.extensions.notEmptyDisplayName
import org.futo.circles.core.extensions.observeData
import org.futo.circles.core.extensions.observeResponse
import org.futo.circles.core.extensions.onBackPressed
import org.futo.circles.core.extensions.setEnabledChildren
import org.futo.circles.core.extensions.setIsVisible
import org.futo.circles.core.extensions.showNoInternetConnection
import org.futo.circles.core.extensions.showSuccess
import org.futo.circles.core.extensions.withConfirmation
import org.futo.circles.core.base.fragment.BaseFullscreenDialogFragment
import org.futo.circles.databinding.DialogFragmentUserBinding
import org.futo.circles.extensions.*
import org.futo.circles.feature.people.user.list.UsersCirclesAdapter
import org.futo.circles.model.IgnoreUser
import org.futo.circles.model.UnfollowTimeline
import org.futo.circles.model.UnfollowUser
import org.matrix.android.sdk.api.session.user.model.User

@AndroidEntryPoint
class UserDialogFragment : BaseFullscreenDialogFragment(DialogFragmentUserBinding::inflate) {

    private val viewModel by viewModels<UserViewModel>()
    private val binding by lazy {
        getBinding() as DialogFragmentUserBinding
    }

    private val usersCirclesAdapter by lazy {
        UsersCirclesAdapter(
            onRequestFollow = { timelineId ->
                if (showNoInternetConnection()) return@UsersCirclesAdapter
                viewModel.requestFollowTimeline(timelineId)
            },
            onUnFollow = { timelineId ->
                if (showNoInternetConnection()) return@UsersCirclesAdapter
                withConfirmation(UnfollowTimeline()) {
                    viewModel.unFollowTimeline(timelineId)
                }
            }
        )
    }

    private var isUserIgnored = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        setupMenu()
        binding.rvCircles.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = usersCirclesAdapter
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setupMenu() {
        with(binding.toolbar) {
            (menu as? MenuBuilder)?.setOptionalIconsVisible(true)
            menu.findItem(R.id.unFollow).isVisible = viewModel.amIFollowingUser()
            menu.findItem(R.id.ignore).isVisible = !isUserIgnored
            menu.findItem(R.id.unIgnore).isVisible = isUserIgnored
            setOnMenuItemClickListener { item ->
                return@setOnMenuItemClickListener when (item.itemId) {
                    R.id.unFollow -> {
                        withConfirmation(UnfollowUser()) { viewModel.unFollowUser() }
                        true
                    }

                    R.id.ignore -> {
                        withConfirmation(IgnoreUser()) { viewModel.ignoreUser() }
                        true
                    }

                    R.id.unIgnore -> {
                        viewModel.unIgnoreUser()
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun setupObservers() {
        NetworkObserver.observe(this) {
            binding.toolbar.apply {
                isEnabled = it
                setEnabledChildren(it)
            }
        }
        viewModel.userLiveData.observeData(this) { setupUserInfo(it) }
        viewModel.timelineLiveDataLiveData.observeData(this) {
            usersCirclesAdapter.submitList(it)
            binding.tvEmptyCirclesList.setIsVisible(it.isEmpty())
        }
        viewModel.requestFollowLiveData.observeResponse(this,
            success = { showSuccess(getString(R.string.request_sent)) })
        viewModel.ignoreUserLiveData.observeResponse(this,
            success = {
                context?.let { showSuccess(it.getString(R.string.user_ignored)) }
                onBackPressed()
            })
        viewModel.unIgnoreUserLiveData.observeResponse(this,
            success = {
                context?.let { showSuccess(it.getString(R.string.user_unignored)) }
            })
        viewModel.isUserIgnoredLiveData?.observeData(this) {
            isUserIgnored = it
            binding.toolbar.invalidateMenu()
        }
        viewModel.unFollowUserLiveData.observeResponse(this,
            success = { onBackPressed() })
    }

    private fun setupUserInfo(user: User) {
        with(binding) {
            toolbar.title = user.notEmptyDisplayName()
            tvUserId.text = user.userId
            tvUserName.text = user.notEmptyDisplayName()
            ivUser.loadProfileIcon(user.avatarUrl, user.notEmptyDisplayName())
            tvEmptyCirclesList.text =
                getString(R.string.not_following_any_circles_format, user.notEmptyDisplayName())
        }
    }

}