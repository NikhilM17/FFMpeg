package com.example.ff_mepg

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


abstract class VideoPickerFragment : Fragment() {
    private var permissionsAlert: AlertDialog? = null

    val REQUEST_CODE_CHOOSE = 976

    protected fun openGallery() {

        if (hasStoragePermissions()) {
            getVideos.launch(arrayOf("video/*"))
        } else if (hasDeniedPermissions()) {
            showPermissionsAlert()
        } else {
            askPermissions()
        }
    }

    private val getVideos = registerForActivityResult(ActivityResultContracts.OpenDocument()) {

        try {
            it?.apply {
                val contentResolver = requireContext().applicationContext.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    contentResolver.takePersistableUriPermission(it, takeFlags)
                }

                onVideoPicked(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun hasStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }


    private val askPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (hasStoragePermissions()) {
                openGallery()
            } else if (hasDeniedPermissions()) {
                showPermissionsAlert()
            }
        }

    private fun askPermissions() {
        askPermissions.launch(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }


    private fun hasDeniedPermissions(): Boolean {
        return shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun launchAppSettings() {
        launchAppSettings.launch(null)
    }


    private val launchAppSettings = registerForActivityResult(SettingsContract()) {

    }


    abstract fun onVideoPicked(uri: Uri)


    private fun showPermissionsAlert() {
        permissionsAlert?.dismiss()
        permissionsAlert = AlertDialog.Builder(context).create()
        permissionsAlert?.apply {
            setTitle("Allow storage permissions")
            setMessage("In order to upload your videos, you need to provide permissions to access your file storage.")
            setButton(DialogInterface.BUTTON_NEGATIVE, "No") { _, _ ->
                dismiss()
            }

            setButton(DialogInterface.BUTTON_POSITIVE, "Yes") { _, _ ->
                dismiss()
                launchAppSettings()
            }

        }

    }


    private fun getPath(uri: Uri?): String? {
        var path: String? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uri?.apply {
                    path = toString()
                    requireActivity().applicationContext.contentResolver.takePersistableUriPermission(
                        this, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                }

            } else {
                uri?.let {
                    val projection = arrayOf(MediaStore.Video.Media.DATA)

                    val cursor =
                        requireContext().contentResolver.query(it, projection, null, null, null)

                    cursor?.apply {
                        // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
                        // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
                        val column_index = getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                        moveToFirst()
                        path = getString(column_index)
                        close()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return path
    }

    class PickVideo : ActivityResultContract<String?, Uri?>() {
        override fun createIntent(
            context: Context,
            input: String?
        ): Intent {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                .setType(input)
            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)

            return Intent.createChooser(intent, "Select Video")
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return if (intent == null || resultCode != RESULT_OK) null else intent.data
        }
    }

    class SettingsContract : ActivityResultContract<String?, Boolean>() {
        override fun createIntent(context: Context, input: String?): Intent {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", context.packageName, null)
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return true
        }
    }
}