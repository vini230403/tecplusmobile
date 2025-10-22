package com.example.tecplusmobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPass: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textCadastrar: TextView

    private lateinit var sharedPreferences: android.content.SharedPreferences
    private val PREF_NAME = "TecPlusPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        editEmail = findViewById(R.id.editTextEmail)
        editPass = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textCadastrar = findViewById(R.id.textCadastrar)

        editEmail.setText("")
        editPass.setText("")

        buttonLogin.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val pass = editPass.text.toString()

            if (!isValidEmail(email)) {
                showCustomToast("Email inválido", false)
                return@setOnClickListener
            }

            if (pass.isEmpty()) {
                showCustomToast("Senha não pode ser vazia", false)
                return@setOnClickListener
            }

            val senhaSalva = sharedPreferences.getString(email, null)
            if (senhaSalva == pass) {
                val editor = sharedPreferences.edit()
                editor.putString("ultimoEmail", email)
                editor.apply()
                showCustomToast("Login bem-sucedido", true)
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            } else {
                showCustomToast("Usuário não encontrado. Cadastre-se!", false)
            }
        }

        textCadastrar.setOnClickListener {
            abrirDialogCadastro()
        }
    }

    private fun abrirDialogCadastro() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_cadastro_estilizado, null)
        val editNovoEmail = dialogView.findViewById<EditText>(R.id.editNovoEmail)
        val editNovaSenha = dialogView.findViewById<EditText>(R.id.editNovaSenha)
        val btnCadastrar = dialogView.findViewById<Button>(R.id.buttonCadastrarUsuario)
        val btnCancelar = dialogView.findViewById<Button>(R.id.buttonCancelarCadastro)

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        btnCadastrar.setOnClickListener {
            val novoEmail = editNovoEmail.text.toString().trim()
            val novaSenha = editNovaSenha.text.toString()

            if (!isValidEmail(novoEmail)) {
                showCustomToast("Email inválido", false)
                return@setOnClickListener
            }
            if (novaSenha.isEmpty()) {
                showCustomToast("Senha não pode ser vazia", false)
                return@setOnClickListener
            }

            val editor = sharedPreferences.edit()
            editor.putString(novoEmail, novaSenha)
            editor.apply()

            showCustomToast("Usuário cadastrado com sucesso!", true)
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showCustomToast(mensagem: String, isSuccess: Boolean = true) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.toast_custom, null)
        val textToast = layout.findViewById<TextView>(R.id.textToast)
        val iconToast = layout.findViewById<ImageView>(R.id.iconToast)

        textToast.text = mensagem
        iconToast.setImageResource(
            if (isSuccess) R.drawable.ic_check_24 else R.drawable.ic_close_24
        )
        textToast.setTextColor(
            if (isSuccess) android.graphics.Color.parseColor("#000000")
            else android.graphics.Color.parseColor("#000000")
        )

        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}
