package com.belyaev.artem.agzs_diskont.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.belyaev.artem.agzs_diskont.model.*
import com.belyaev.artem.agzs_diskont.utils.AsyOperationType
import com.belyaev.artem.agzs_diskont.utils.ServiceConstants
import com.belyaev.artem.agzs_diskont.utils.TurnoverRange
import io.realm.Realm
import org.json.JSONArray
import org.json.JSONException
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.PropertyInfo
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapPrimitive
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpResponseException
import java.net.ConnectException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread



class AsyWebService : Service() {

    // WEB SETTINGS

    private var SERVICE_LOGIN = ServiceConstants.LOGIN.value
    private var SERVICE_PASSWORD = ServiceConstants.PASSWORD.value
    private var TEST_SERVICE_PASSWORD = ServiceConstants.TEST_PASSWORD.value
    private var SERVICE_URL = ServiceConstants.SERVICE_URL.value
    private var TEST_SERVICE_URL = ServiceConstants.TEST_SERVICE_URL.value
    private val PARAM_RESULT = "result"
    private val BROADCAST_ID = "com.artem.agzs_project"
    private val TURNOVER = "TURNOVER"
    private val OPERATION = "OPERATION"
    private val DOCS = "DOCS"
    private val STATION = "STATION"

    private val mAsyService = AsyService.instance


    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        BackgroundTaskAsync().execute(intent)

        return super.onStartCommand(intent, flags, startId)
    }





    private fun parse(result: String, type: AsyOperationType): JSONArray? {

        var jsonArray: JSONArray? = null

        val array:List<String> = when (type){
            AsyOperationType.STATIONS ->{
                result
                        .substring(11, result.length - 1)
                        .split("\"Элемент\":")
            }
            AsyOperationType.PRIMARY_DOCS -> {
                result
                        .substring(12, result.length - 1)
                        .split("\"Документ\":")
            }
            AsyOperationType.REGISTRATION-> arrayListOf()
            AsyOperationType.TURNOVER-> arrayListOf()
        }



        var jsonArrayString = ""

        for (item in array){
            jsonArrayString += item
        }
        jsonArrayString = "[$jsonArrayString]"

        try {
            jsonArray = JSONArray(jsonArrayString)

        } catch (e: JSONException){
            e.printStackTrace()
        }


        if (jsonArray != null) {
            return jsonArray
        }

        return null

    }

    private fun answerFromServer(result: String,
                                 operationType: AsyOperationType,
                                 turnoverRange: TurnoverRange?){

        val resultIntent = Intent(BROADCAST_ID)


        when(operationType){
            AsyOperationType.REGISTRATION -> {

            }
            AsyOperationType.TURNOVER -> {
                resultIntent
                        .putExtra(OPERATION, operationType.toString())
                        .putExtra(TURNOVER, turnoverRange.toString())
                        .putExtra(PARAM_RESULT, result)

            }
            AsyOperationType.STATIONS -> {
                val jsonArray = parse(result, operationType)
                if (jsonArray != null){
                    resultIntent.putExtra(STATION, 100)
                    mAsyService.createListStation(jsonArray, {

                        mAsyService.updateRealmStation(it)

                    })
                }


            }
            AsyOperationType.PRIMARY_DOCS -> {
                val jsonArray = parse(result, operationType)

                if (jsonArray != null){

                    resultIntent.putExtra(DOCS, jsonArray.toString())

                    mAsyService.createListPurchases(jsonArray, {
                        mAsyService.updateRealmPurchase(it)
                    })
                }



            }
        }

        sendBroadcast(resultIntent)
        Log.d(turnoverRange.toString(), result)

    }


    @SuppressLint("StaticFieldLeak")
    inner class BackgroundTaskAsync: AsyncTask<Intent, Void, String>(){

        private lateinit var operationType: AsyOperationType
        private var turnoverPeriod: TurnoverRange? = null



        override fun onPostExecute(result: String?) {
            if (result != null){
                answerFromServer(result, operationType, turnoverPeriod)
            }

        }

        override fun doInBackground(vararg intents: Intent?): String {

            val intent = intents[0]

            if (intent == null) return "Нет данных"

            // Тип Оборота

            turnoverPeriod = if (intent.getStringExtra(TURNOVER) != null){
                TurnoverRange.valueOf(intent.getStringExtra(TURNOVER))
            } else {
                null
            }

            // Тип Операции
            operationType = AsyOperationType.valueOf(intent.getStringExtra(OPERATION)!!)

            //Заполнение параметров
            val operationParametr = getParametrOperation(operationType, intent)
            val inDataParametr = getInDataOperation(operationType, intent)

            // Параметры для веб сервера
            val soapObject = SoapObject(ServiceConstants.NAMESPACE.value, ServiceConstants.METHOD_NAME.value)
            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER12)
            val httpTrans = if (operationType != AsyOperationType.STATIONS){
                HttpTransportBasicAuthSE(SERVICE_URL, SERVICE_LOGIN, SERVICE_PASSWORD)
            } else {
                HttpTransportBasicAuthSE(SERVICE_URL, SERVICE_LOGIN, SERVICE_PASSWORD)
            }
            val resultKeySP: SoapPrimitive
            val outDataSP: SoapPrimitive

            soapObject.addProperty(operationParametr)
            soapObject.addProperty(inDataParametr)

            envelope.setOutputSoapObject(soapObject)

            httpTrans.debug = true

            try {
                httpTrans.call(ServiceConstants.SOAP_ACTION.value, envelope)
                //val request = httpTrans.requestDump

                val result = envelope.response as Vector<SoapPrimitive>

                resultKeySP = result[0] as SoapPrimitive

                if (resultKeySP.value.toString() == "Ок!"){
                    outDataSP = result[1] as SoapPrimitive
                } else if (resultKeySP.value.toString() != "") {
                    outDataSP = result[1] as SoapPrimitive
                } else {
                    return "Ошибка"
                }

            } catch (httpError: HttpResponseException){
                return  "Ошибка соединения или доступа к серверу"
            } catch (connectionError: ConnectException) {
                return "Нет доступа к сети!"
            } catch (excClassCast: ClassCastException){
                excClassCast.printStackTrace()
                return "0.0"
            }
            catch (e: Exception) {
                e.printStackTrace()
                return "Прочие Ошибки"
            }

            val returnKey = resultKeySP.value.toString()
            val outData = outDataSP.value.toString()

            when(operationType){
                AsyOperationType.REGISTRATION -> {
                    return ""
                }
                AsyOperationType.TURNOVER -> {
                    try {
                        val numFormat = NumberFormat.getInstance(Locale.FRANCE)
                        val number = numFormat.parse(outData)
                        val num = number.toDouble()
                        return num.toString()

                    } catch (nfe: NumberFormatException){
                        return outData
                    }
                }
                AsyOperationType.STATIONS -> {
                    return  outData
                }
                AsyOperationType.PRIMARY_DOCS -> {
                    return outData
                }
            }



        }

        private fun getParametrOperation(type: AsyOperationType, intent: Intent): PropertyInfo?{
            when (type){
                AsyOperationType.REGISTRATION -> {
                    return null
                }
                AsyOperationType.TURNOVER -> {
                    return initProperty(ServiceConstants.PARAMETER_OPERATION.value,
                            "ОборотПоБК")
                }
                AsyOperationType.STATIONS -> {
                    return  initProperty(ServiceConstants.PARAMETER_OPERATION.value,
                            "ПолучитьКодыЗаправок")
                }
                AsyOperationType.PRIMARY_DOCS -> {
                    return initProperty(ServiceConstants.PARAMETER_OPERATION.value,
                            "ПолучитьПервичкуДляДК")
                }
            }
        }

        private fun getInDataOperation(type: AsyOperationType, intent: Intent): PropertyInfo?{
            when (type){
                AsyOperationType.REGISTRATION -> {
                    return null
                }
                AsyOperationType.TURNOVER -> {
                    val inDataValue = createDataValue(intent.getStringExtra("card"),
                            intent.getStringExtra("start"),
                            intent.getStringExtra("end"))
                    return initProperty(ServiceConstants.PARAMETER_INPUT_DATA.value, inDataValue)
                }
                AsyOperationType.STATIONS -> {
                    return  initProperty(ServiceConstants.PARAMETER_INPUT_DATA.value,
                            "")
                }
                AsyOperationType.PRIMARY_DOCS -> {
                    val inDataValue = createDataValue(intent.getStringExtra("card"),
                            intent.getStringExtra("start"),
                            intent.getStringExtra("end"))
                    return initProperty(ServiceConstants.PARAMETER_INPUT_DATA.value,
                            inDataValue)
                }
            }
        }

        private fun initProperty(name: String, value: String): PropertyInfo {
            val propertyInfo = PropertyInfo()
            propertyInfo.namespace = ServiceConstants.NAMESPACE.value
            propertyInfo.name = name
            propertyInfo.value = value
            propertyInfo.type = PropertyInfo.STRING_CLASS

            return propertyInfo
        }

        // Данные на передачу
        private fun createDataValue(card: String, startDate: String, endDate: String): String{
            return "{\"Элемент\":[\"$card\",\"$startDate\",\"$endDate\"]}"
        }

    }



}
