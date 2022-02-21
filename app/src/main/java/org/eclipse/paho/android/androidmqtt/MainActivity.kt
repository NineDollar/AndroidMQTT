package org.eclipse.paho.android.androidmqtt

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*


class MainActivity : AppCompatActivity() {
    private lateinit var mqttAndroidClient: MqttAndroidClient
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var data: Data

    /**\
     * Port cannot be specified
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Logger.addLogAdapter(AndroidLogAdapter())
        textView_show.movementMethod = ScrollingMovementMethod.getInstance()
        preferences = getSharedPreferences("data", MODE_PRIVATE)
        editor = preferences.edit()

        firstStart()
        data = getParamete()
        initView(data)

        image_run.visibility = View.INVISIBLE
        image_submit.visibility = View.INVISIBLE
        imageView_disconnect.visibility = View.INVISIBLE

        imageView_connect.apply {
            addClickScale()
            onClick {
                data = saveParamete(data)
                saveParamete(data)
                initConnect(data)
            }
        }
        image_submit.apply {
            addClickScale()
            onClick {
                Logger.d("Click the image_submit")
                publishMessage(data)
            }
        }
        image_run.apply {
            addClickScale()
            onClick {
                Logger.d("Click the image_run")
                image_stop.visibility = View.VISIBLE
                image_run.visibility = View.INVISIBLE
                subscribeToTopic(data)
            }
        }
        image_stop.apply {
            addClickScale()
            onClick {
                Logger.d("Click the image_stop")
                unsubscribe(data)
            }
        }
        textView_delete.apply {
            addClickScale()
            onClick {
                Logger.d("Click the textView_delete")
                textView_show.text = ""
            }
        }
        imageView_disconnect.apply {
            addClickScale()
            onClick {
                Logger.d("Click the imageView_disconnect")
                disconnect()
            }
        }
    }

    private fun disconnect() {
        isServiceWork(baseContext, "org.eclipse.paho.android.service.MqttService").also {
            if (it) {
                mqttAndroidClient.unregisterResources()
                Thread.sleep(50)
            }
        }
        try {
            mqttAndroidClient.disconnect()
            Logger.d("disconnect")
            image_run.visibility = View.INVISIBLE
            image_stop.visibility = View.INVISIBLE
            image_submit.visibility = View.INVISIBLE
            imageView_disconnect.visibility = View.INVISIBLE
            imageView_connect.visibility = View.VISIBLE
            refreshLogView("disconnect")
        } catch (ex: MqttException) {
            Logger.d("disconnect error ${ex.message}")
            ex.printStackTrace()
        }
    }

    private fun unsubscribe(data: Data) {
        mqttAndroidClient.unsubscribe(data.subTopic, null, object : IMqttActionListener {
            override fun onSuccess(p0: IMqttToken?) {
                image_stop.visibility = View.INVISIBLE
                image_run.visibility = View.VISIBLE
                refreshLogView("unsubscribe")
                Logger.d("unsubscribe")
            }

            override fun onFailure(p0: IMqttToken?, p1: Throwable?) {
                refreshLogView("unsubscribe error")
                Logger.d("unsubscribe error")
            }
        })
    }

    private fun firstStart() {
        val isBoolean = preferences.getBoolean("isBoolean", false)
        if (!isBoolean) {
//            Start app for the first time
            editor.apply {
                putBoolean("isBoolean", true)
                putString("broker", "www.songyun.work")
                putBoolean("cleanSession", true)
                putString("clientId", "AndroidClientPub")
                putString("message", "Message from MQTTv3 android client")
                putInt("port", 1883)
                putString("pubTopic", "pic")
                putInt("qos", 1)
                putString("subTopic", "sub")
                putString("password", null)
                putString("userName", null)

                commit()
            }
        }
    }

    private fun getParamete(): Data {
        return Data(
            _broker = preferences.getString("broker", null).toString(),
            _qos = preferences.getInt("qos", 0),
            _port = preferences.getInt("port", 0),
            _clientId = preferences.getString("clientId", null).toString(),
            _subTopic = preferences.getString("subTopic", null).toString(),
            _pubTopic = preferences.getString("pubTopic", null).toString(),
            _password = preferences.getString("password", null).toString(),
            _userName = preferences.getString("userName", null).toString(),
            _message = preferences.getString("message", null).toString(),
            _cleanSession = preferences.getBoolean("cleanSession", false)
        )
    }

    private fun saveParamete(data: Data): Data {
        data.apply {
            broker = edit_broker.text.toString()
            cleanSession = swith_cleanSession.isChecked
            clientId = edit_clientId.text.toString()
            message = edit_message.text.toString()
            port = edit_port.text.toString().toInt()
            pubTopic = edit_pubTopic.text.toString()
            qos = edit_qos.text.toString().toInt()
            subTopic = edit_subTopic.text.toString()
            password = edit_password.text.toString()
            userName = edit_userName.text.toString()
        }

        editor.apply {
            putString("broker", data.broker)
            putBoolean("cleanSession", data.cleanSession)
            putString("clientId", data.clientId)
            putString("message", data.message)
            putInt("port", data.port)
            putString("pubTopic", data.pubTopic)
            putInt("qos", data.qos)
            putString("subTopic", data.subTopic)
            putString("password", data.password)
            putString("userName", data.userName)

            commit()
        }
        return data
    }

    private fun initView(data: Data) {
        edit_broker.setText(data.broker)
        edit_qos.setText(data.qos.toString())
        edit_port.setText(data.port.toString())
        edit_clientId.setText(data.clientId)
        edit_subTopic.setText(data.subTopic)
        edit_pubTopic.setText(data.pubTopic)
        edit_password.setText(data.password)
        edit_userName.setText(data.userName)
        edit_message.setText(data.message)
        swith_cleanSession.isChecked = data.cleanSession
    }

    private fun initConnect(data: Data) {
        mqttAndroidClient =
            MqttAndroidClient(applicationContext, "tcp://" + data.broker, data.clientId).apply {
                setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String) {
                        if (reconnect) {
                            refreshLogView("Reconnected to : $serverURI")
                            Logger.d("Reconnected to : $serverURI")
                            // Because Clean Session is true, we need to re-subscribe

                        } else {
                            imageView_connect.visibility = View.INVISIBLE
                            image_run.visibility = View.VISIBLE
                            image_submit.visibility = View.VISIBLE
                            imageView_disconnect.visibility = View.VISIBLE
                            refreshLogView("Connected to: $serverURI")
                            Logger.d("Connected to: $serverURI")
                        }
                    }

                    override fun connectionLost(cause: Throwable) {
                        refreshLogView("The Connection was lost.")
                        Logger.e("The Connection was lost.")
                    }

                    @Throws(Exception::class)
                    override fun messageArrived(topic: String, message: MqttMessage) {
                        refreshLogView("Incoming message: ${message.payload}")
                        Logger.d("Incoming message: ${message.payload}")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken) {
                        refreshLogView("deliveryComplete")
                    }
                })
            }
        val mqttConnectOptions = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = data.cleanSession
        }

        try {
            mqttAndroidClient.connect(mqttConnectOptions, object : IMqttActionListener {
                override fun onSuccess(p0: IMqttToken?) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions().apply {
                        isBufferEnabled = true
                        bufferSize = 100
                        isPersistBuffer = false
                        isDeleteOldestMessages = false
                    }
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)

                }

                override fun onFailure(p0: IMqttToken?, p1: Throwable?) {
                    refreshLogView("Failed to connect to: ${data.broker}")
                    Logger.e("Failed to connect to: $data")
                }

            })
        } catch (ex: MqttException) {
            ex.printStackTrace()
        }
    }

    private fun subscribeToTopic(data: Data) {
        try {
            mqttAndroidClient.subscribe(
                data.subTopic,
                data.qos,
                null,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        refreshLogView("Subscribed!")
                        Logger.d("Subscribed!")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        refreshLogView("Failed to subscribe")
                        Logger.e("Failed to subscribe")
                    }
                })

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(
                data.subTopic,
                0
            ) { topic, message -> // message Arrived!
                refreshLogView("Message: $topic : ${String(message.payload)}")
                Logger.d("Message: $topic : ${String(message.payload)}")
            }
        } catch (ex: MqttException) {
            refreshLogView("Exception whilst subscribing")
            Logger.e("Exception whilst subscribing")
            ex.printStackTrace()
        }
    }

    private fun publishMessage(data: Data) {
        try {
            val message = MqttMessage()
            message.payload = data.message.toByteArray()
            mqttAndroidClient.publish(data.pubTopic, message)
            refreshLogView("Message Published")
            Logger.d("Message Published")
            if (!mqttAndroidClient.isConnected) {
                refreshLogView("${mqttAndroidClient.bufferedMessageCount} messages in buffer.")
                Logger.d("${mqttAndroidClient.bufferedMessageCount} messages in buffer.")
            }
        } catch (e: MqttException) {
            refreshLogView("Error Publishing: ${e.message}")
            Logger.d("Error Publishing: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun refreshLogView(msg: String) {
        runOnUiThread {
            textView_show.append(msg + "\n")
            val offset = textView_show.lineCount * textView_show.lineHeight
            if (offset > textView_show.height) {
                textView_show.scrollTo(
                    0,
                    offset - textView_show.height + textView_show.lineHeight * 2
                )
            }
        }
    }
}