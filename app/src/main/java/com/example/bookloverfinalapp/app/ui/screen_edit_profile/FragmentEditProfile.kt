package com.example.bookloverfinalapp.app.ui.screen_edit_profile

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.bookloverfinalapp.R
import com.example.bookloverfinalapp.app.base.BaseFragment
import com.example.bookloverfinalapp.app.models.User
import com.example.bookloverfinalapp.app.models.UserImage
import com.example.bookloverfinalapp.app.utils.cons.RESULT_LOAD_IMAGE
import com.example.bookloverfinalapp.app.utils.extensions.*
import com.example.bookloverfinalapp.app.utils.pref.CurrentUser
import com.example.bookloverfinalapp.databinding.FragmentEditProfileBinding
import com.example.domain.models.UserUpdateDomain
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.parse.ParseFile
import com.parse.SaveCallback
import dagger.hilt.android.AndroidEntryPoint


@Suppress("DEPRECATION")
@AndroidEntryPoint
class FragmentEditProfile :
    BaseFragment<FragmentEditProfileBinding, FragmentEditProfileViewModel>(
        FragmentEditProfileBinding::inflate), View.OnClickListener {

    override val viewModel: FragmentEditProfileViewModel by viewModels()

    override fun onReady(savedInstanceState: Bundle?) {}

    private var parseFile: ParseFile? = null
    private var image: UserImage? = null

    private lateinit var student: User
    private lateinit var gender: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding().apply {
            saveEditButton.setOnClickListener(this@FragmentEditProfile)
            editStudentImage.setOnClickListener(this@FragmentEditProfile)
            confirmPasswordBtn.setOnClickListener(this@FragmentEditProfile)
            female.setOnClickListener(this@FragmentEditProfile)
            male.setOnClickListener(this@FragmentEditProfile)
            toolbar.setNavigationOnClickListener { viewModel.goBack() }
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding().saveEditButton -> checkChanges()
            binding().editStudentImage -> getImage()
            binding().confirmPasswordBtn -> chekConfirmPassword()
            binding().male -> gender = "male"
            binding().female -> gender = "female"
        }
    }

    private fun chekConfirmPassword() {
        binding().apply {
            if (editStudentPasswordConfirm.text.toString() == student.password) {
                editStudentNumber.isEnabled = true
                editStudentEmail.isEnabled = true
            } else {
                editStudentNumber.isEnabled = false
                editStudentEmail.isEnabled = false
                showToast(message = getString(R.string.invalid_password_error))
            }
        }
    }

    private fun setupUi() {
        student = CurrentUser().getCurrentUser(activity = requireActivity())
        binding().apply {
            when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> materialCardView.setBackgroundColor(Color.parseColor(
                    "#2A00A2"))
                Configuration.UI_MODE_NIGHT_YES -> materialCardView.setBackgroundColor(Color.parseColor(
                    "#305F72"))
            }
            gender = student.gender
            editStudentNumber.setText(student.number)
            editStudentName.setText(student.name)
            editStudentLastName.setText(student.lastname)
            editStudentEmail.setText(student.email)
            if (student.gender == "female") female.isChecked = true
            else male.isChecked = true
            requireContext().glide(student.image?.url, binding().profileImg)
            toolbar.title = getString(R.string.my_profile)
            setToolbarColor(toolbar = toolbar)

        }
    }

    private fun checkChanges() {
        binding().apply {
            if (editStudentName.text.toString() == student.name &&
                editStudentLastName.text.toString() == student.lastname &&
                editStudentEmail.text.toString() == student.email &&
                editStudentNumber.text.toString() == student.number &&
                gender == student.gender && parseFile == null
            ) return@apply
            else checkImage()
        }
    }

    private fun checkImage() {
        binding().apply {
            if (!editStudentName.validateName()) showToast(message = getString(R.string.name_input_format_error))
            else if (!editStudentLastName.validateLastName()) showToast(message = getString(R.string.last_name_input_format_error))
            else if (!editStudentNumber.validateEditPhone()) showToast(message = getString(R.string.phone_input_format_error))
            else if (!editStudentEmail.validateEmail()) showToast(message = getString(R.string.email_input_format_error))
            else {
                if (parseFile == null) {
                    image = student.image
                    updateStudent()
                } else {
                    loadingDialog.show()
                    saveImage()
                }
            }
        }
    }

    private fun saveImage() {
        parseFile!!.saveInBackground(SaveCallback {
            if (it == null) {
                image = parseFile!!.toImage()
                updateStudent()
            } else {
                loadingDialog.dismiss()
                showToast(it.message!!)
            }
        })
    }

    private fun updateStudent() {
        binding().apply {
            val student = UserUpdateDomain(
                gender = gender,
                name = editStudentName.text.toString(),
                lastname = editStudentLastName.text.toString(),
                email = editStudentEmail.text.toString(),
                number = editStudentNumber.text.toString(),
                image = image!!.toDto()
            )
            viewModel.updateStudent(id = this@FragmentEditProfile.student.id,
                user = student, sessionToken = currentUser.sessionToken)
                .observe(viewLifecycleOwner) { successUpdated() }
        }
    }

    private fun successUpdated() {
        binding().apply {
            val newStudent = User(
                gender = gender,
                name = editStudentName.text.toString(),
                lastname = editStudentLastName.text.toString(),
                email = editStudentEmail.text.toString(),
                password = student.password.toString(),
                number = editStudentNumber.text.toString(),
                image = image!!,
                classId = student.classId,
                className = student.className,
                schoolName = student.schoolName,
                id = student.id,
                createAt = student.createAt,
                userType = student.userType,
                sessionToken = student.sessionToken,
                schoolId = student.schoolId
            )
            CurrentUser().saveCurrentUser(user = newStudent, requireActivity())
            student = CurrentUser().getCurrentUser(activity = requireActivity())
            showToast(message = getString(R.string.profile_has_been_successfully_updated))
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            val uri = data.data!!
            parseFile = ParseFile("image.png", uriToImage(uri))
            requireContext().glide(uri, binding().profileImg)
        }
    }


    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).hideView()
    }
}