package br.com.frazo.janac

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JANACApplication : Application() {

//    init {
//        CoroutineScope(Dispatchers.IO).launch {
//            RoomAppDatabase.getDataBase(applicationContext).notesDAO().deleteAllNoFilter()
//        }
//    }
}