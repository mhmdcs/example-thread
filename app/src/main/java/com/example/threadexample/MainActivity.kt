package com.example.threadexample

import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button

    // by default, the Handler object is associated with the thread where we instantiated it, and since Activity class runs on the main thread, it'll be associated with the main thread always and will only schedule work on the main thread's message queue. If we for example created this Handler object inside a background thread, it will be associated with that background thread.
    // the empty constructor for Handler has been deprecated, because google now wants you to explicitly state which thread's Looper this Handle must be bound with, you can do this by either passing Looper.getMainLooper() or Looper.myLooper()!! inside the constructor, note that the latter will ONLY work if you're currently inside a thread that has a Looper, else it'll result in a crash.
    private val handler: Handler = Handler() // Handler class is an Android SDK class that makes it more convenient to pass work to be done between different threads.

    @Volatile // Volatile annotation makes sure that all threads access the most updated version of this variable, and not a cached version from the CPU's register.
    private var stopThread: Boolean = false // if you want to force a thread to stop before its run() method finishes, you can do this with a boolean flag variable.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.start_btn)
    }

    fun startThread(view: View) {
        stopThread = false

        // creating a background thread by creating an object of a class that implements Runnable and then passing that object to a Thread object constructor
        val runnable: ExampleRunnable = ExampleRunnable(10)
        Thread(runnable).start()

        // creating a background thread by creating an object of a class that extends Thread
        // actually even here a Runnable instance was created and its run method was implemented :) if we look inside Thread's class we'll have that it implements Runnable as well.
//        val thread: ExampleThread = ExampleThread(10)
//        thread.start() // if you call the start() method again on the same thread object, it'll crash because a thread object can only be used once, and once it finishes(dies) it becomes unusable.

        // starting a new thread via anonymous objects
        Thread(object: Runnable {
            override fun run() {
                // some work to be done on a background thread
                runOnUiThread(object: Runnable {
                    override fun run() {
                        // some work to be done on the main thread
                    }

                })
            }
        })

        // starting a new thread via anonymous objects and SAM lambda conversion, which implicitly creates anonymous objects of Runnable interface and puts our lambda block of code inside the run() method.
        Thread {
            // some work to be done on a background thread
           runOnUiThread {
                // some work to be done on the main thread
            }
        }
    }


    fun stopThread(view: View) {
        stopThread = true
    }

    inner class ExampleThread(private val seconds: Int) : Thread() {

        override fun run() {
            super.run() // you can safely omit the super method call since the run() method inside the Thread class does nothing important at all.
            for (count in 1..seconds) {
                Thread.sleep(1000) // or SystemClock.sleep(1000)
                Log.d(TAG, "startThread: $count")
            }
        }
    }

    inner class ExampleRunnable(private val seconds: Int) : Runnable {
        override fun run() {
            for (count in 1..seconds) {
                if (stopThread) // if stopThread is true, then we want to return from this method, which will stop this thread because its run method will be done.
                    return
                if (count == 5) {
                    handler.post(object : Runnable {
                        override fun run() {
                            startButton.text = "Changed" // if we execute this in a background thread the app will crash, because only the UI/main thread is allowed to access UI widgets/views, all Android UI widgets/views are not thread-safe, and must always be executed on the main thread.
                            // We can update a view while we're inside a background by various ways, we could use a Handler object and call its post() method (then either use a lambda and wrap our code inside or create an anonymous Runnable object explicitly and pass it as an argument to the post() method, or wrap our code inside a runOnUiThread {} defined inside Activity class or startButton.post {} defined inside View class, which all use a Handler internally too.
                            // The Handler's post() method causes its passed Runnable object to be added to the message queue. And that runnable will run on the thread of which the handler was attached to.
                        }

                    })
                }

                Thread.sleep(1000) // or SystemClock.sleep(1000)
                Log.d(TAG, "startThread: $count")
            }
        }
    }
}