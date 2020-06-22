package com.example.ff_mepg

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.FFmpeg
import com.blackbox.ffmpeg.examples.callback.FFMpegCallback
import com.blackbox.ffmpeg.examples.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.net.URI

class MainActivity : AppCompatActivity(), View.OnClickListener, FFMpegCallback {

    private val fFmpeg: FFmpeg? = null
    private var player: MediaPlayer? = null

    private var videoUri: Uri? = null
    private var audioUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnVideoUpload.setOnClickListener(this)
        btnAudioUpload.setOnClickListener(this)
        ivPlay.setOnClickListener(this)
        ivStop.setOnClickListener(this)
        btnMerge.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnAudioUpload -> {
                getImages.launch(arrayOf("audio/*"))
            }
            R.id.btnVideoUpload -> {
                getVideos.launch(arrayOf("video/*"))
            }
            R.id.ivPlay -> {
                player?.apply {
                    this.start()
                }
            }
            R.id.ivStop -> {
                player?.apply {
                    this.stop()
                }
            }
            R.id.btnMerge -> {
                /*val audioFile = Utils.copyFileToExternalStorage(
                    R.raw.crazy_feeling,
                    "crazy_feeling.mp3",
                    applicationContext
                )

                val videoFile = Utils.copyFileToExternalStorage(
                    R.raw.endhukante_pemanta,
                    "endhukante_pemanta",
                    applicationContext
                )*/

                Merger
                    .mergeAV(applicationContext)
                    /*.setAudioFile(
                        audioFile
                    )
                    .setVideoFile(
                        videoFile
                    )*/
                    .setAudioFile(File(audioUri?.path!!))
                    .setVideoFile(File(videoUri?.path!!))
                    .setOutputPath(Utils.outputPath + "video")
                    .setOutputFileName("merged_" + System.currentTimeMillis() + ".mp4")
                    .setCallback(this@MainActivity)
                    .merge()
            }
        }
    }

    private val getImages = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        try {
            it?.apply {
                audioUri = this
                val contentResolver = applicationContext.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    contentResolver.takePersistableUriPermission(this, takeFlags)
                }
                player = MediaPlayer.create(applicationContext, this)
                linearControl.visibility = VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val getVideos = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        try {
            it?.apply {
                videoUri = this
                val contentResolver = applicationContext.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    contentResolver.takePersistableUriPermission(this, takeFlags)
                }
                videoView.setVideoURI(this)
                videoView.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onProgress(progress: String) {
        Log.e("MainActivity", progress)
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.e("MainActivity", type)
    }

    override fun onFailure(error: Exception) {
        Log.e("MainActivity", error.message!!)
    }

    override fun onNotAvailable(error: Exception) {
        Log.e("MainActivity", error.message!!)
    }

    override fun onFinish() {
        Log.e("MainActivity", "Finished")
    }
}