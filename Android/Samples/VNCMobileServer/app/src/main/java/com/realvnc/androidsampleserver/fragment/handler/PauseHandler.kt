package com.realvnc.androidsampleserver.fragment.handler

import android.os.Bundle
import android.os.Handler
import android.os.Message
//import android.util.Log


/**
 * Pause中の非同期処理をキューイングし、Resumeで実行する機能を持つHandlerクラス
 * シングルトンクラスです!!!
 * （IllegalStateException対応のため追加）
 */
object PauseHandler: Handler() {
    /**
     * ログ用TAG名
     */
    //private val TAG = "VNCMobileServer"
    //private val TAG = "PauseHandler"

    /**
     * 処理待ちメッセージキュー
     */
    private var messageQueueBuffer = mutableListOf<Message>()

    /**
     * Pause中かどうかのフラグ（true:Pause状態, false: Resume状態）
     */
    private var isPaused = false

    /**
     * メッセージ処理用メソッド(返り値はtrue固定)
     */
    private var processMessage: ((Message) -> Boolean)? = null


    //---------------------------------------------------
    // Override methods
    //---------------------------------------------------

    /**
     * メッセージ処理
     *
     * @param message メッセージ
     */
    override fun handleMessage(message: Message) {
        //Log.d(TAG, "PauseHandler.handleMessage() >> isPaused = " + isPaused.toString())

        if (isPaused) {
            // Pause状態のときは、処理できないので、メッセージをキューに追加します
            val msgCopy = Message()
            msgCopy.copyFrom(message)
            messageQueueBuffer.add(msgCopy)
        }
        else {
            // Resume状態のときは、メッセージを処理します
            if(this.processMessage != null) {
                // メッセージ処理するため、事前に設定されている処理メソッドを呼び出します
                this.processMessage?.invoke(message)
            }
        }
        //Log.d(TAG, "PauseHandler.handleMessage() >> remain size = " + messageQueueBuffer.size.toString())
    }

    //---------------------------------------------------
    // Public methods
    //---------------------------------------------------

    /**
     * メッセージ処理するためのメソッドを登録する
     *
     * @param processMessage メッセージ処理用メソッド
     */
    fun setProcessMessage(processMessage: ((Message) -> Boolean)?) {
        this.processMessage = processMessage
    }


    /**
     * onResume()で、呼び出すメソッド
     */
    fun resume() {
        // Resume状態に遷移
        isPaused = false

        //Log.d(TAG, "PauseHandler.resume() >> ThreadID = " + Thread.currentThread().id.toString())
        //Log.d(TAG, "PauseHandler.resume() >> isPaused = " + isPaused.toString() + ", Size = " + messageQueueBuffer.size.toString())

        // キューに溜まっているメッセージを処理する
        while (messageQueueBuffer.size > 0) {
            // リストの先頭の処理待ちメッセージを取得
            val msg = messageQueueBuffer.get(0)
            messageQueueBuffer.removeAt(0)

            //Log.d(TAG, "PauseHandler.resume() >> SND MSG - What = " + msg.what.toString())

            // メッセージ送信
            sendMessage(msg)
        }
    }

    /**
     * OnPause()で、呼び出すメソッド
     */
    fun pause() {
        // Pause状態に遷移
        isPaused = true

        //Log.d(TAG, "PauseHandler.pause() >> ThreadID = " + Thread.currentThread().id.toString())
        //Log.d(TAG, "PauseHandler.pause() >> isPaused = " + isPaused.toString() + ", size = " + messageQueueBuffer.size.toString())
    }

    /**
     * メッセージ送信
     *
     * @param what メッセージID
     */
    fun sendMessage(what: Int) {
        this.sendMessage(what, null)
    }

    /**
     * メッセージ送信
     *
     * @param what メッセージID
     * @param bundle 付加情報
     */
    fun sendMessage(what: Int, bundle: Bundle?) {
        //Log.d(TAG, "PauseHandler.sendMessage() >> ThreadID = " + Thread.currentThread().id.toString())
        //Log.d(TAG, "PauseHandler.sendMessage() >> what = " + what.toString())

        if (!hasMessages(what)) {
            // 同じIDのメッセージがキューにない場合
            // メッセージ作成
            var message = obtainMessage(what)
            if (bundle != null) {
                // 付加データがあれば、セット
                message.data = bundle
            }
            // メッセージ送信する
            this.sendMessage(message)
        }
    }

    /**
     * メッセージをクリアする。
     *
     */
    fun clear() {
        //Log.d(TAG, "PauseHandler.clear() >> ThreadID = " + Thread.currentThread().id.toString())
        //Log.d(TAG, "PauseHandler.clear() >> clear size = " + messageQueueBuffer.size.toString())

        messageQueueBuffer.clear()
    }
}