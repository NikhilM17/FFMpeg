package com.example.ff_mepg;

import android.os.AsyncTask;
import android.util.Log;

import com.arthenica.mobileffmpeg.FFmpeg;

import org.jetbrains.annotations.NotNull;

public class MergerAsync extends AsyncTask<Integer, Void, Void> {

    @Override
    protected Void doInBackground(Integer... integers) {
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.e("MainActivity", "Success");
    }
}
