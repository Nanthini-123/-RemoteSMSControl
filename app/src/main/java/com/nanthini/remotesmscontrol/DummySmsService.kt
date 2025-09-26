package com.nanthini.remotesmscontrol

import android.app.Service
import android.content.Intent
import android.os.IBinder

// You need this even if unused, else Android won't set you as default SMS app
class DummySmsService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}