package com.example.bookloverfinalapp.app.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.bookloverfinalapp.R
import com.example.bookloverfinalapp.app.models.User
import com.example.bookloverfinalapp.app.utils.dialog.LoadingDialog
import com.example.bookloverfinalapp.app.utils.extensions.showSnackbar
import com.example.bookloverfinalapp.app.utils.extensions.showToast
import com.example.bookloverfinalapp.app.utils.navigation.NavigationCommand
import com.example.bookloverfinalapp.app.utils.pref.CurrentUser

abstract class BaseFragment<V : ViewBinding, VM : BaseViewModel>(
    private val binder: (LayoutInflater, ViewGroup?, Boolean) -> V,
) : Fragment() {

    protected abstract val viewModel: VM

    private var viewBinding: V? = null

    protected fun binding(): V =
        checkNotNull(viewBinding)

    protected val currentUser: User by lazy(LazyThreadSafetyMode.NONE) {
        CurrentUser().getCurrentUser(activity = requireActivity())
    }

    protected abstract fun onReady(savedInstanceState: Bundle?)

    protected val loadingDialog: LoadingDialog by lazy(LazyThreadSafetyMode.NONE) {
        LoadingDialog(context = requireContext(), getString(R.string.loading_please_wait))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = binder.invoke(inflater, container, false)
        this.viewBinding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onReady(savedInstanceState)
//        setupTransitions(view = view)
        observeRecourse()
    }

    private fun observeRecourse() {
        viewModel.observeNavigation(viewLifecycleOwner) {
            it.getValue()?.let { navigationCommand ->
                handleNavigation(navigationCommand)
            }
        }
        viewModel.observeProgressDialog(viewLifecycleOwner) {
            it.getValue()?.let { status ->
                if (status) loadingDialog.show()
                else loadingDialog.dismiss()
            }
        }

        viewModel.observeNetworkError(viewLifecycleOwner) {
            it.getValue()?.let {
                showSnackbar(binding().root, getString(R.string.network_error))
            }
        }
        viewModel.observeError(viewLifecycleOwner) {
            it.getValue()?.let { message ->
                showToast(message = message)
            }
        }
    }

    fun showToast(@StringRes messageRes: Int) {
        Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
    }
//    private fun setupTransitions(view: View) {
//        postponeEnterTransition()
//        view.doOnPreDraw { startPostponedEnterTransition() }
//        exitTransition = MaterialFadeThrough().apply { duration = 2000 }
//        reenterTransition = MaterialFadeThrough().apply { duration = 2000 }
//    }

    private fun handleNavigation(navCommand: NavigationCommand) {
        when (navCommand) {
            is NavigationCommand.ToDirection -> findNavController().navigate(navCommand.directions)
            is NavigationCommand.Back -> findNavController().navigateUp()
        }
    }

    override fun onDestroy() {
        viewBinding = null
        super.onDestroy()
    }
}