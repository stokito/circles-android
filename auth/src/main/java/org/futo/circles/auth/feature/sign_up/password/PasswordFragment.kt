package org.futo.circles.auth.feature.sign_up.password

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.auth.R
import org.futo.circles.auth.databinding.FragmentPasswordBinding
import org.futo.circles.auth.model.PasswordModeArg
import org.futo.circles.core.extensions.getText
import org.futo.circles.core.extensions.observeResponse
import org.futo.circles.core.extensions.setIsVisible
import org.futo.circles.core.base.fragment.HasLoadingState
import org.futo.circles.core.base.fragment.ParentBackPressOwnerFragment

@AndroidEntryPoint
class PasswordFragment : ParentBackPressOwnerFragment(R.layout.fragment_password),
    HasLoadingState {

    private val args: PasswordFragmentArgs by navArgs()
    private val viewModel by viewModels<PasswordViewModel>()
    override val fragment: Fragment = this
    private val binding by viewBinding(FragmentPasswordBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        with(binding) {
            btnLogin.apply {
                setText(getString(if (isSignupMode()) R.string.set_password else R.string.log_in))
                setOnClickListener {
                    startLoading(btnLogin)
                    viewModel.loginWithPassword(tilPassword.getText())
                }
            }
            tilPassword.editText?.apply {
                doAfterTextChanged {
                    if (isSignupMode()) vPasswordStrength.calculateStrength(tilPassword.getText())
                    onPasswordDataChanged()
                }
                imeOptions = if (isSignupMode()) EditorInfo.IME_ACTION_NEXT
                else EditorInfo.IME_ACTION_DONE
            }
            tilRepeatPassword.editText?.doAfterTextChanged { onPasswordDataChanged() }
            tilRepeatPassword.setIsVisible(isSignupMode())
        }
    }

    private fun isSignupMode() = when (args.mode) {
        PasswordModeArg.ReAuthBsSpekeSignup, PasswordModeArg.SignupPasswordStage, PasswordModeArg.SignupBsSpekeStage -> true
        else -> false
    }

    private fun setupObservers() {
        viewModel.passwordResponseLiveData.observeResponse(this)
    }

    private fun onPasswordDataChanged() {
        val password = binding.tilPassword.getText()
        val repeat = binding.tilRepeatPassword.getText()
        binding.btnLogin.isEnabled = if (isSignupMode()) {
            binding.vPasswordStrength.isPasswordStrong() && password == repeat
        } else password.isNotEmpty()
    }
}