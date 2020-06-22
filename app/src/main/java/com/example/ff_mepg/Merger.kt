package com.example.ff_mepg

import android.content.Context
import android.net.Uri
import com.arthenica.mobileffmpeg.FFmpeg
import com.blackbox.ffmpeg.examples.callback.FFMpegCallback
import com.blackbox.ffmpeg.examples.utils.Utils
import java.io.File
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Merger {

    companion object {
        fun mergeAV(
            context: Context
        ): AudioVideoMerger {
            return AudioVideoMerger(context)
        }
    }

    class AudioVideoMerger(private val context: Context) {

        private var audio: File? = null
        private var video: File? = null
        private var callback: FFMpegCallback? = null
        private var outputPath = ""
        private var outputFileName = ""

        fun setAudioFile(originalFiles: File): AudioVideoMerger {
            this.audio = originalFiles
            return this
        }

        fun setVideoFile(originalFiles: File): AudioVideoMerger {
            this.video = originalFiles
            return this
        }

        fun setCallback(callback: FFMpegCallback): AudioVideoMerger {
            this.callback = callback
            return this
        }

        fun setOutputPath(output: String): AudioVideoMerger {
            this.outputPath = output
            return this
        }

        fun setOutputFileName(output: String): AudioVideoMerger {
            this.outputFileName = output
            return this
        }

        fun merge() {

            if (audio == null || !audio!!.exists() || video == null || !video!!.exists()) {
                callback!!.onFailure(IOException("File not exists"))
                return
            }
            if (!audio!!.canRead() || !video!!.canRead()) {
                callback!!.onFailure(IOException("Can't read the file. Missing permission?"))
                return
            }

            try {
                val outputLocation = Utils.getConvertedFile(outputPath, outputFileName)

            //    -i video.mp4 -i genaudio.mp3 -map 0:v -map 1:a -c:v copy -c:a copy output.mp4 -y
                val cmd = arrayOf(
                    "-i",
                    video!!.path,
                    "-i",
                    audio!!.path,
                   /* "-c:v",
                    "copy",
                    "-c:a",
                    "aac",
                    "-strict",
                    "experimental",*/
                    "-map",
                    "0:v",
                    "-map",
                    "1:a",
                    "-c:v",
//                    "-shortest",
                    "copy",
                    outputLocation.path
                )

                try {
                    FFmpeg.execute(cmd)
//                    MergerAsync().execute(FFmpeg.execute(cmd))
                    /*FFmpeg.execute(cmd, object : ExecuteBinaryResponseHandler() {
                        override fun onStart() {}

                        override fun onProgress(message: String?) {
                            callback!!.onProgress(message!!)
                        }

                        override fun onSuccess(message: String?) {
                            Utils.refreshGallery(outputLocation.path, context)
                            callback!!.onSuccess(outputLocation, "video")

                        }

                        override fun onFailure(message: String?) {
                            if (outputLocation.exists()) {
                                outputLocation.delete()
                            }
                            callback!!.onFailure(IOException(message))
                        }

                        override fun onFinish() {
                            callback!!.onFinish()
                        }
                    })*/
                } catch (e: Exception) {
                    callback!!.onFailure(e)
                }
            } catch (e: Exception) {
                callback!!.onFailure(e)
                return
            }
        }
    }
}