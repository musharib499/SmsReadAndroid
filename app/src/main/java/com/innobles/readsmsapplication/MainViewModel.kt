package com.innobles.readsmsapplication

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.provider.Telephony
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.regex.Pattern

/**
 * Created by Musharib Ali on 26/07/20.
 * I.S.T Pvt. Ltd
 * musharib.ali@innobles.com
 */
class MainViewModel():BaseViewModel(){
    val smsListLive = MutableLiveData<List<MessageInfo>>()
    var contentResolver:ContentResolver? = null
    var regEx: Pattern = Pattern.compile("(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)")

    @SuppressLint("NewApi")
    fun getAllSms() {
        dataLoading.value = true
        val lstSms: MutableList<MessageInfo> = ArrayList()
        val cr = contentResolver
        val c: Cursor? = cr?.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            null,  // Select body text
            null,
            null,
            Telephony.Sms.Inbox.DEFAULT_SORT_ORDER
        ) // Default
        // sort
        // order);
        c?.let {
            val totalSMS: Int = if (it.count>10){10}else{it.count}

            if (totalSMS!=0 && it.moveToFirst()) {
                for (i in 0 until totalSMS) {
                    if (c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)).contains("1")) {
                        var messageInfo = MessageInfo(
                            it.getString(it.getColumnIndexOrThrow(Telephony.Sms.DATE)),
                            it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY)),
                            it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))

                        )
                        if (regEx.matcher(messageInfo.message).find()) {
                            lstSms.add(messageInfo)
                        }
                        it.moveToNext()
                    }
                }
            } else {

                Log.d("SMS","You have no SMS in Inbox")
                it.close()
            }
            it.close()
        }
        dataLoading.value = false
        smsListLive.postValue(lstSms)

    }

}