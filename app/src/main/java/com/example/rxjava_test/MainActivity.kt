package com.example.rxjava_test

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * 비동기 처리와 동기 처리 이해하는방법
 * Next에 넣으면 순차적으로 같은스레드에서 움직이는데
 * subscribeOn에 Schedulers.io()로 스레드를 하나 추가해 그쪽에서 돌게함
 * 진짜 잘쓰면 유용할듯*/
class MainActivity : AppCompatActivity() {
    var logtext : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var MainThread : TextView = findViewById(R.id.mainthread)
        var IOThread : TextView = findViewById(R.id.iothread)
        var NewThread : TextView = findViewById(R.id.newthread)
        var log : TextView = findViewById(R.id.log)

        val handler = Handler(Looper.getMainLooper()){
            when (it.what){
                1 -> MainThread.visibility = View.VISIBLE
                2 -> IOThread.visibility = View.VISIBLE
                3 -> NewThread.visibility = View.VISIBLE
                4 -> log.text = logtext
                5 -> Threat().run()
            }
            true
        }

        Observable.just("하나","둘","셋","넷","다섯")
            .doOnNext {
                Log.i("여기",getThreadName()+" : doOnNext() : "+it.toString())
                logtext += getThreadName()+" : doOnNext() : "+it.toString()+"\n"
                handler.obtainMessage(4).sendToTarget()
                Thread.sleep(2000)
            }
            .subscribeOn(Schedulers.io())//동기 I/O를 별도로 처리시켜 비동기 효율을 얻을때 쓰는 스케쥴러 API호출같은거쓸떄씀
            .filter { it.length > 1 }
            .elementAt(4)
            .doOnComplete {
                Log.i("여기",getThreadName()+" => Complete")
                logtext += getThreadName()+" => Complete"+"\n"
                handler.obtainMessage(4).sendToTarget()
                handler.obtainMessage(2).sendToTarget()
            }
            .subscribe{
                Log.i("여기",getThreadName()+" : result : "+it.toString())
            }


        Observable.just(100,200,300,400,500)
            .doOnNext {
                Log.i("여기",getThreadName()+" : doOnNext() : "+it.toString())
                logtext += getThreadName()+" : doOnNext() : "+it.toString()+"\n"
                handler.obtainMessage(4).sendToTarget()
                Thread.sleep(3000)
            }
            .doOnComplete { Log.i("여기",getThreadName()+" => Complete") }
            .subscribeOn(Schedulers.newThread())//새로운 스레드를 만든다
//            .observeOn(Schedulers.computation())
            .filter { it > 300 }
            .doOnComplete {
                Log.i("여기",getThreadName()+" => Complete")
                logtext += getThreadName()+" => Complete"+"\n"
                handler.obtainMessage(4).sendToTarget()
                handler.obtainMessage(3).sendToTarget()
            }
            .subscribe {
                Log.i("여기",getThreadName()+" : result : "+it.toString())
//                NewThread.setText("종료")
            }
//        handler.obtainMessage(5).sendToTarget()
        handler.obtainMessage(1).sendToTarget()
    }
    fun getThreadName() :String {
        return Thread.currentThread().name
    }
    class Threat : Thread() {
        override fun run() {
            for (i in 0..3){
                Log.i("여기",Thread.currentThread().name+" "+i)
                sleep(1000)
            }
            Log.i("여기",Thread.currentThread().name+" => Complete")
        }
    }
}