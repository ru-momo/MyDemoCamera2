package com.momo.camera2test;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class SaveService extends Service {

    private final IBinder mBinder = new LocalBinder();

    class LocalBinder extends Binder {
        public SaveService getService() {
            return SaveService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


}
