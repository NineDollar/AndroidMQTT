package org.eclipse.paho.android.androidmqtt

data class Data(
    private var _message: String,
    private var _qos: Int,
    private var _broker: String,
    private var _port: Int,
    private var _clientId: String,
    private var _subTopic: String,
    private var _pubTopic: String,
    private var _cleanSession: Boolean,
    private var _password: String?,
    private var _userName: String?
) {
    var message: String = _message
    var qos: Int = _qos
    var broker: String = _broker
    var port: Int = _port
    var clientId: String = _clientId
    var subTopic: String = _subTopic
    var pubTopic: String = _pubTopic
    var cleanSession: Boolean = _cleanSession
    var password: String? = _password
    var userName: String? = _userName
}