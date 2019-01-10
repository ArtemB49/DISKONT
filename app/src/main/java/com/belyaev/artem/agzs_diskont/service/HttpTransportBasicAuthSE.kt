package com.belyaev.artem.agzs_diskont.service

import org.ksoap2.transport.HttpTransportSE
import org.ksoap2.transport.ServiceConnection
import org.ksoap2.transport.ServiceConnectionSE

/**
 * Created by Artem on 21.11.2017.
 */
class HttpTransportBasicAuthSE(url: String, login: String, password: String):
        HttpTransportSE(url) {

    private val login: String?
    private val password: String?

    init{
        this.url = url
        this.login = login
        this.password = password
    }


    override fun getServiceConnection(): ServiceConnection {
        val midConnection = ServiceConnectionSE(url)
        addBasicAuthentification(midConnection)
        return midConnection
    }

    private fun addBasicAuthentification(midConnection: ServiceConnection){
        if (login != null && password != null){
            val buffer = StringBuffer(login)
            buffer.append(':').append(password)
            val raw: ByteArray = buffer.toString().toByteArray()
            buffer.setLength(0)
            buffer.append("Basic ")
            org.kobjects.base64.Base64.encode(raw, 0, raw.count(), buffer)
            midConnection.setRequestProperty("Authorization", buffer.toString())
        }
    }
}