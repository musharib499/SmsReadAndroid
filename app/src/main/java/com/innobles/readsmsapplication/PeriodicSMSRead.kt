package com.innobles.readsmsapplication

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.os.Build
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * Created by Musharib Ali on 2020-01-18.
 * I.S.T Pvt. Ltd
 * musharib.ali@innobles.com
 */
class PeriodicSMSRead(context: Context,workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private var context: Context = context
    private var viewModel:MainViewModel? = null
    var sharedPreference:SharedPreferences? = null
    var smsId :String  = ""
    var checkSMSID:String  = ""
    var regEx: Pattern = Pattern.compile("(?i)(?:(?:RS|INR|MRP)\\.?\\s?)(\\d+(:?\\,\\d+)?(\\,\\d+)?(\\.\\d{1,2})?)")
    private val database: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference.child("smsReadList")
    }
    override suspend fun doWork(): Result = coroutineScope{
        sharedPreference  =  context.getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
       smsId = sharedPreference?.getString("smsId", "") ?: ""
        getAllSms()
        Log.d("TIME_SYNC",SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(System.currentTimeMillis()))
        Result.success()
    }

    fun saveKey(id:String = ""){
        var editor = sharedPreference?.edit()
        editor?.putString("smsId",id)
        editor?.commit()
    }




    fun getAllSms() {
       // dataLoading.value = true
        val lstSms: MutableList<MessageInfo> = ArrayList()
        val date: Long =
            Date(System.currentTimeMillis() - 15 * 3600 * 1000).getTime()
        val cr = applicationContext.contentResolver
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
            val totalSMS: Int = it.count
            var k:Int  = 0

            if (totalSMS!=0 && it.moveToFirst()) {

                for (i in 0 until totalSMS) {

                    if (c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)).contains("1")) {
                        var messageInfo = MessageInfo(
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(it.getColumnIndexOrThrow(Telephony.Sms.DATE)),
                            it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY)),
                            it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))

                        )
                        if (!it.getString(it.getColumnIndexOrThrow(Telephony.Sms._ID)).equals(smsId)) {
                            if (regEx.matcher(messageInfo.message).find()) {

                                database?.push().setValue(messageInfo)
                                if (k > 10){
                                    break
                                }else{
                                    k += 1
                                    if (k == 1){
                                        checkSMSID  = it.getString(it.getColumnIndexOrThrow(Telephony.Sms._ID))
                                        saveKey(checkSMSID)
                                    }

                                }

                            }
                        }else{
                           // saveKey(checkSMSID)
                            break

                        }
                        it.moveToNext()
                    }
                }
            } else {
                Log.d("SMS","You have no SMS in Inbox")
                it.close()
                //throw RuntimeException("You have no SMS in Inbox")
            }
            it.close()
        }
        //dataLoading.value = false
       /// smsListLive.postValue(lstSms)

    }


}