package com.example.bookloverfinalapp.app.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.example.bookloverfinalapp.app.utils.dispatchers.Dispatchers
import com.example.bookloverfinalapp.app.utils.communication.ErrorCommunication
import com.example.bookloverfinalapp.app.utils.communication.NavigationCommunication
import com.example.bookloverfinalapp.app.utils.communication.ProgressCommunication
import com.example.bookloverfinalapp.app.utils.communication.ProgressDialogCommunication
import com.example.bookloverfinalapp.app.utils.event.Event
import com.example.bookloverfinalapp.app.utils.navigation.NavigationCommand


abstract class BaseViewModel : ViewModel() {

    private var progressCommunication = ProgressCommunication.Base()

    private var errorCommunication = ErrorCommunication.Base()

    private var navigationCommunication = NavigationCommunication.Base()

    private var progressDialogCommunication = ProgressDialogCommunication.Base()

    var dispatchers = Dispatchers.Base()

    fun observeProgressAnimation(owner: LifecycleOwner, observer: Observer<Event<Boolean>>) =
        progressCommunication.observe(owner = owner, observer = observer)

    fun observeProgressDialog(owner: LifecycleOwner, observer: Observer<Event<Boolean>>) =
        progressDialogCommunication.observe(owner = owner, observer = observer)

    fun observeNavigation(owner: LifecycleOwner, observer: Observer<Event<NavigationCommand>>) =
        navigationCommunication.observe(owner = owner, observer = observer)

    fun observeError(owner: LifecycleOwner, observer: Observer<Event<String>>) =
        errorCommunication.observe(owner = owner, observer = observer)

    fun navigate(navDirections: NavDirections) =
        navigationCommunication.put(Event(NavigationCommand.ToDirection(navDirections)))

    fun navigateBack() = navigationCommunication.put(Event(value = NavigationCommand.Back))

    fun error(message: String) = errorCommunication.put(Event(value = message))

    fun showProgressAnimation() = progressCommunication.put(Event(value = true))

    fun dismissProgressAnimation() = progressCommunication.put(Event(value = false))

    fun showProgressDialog() = progressDialogCommunication.put(source = Event(true))

    fun dismissProgressDialog() = progressDialogCommunication.put(source = Event(false))
}

