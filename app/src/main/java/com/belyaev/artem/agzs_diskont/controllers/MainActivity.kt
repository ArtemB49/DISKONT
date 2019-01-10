package com.belyaev.artem.agzs_diskont.controllers

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.Toast
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.controllers.fragment.FragmentCardScan
import com.belyaev.artem.agzs_diskont.controllers.fragment.FragmentNames
import com.belyaev.artem.agzs_diskont.controllers.fragment.FragmentVerifyPhone
import com.belyaev.artem.agzs_diskont.model.GazStationParcelable
import com.belyaev.artem.agzs_diskont.utils.OnFragmentInteractionListener
import com.belyaev.artem.agzs_diskont.utils.ServiceConstants
import com.belyaev.artem.agzs_diskont.service.HttpTransportBasicAuthSE
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.lang.RandomStringUtils
import org.json.JSONArray
import org.json.JSONException
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.PropertyInfo
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapPrimitive
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpResponseException
import java.net.ConnectException
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), OnFragmentInteractionListener {

    // Fragment

    var fragmentNames: FragmentNames? = null
    var fragmentVerifyPhone: FragmentVerifyPhone? = null
    var fragmentCardScan: FragmentCardScan? = null

    var currentFragment = 0

    // View
    var btnNext: Button? = null

    var jsonArray: JSONArray? = null
    val listGazStation: ArrayList<GazStationParcelable> = ArrayList()

    private var firstName: String? = null
    private var lastName: String? = null
    private var fatherName: String? = null
    private var phoneNumber: String?= null
    private var fullCardNumber: String? = null
    private var cardNumber: String? = null
    private var contentView: ScrollView? = null
    private lateinit var identifier: String
    private var doubleBackToExitPressedOnce = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseAuth.getInstance().signOut()

        initLayout()
        initButtons()



        /*
        Send list gazstaion to activity
        val buttonSpeller: Button = findViewById(R.id.button2)
        buttonSpeller.setOnClickListener{ view ->
            val length = jsonArray?.length()
            for (i in (0..length!!)) {
                try {
                    val itemArray: JSONArray = jsonArray?.getJSONArray(i)!!
                    val station = GazStationParcelable(itemArray)
                    listGazStation.add(i, station)
                } catch (jEx: JSONException) {
                    jEx.printStackTrace()
                }
            }

                val intent = Intent(applicationContext, com.example.artem.agzs_android_project2.controllers.ListActivity::class.java)
                intent.putParcelableArrayListExtra("list", listGazStation)
                startActivity(intent)
        }
        */
    }



    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce){
            finish()
            return
        }

        doubleBackToExitPressedOnce = true

        Snackbar
                .make(contentView!!,
                        "Для выхода нажмите кнопку назад еще раз",
                        Snackbar.LENGTH_SHORT)
                .show()

        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    private fun initLayout(){
        // Root View
        contentView = findViewById(R.id.main_content)

        // Fragments
        fragmentNames = FragmentNames()
        fragmentVerifyPhone = FragmentVerifyPhone()
        fragmentCardScan = FragmentCardScan()

        fragmentManager
                .beginTransaction()
                .add(R.id.frgm_main, fragmentNames!!)
                .commit()

    }

    private fun initButtons(){

        btnNext = findViewById(R.id.btn_next)

        btnNext?.setOnClickListener {

            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction?.setCustomAnimations(R.animator.slide_in_left,  R.animator.slide_out_right)
            btn_next.isEnabled = false
            when (currentFragment){
                0 -> {
                    if (fragmentNames?.validateNames() == true){
                        fragmentTransaction?.replace(R.id.frgm_main, fragmentVerifyPhone!!)
                        currentFragment = 1
                        tv_title.text = "Введите номер телефона"

                    }
                }
                1 -> {
                    identifier = RandomStringUtils.randomAlphanumeric(16)
                    Log.d("ID", identifier)
                    fragmentTransaction?.replace(R.id.frgm_main, fragmentCardScan!!)
                    currentFragment = 2
                    btnNext?.text = "Зарегистрироваться"
                    tv_title.text = "Сканируйте карту"

                }
                2 -> {

                    CallWebService().execute("3")
                }
            }
            fragmentTransaction?.addToBackStack(null)
            fragmentTransaction?.commit()
        }
    }



    override fun onFragmentInteraction(frID: Int, data: Array<String>) {

        Log.d("frag", data[0])

        when (frID){

            0 -> {
                phoneNumber = data[0]
            }
            1 -> {
                fullCardNumber = data[0]
                cardNumber = fullCardNumber?.substring(6, 12)
            }
            2 -> {
                firstName = data[0]
                lastName = data[1]
                fatherName = data[2]
            }
        }
        btn_next.isEnabled = true
    }


    private fun successfulRegistration(){
        val intent = Intent(this, NavigationActivity::class.java)
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .edit()
                .putString("card", fullCardNumber)
                .putString("appKey", identifier)
                .putBoolean("isLogin", true)
                .apply()
        startActivity(intent)
        finish()
    }



    inner class CallWebService: AsyncTask<String, Void, String>(){

        override fun onPostExecute(result: String?) {
            if (result == "Ок!"){
                successfulRegistration()
            }
            Toast.makeText(this@MainActivity, result, Toast.LENGTH_LONG).show()
        }

        override fun doInBackground(vararg params: String?): String {
            val result: Vector<SoapPrimitive>
            val soapPrimitive: SoapPrimitive
            val soapPrimitive2: SoapPrimitive

            val opID = params[0]?.toInt()

            // Сервисные константы

            val SERVICE_URL = ServiceConstants.SERVICE_URL.value
            val SERVICE_LOGIN = ServiceConstants.LOGIN.value
            val SERVICE_PASSWORD = ServiceConstants.PASSWORD.value

            val operations = arrayOf("ОборотПоБК", "Организации", "Объекты", "МАПередатьАвторизацию")

            val soapObject = SoapObject(
                    ServiceConstants.NAMESPACE.value,
                    ServiceConstants.METHOD_NAME.value)

            val pOperation: PropertyInfo
            val pInData: PropertyInfo

            when(opID){
                3 -> {
                    pOperation = initProperty(ServiceConstants.PARAMETER_OPERATION.value,
                            operations[opID])
                    val inDataValue =
                            "{\"Элемент\":[\"$cardNumber\",\"$lastName $firstName $fatherName\",\"$phoneNumber\",\"$identifier\"]}"
                    pInData = initProperty(ServiceConstants.PARAMETER_INPUT_DATA.value,
                            inDataValue)
                }

                else -> {
                    return "error"
                }

            }

            soapObject.addProperty(pOperation)
            soapObject.addProperty(pInData)

            val envelope = SoapSerializationEnvelope(SoapEnvelope.VER12)
            envelope.setOutputSoapObject(soapObject)

            val httpTrans = HttpTransportBasicAuthSE(SERVICE_URL,
                    SERVICE_LOGIN, SERVICE_PASSWORD)

            httpTrans.debug = true

            try {
                httpTrans.call(ServiceConstants.SOAP_ACTION.value, envelope)
                val request = httpTrans.requestDump

                result = envelope.response as Vector<SoapPrimitive>

                soapPrimitive = result[0] as SoapPrimitive
                soapPrimitive2 = result[1] as SoapPrimitive


            } catch (httpError: HttpResponseException){
                return "Ошибка соединения или доступа к серверу"
            } catch (connectionError: ConnectException) {
                return "Нет доступа к сети!"
            } catch (excClassCast: ClassCastException){
                excClassCast.printStackTrace()
                Log.d("ОШИБКА", "Ошибка пребразования XML")
                return "Ошибка пребразования XML"
            }
            catch (e: Exception) {
                e.printStackTrace()
                return "Прочие Ошибки"
            }

            val str = soapPrimitive.value.toString()
            val str2 = soapPrimitive2.value.toString()


            return str

        }

        private fun parse(result: SoapPrimitive): JSONArray? {

            var jsonArray: JSONArray? = null
            var sample = result.toString()
            sample = sample.substring(11, sample.length - 1)
            val array = sample.split("\"Элемент\":")
            var jsonArrayString:String = ""

            for (item in array){
                jsonArrayString += item
            }

            jsonArrayString = "[$jsonArrayString]"

            try {
                jsonArray = JSONArray(jsonArrayString)
                Log.d("", jsonArray.toString())
            } catch (e: JSONException){
                e.printStackTrace()
            }

            return jsonArray

        }

        private fun initProperty(name: String, value: String): PropertyInfo {
            val propertyInfo = PropertyInfo()
            propertyInfo.namespace = ServiceConstants.NAMESPACE.value
            propertyInfo.name = name
            propertyInfo.value = value
            propertyInfo.type = PropertyInfo.STRING_CLASS

            return propertyInfo
        }

    }
}


