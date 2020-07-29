package com.innobles.readsmsapplication

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.innobles.readsmsapplication.databinding.ActivityMainBinding
import com.innobles.readsmsapplication.databinding.ItemSmsBinding
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(),BaseAdapterBinding.BindAdapterListener {
    private var adapter: BaseAdapterBinding<MessageInfo>? = null
    private var binding:ActivityMainBinding? = null
    private var viewModel:MainViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.apply {
            viewModel = ViewModelProviders.of(this@MainActivity).get(MainViewModel::class.java)
            viewModel?.contentResolver = contentResolver
            setRecyclerView()
            this?.activity = this@MainActivity
        }


    }

    private fun setRecyclerView() {
        adapter = BaseAdapterBinding(this, arrayListOf(), this, R.layout.item_sms)
        binding?.recyclerView?.adapter = adapter

    }



    override fun onBind(holder: BaseAdapterBinding.DataBindingViewHolder, position: Int) {
        (holder.binding as ItemSmsBinding).apply {
            this.item = adapter?.getItem(position)
        }

    }

    fun onManualSync(){
        setSyncPeriodic()
        viewModel?.getAllSms()
    }

    fun setSyncPeriodic(){
        var periodicWorkRequest = PeriodicWorkRequest.Builder(
            PeriodicSMSRead::class.java,
            15, TimeUnit.MINUTES
        )   .addTag("SMS")
            .build()

        WorkManager.getInstance(applicationContext).cancelAllWork()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork("SMS",
            ExistingPeriodicWorkPolicy.REPLACE,periodicWorkRequest)
    }

    override fun onResume() {
        super.onResume()
        // For Permission
        Permissions.check(this,Manifest.permission.READ_SMS,null,object : PermissionHandler(){
            override fun onGranted() {
                if (adapter?.itemCount ==  0) {
                    viewModel?.getAllSms()
                    viewModel?.smsListLive?.observe(this@MainActivity, Observer {
                        adapter?.setData(it)
                        adapter?.notifyDataSetChanged()
                    })
                    setSyncPeriodic()
                }

            }

        })
    }


}