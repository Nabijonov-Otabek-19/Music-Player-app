package uz.gita.musicplayer_bek.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import uz.gita.musicplayer_bek.data.model.CommandEnum
import uz.gita.musicplayer_bek.utils.base.manageMusicService
import uz.gita.musicplayer_bek.utils.logger

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            when (telephonyManager.callState) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    // Handle incoming call and pause music
                    manageMusicService(context, CommandEnum.PAUSE)
                    logger("Incoming call")
                }

                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    // Handle outgoing call and pause music
                    manageMusicService(context, CommandEnum.PAUSE)
                    logger("Outcoming call")
                }

                TelephonyManager.CALL_STATE_IDLE -> {
                    // Handle phone call is end
                    logger("Phone call is end")
                }
            }
        }
    }
}
