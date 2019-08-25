package com.java.lichenhao

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText


class LoginDialog(context: Context) : AlertDialog(context) {
    val content = LayoutInflater.from(getContext()).inflate(R.layout.login_dialog, null)

    init {
        setView(content)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    // handler: (用户名，密码，是否是注册)
    inline fun show(crossinline handler: (String, String, Boolean) -> Unit) {
        val usernameText = content.findViewById<EditText>(R.id.username_text)
        val passwordText = content.findViewById<EditText>(R.id.password_text)
        passwordText.clearFocus()
        usernameText.requestFocus()

        content.findViewById<Button>(R.id.login_button).setOnClickListener {
            handler(usernameText.text.toString(), passwordText.text.toString(), false)
        }
        content.findViewById<Button>(R.id.register_button).setOnClickListener {
            handler(usernameText.text.toString(), passwordText.text.toString(), true)
        }
        super.show()
    }
}