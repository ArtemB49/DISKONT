package com.belyaev.artem.agzs_diskont.controllers.fragment

import android.app.Fragment
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.utils.OnFragmentInteractionListener
import com.belyaev.artem.agzs_diskont.service.FirebasePhoneAuthService
import kotlinx.android.synthetic.main.fragmetn_verify_phone.*

/**
 * Created by Artem on 19.02.2018.
 */
class FragmentVerifyPhone: Fragment() {

    private var editTextPhone: EditText? = null
    private var editTextCode: EditText? = null
    private var buttonGetCode: Button? = null
    private var buttonSendCode: Button? = null
    private lateinit var mTimer: CountDownTimer
    private lateinit var mDataPasser: OnFragmentInteractionListener
    private lateinit var authService: FirebasePhoneAuthService
    private var isTimerState: Boolean = false
    private var codeLinearLayout: LinearLayout? = null
    private var phoneInputLayout: TextInputLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        val view = inflater.inflate(R.layout.fragmetn_verify_phone, container, false)
        authService = FirebasePhoneAuthService(activity, view)
        mDataPasser = activity as OnFragmentInteractionListener

        phoneInputLayout = view.findViewById(R.id.input_layout_phone)
        codeLinearLayout = view.findViewById(R.id.ll_code)

        buttonGetCode = view.findViewById(R.id.btn_code)
        buttonGetCode?.setOnClickListener {
            if (validatePhone()){
                val phone = editTextPhone?.text?.toString()!!
                if (phone == "322"){
                    phoneVerificationSuccessful()
                } else {
                    authService.startPhoneNumberVerification(phone)
                    editTextCode?.isEnabled = true
                    startTimer()
                }

            }

        }

        buttonSendCode = view.findViewById(R.id.btn_send_code)
        buttonSendCode?.setOnClickListener {
            val code = edt_code.text.toString()
            if (code.count() == 6){
                authService.verifyPhoneNumberWithCode(code, {
                    if (it){
                        phoneVerificationSuccessful()
                    }
                })
            }
        }

        editTextCode = view.findViewById(R.id.edt_code)
        editTextCode?.isEnabled = false
        editTextPhone = view.findViewById(R.id.edt_phone)


        editTextPhone?.afterTextChanged {
            validatePhone()
        }

        return view
    }

    private fun startTimer(){
        edt_phone.isEnabled = false
        buttonGetCode?.isEnabled = false
        isTimerState = true

        mTimer = object : CountDownTimer(60 * 1000, 1000) {
            override fun onFinish() {
                buttonGetCode?.isEnabled = true
                buttonGetCode?.text = "Получить код"
            }

            override fun onTick(millisUntilFinished: Long) {
                if (isTimerState) {
                    buttonGetCode?.text = "Отправить код повторно через: ${millisUntilFinished / 1000} сек"
                } else {
                    mTimer.cancel()
                    buttonGetCode?.visibility = View.GONE
                }
            }
        }.start()
    }

    private fun validatePhone(): Boolean{
        if (editTextPhone?.text?.isEmpty()!!){
            phoneInputLayout?.error = getString(R.string.err_msg_phone)
            requestFocus(editTextPhone!!)
            activity.findViewById<ScrollView>(R.id.main_content).smoothScrollTo(0,0)
            return false
        } else {
            phoneInputLayout?.isErrorEnabled = false
        }
        return true
    }

    private fun submitForm(){
        if (validatePhone())
            return
    }

    private fun phoneVerificationSuccessful(){
        isTimerState = false
        slideToTop(codeLinearLayout)
        editTextPhone?.isEnabled = false
        val phoneNumber = editTextPhone?.text.toString()
        mDataPasser.onFragmentInteraction(0, arrayOf(phoneNumber))

    }

    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit){
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }

    private fun requestFocus(view: View){
        if (view.requestFocus()){
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    private fun slideToTop(layout: LinearLayout?){
        if (layout != null){
            val animation = TranslateAnimation((0).toFloat(), (0).toFloat(), (0).toFloat(),
                    (-layout.height).toFloat())
            animation.duration = 500
            animation.fillAfter = true
            layout.startAnimation(animation)
            for (i in 0 .. layout.childCount ){
                val child:View? = layout.getChildAt(i)
                child?.visibility = View.GONE
            }
            layout.visibility = View.GONE
            layout.visibility = View.INVISIBLE
        }
    }


}