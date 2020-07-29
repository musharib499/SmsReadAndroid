package com.innobles.readsmsapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Created by Musharib Ali on 26/07/20.
 * I.S.T Pvt. Ltd
 * musharib.ali@innobles.com
 */
open class BaseViewModel : ViewModel() {

    val empty = MutableLiveData<Boolean>().apply { value = false }

    val dataLoading = MutableLiveData<Boolean>().apply { value = false }

    val toastMessage = MutableLiveData<String>()
}