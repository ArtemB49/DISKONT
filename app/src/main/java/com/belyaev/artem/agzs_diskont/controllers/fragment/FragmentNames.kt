package com.belyaev.artem.agzs_diskont.controllers.fragment

import android.app.Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.belyaev.artem.agzs_diskont.R
import com.belyaev.artem.agzs_diskont.utils.OnFragmentInteractionListener
import kotlinx.android.synthetic.main.fragment_names.*

/**
 * Created by Artem on 19.02.2018.
 */
class FragmentNames: Fragment() {

    private lateinit var mDataPasser: OnFragmentInteractionListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mDataPasser = activity as OnFragmentInteractionListener
        val view = inflater.inflate(R.layout.fragment_names, container, false)
        val edtFatherName = view.findViewById<EditText>(R.id.edt_father_name)
        edtFatherName.afterTextChanged {
            if (edt_last_name.text.toString() != "" &&
                    edt_father_name.text.toString() != "" &&
                    edt_first_name.text.toString() != ""){
                saveNames()
            }
        }
        return view
    }

    private fun saveNames(){
        val first = edt_first_name.text.toString()
        val last = edt_last_name.text.toString()
        val father = edt_father_name.text.toString()
        mDataPasser.onFragmentInteraction(2, arrayOf(first, last, father))
    }

    fun validateNames(): Boolean{
        var result = true
        if (edt_first_name.text.isEmpty()){
            input_layout_first_name.error = getString(R.string.err_msg_first_name)
            result = false
        } else {
            input_layout_first_name.isErrorEnabled = false
        }

        if (edt_father_name.text.isEmpty()){
            input_layout_father_name.error = getString(R.string.err_msg_father_name)
            result = false
        } else {
            input_layout_father_name.isErrorEnabled = false
        }

        if (edt_last_name.text.isEmpty()){
            input_layout_last_name.error = getString(R.string.err_msg_last_name)
            result = false
        } else {
            input_layout_last_name.isErrorEnabled = false
        }
        return result
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
}