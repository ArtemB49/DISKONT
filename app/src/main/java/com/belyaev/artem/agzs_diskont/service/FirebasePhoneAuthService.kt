package com.belyaev.artem.agzs_diskont.service

import android.app.Activity
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Artem on 14.02.2018.
 */
class FirebasePhoneAuthService(val context: Activity, val view: View) {

    var mAuth: FirebaseAuth? = null
    var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    var mCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    var mVerificationId: String? = null
    val TAG = "PhoneAuthActivity"
    private lateinit var mPhoneNumber: String



    init {
        mAuth = FirebaseAuth.getInstance()
        mCallback = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential?) {
                Log.d(TAG, "onVerificationCompleted: $credential")
                singInWithPhoneAuthCredential(credential, { })
            }

            override fun onVerificationFailed(exception: FirebaseException?) {

                Log.w(TAG, "onVerificationFailed:", exception)
                if (exception is FirebaseAuthInvalidCredentialsException){
                    Snackbar.make(view, "Invalid Phone Number", Snackbar.LENGTH_LONG).show()
                    //phoneET?.error = "Invalid Phone Number"
                } else if (exception is FirebaseTooManyRequestsException){
                    Snackbar.make(view, "Request Error", Snackbar.LENGTH_LONG).show()
                    //phoneET?.error = "Request Error"
                } else if (exception is FirebaseException){
                    Snackbar.make(view, "Произошла ошибка", Snackbar.LENGTH_LONG).show()

                }
            }

            override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                Log.d(TAG, "onCodeSent: $verificationId")
                mVerificationId = verificationId
                mResendToken = token
            }
        }
    }

    fun singInWithPhoneAuthCredential(credential: PhoneAuthCredential?, completion: (Boolean)->Unit) {
        mAuth?.signInWithCredential(credential!!)?.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "singInWithPhoneAuthCredential: successful")
                completion(true)
            } else {
                Log.d(TAG, "singInWithPhoneAuthCredential: failure")

                if (it.exception is FirebaseAuthInvalidCredentialsException){
                    Log.d(TAG, "Invalid code was entered")
                }
            }
        }
    }

    fun startPhoneNumberVerification(phoneNumber: String){
        mPhoneNumber = phoneNumber
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                context,
                mCallback!!
        )
        Log.d(TAG, "startPhoneNumberVerification")
    }

    fun verifyPhoneNumberWithCode(code: String, completion: (Boolean)->Unit){
        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
        singInWithPhoneAuthCredential(credential, {
            completion(it)
        })
    }




}