package com.example.tecplusmobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editEmail: EditText
    private lateinit var editPass: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var buttonLogin: Button
    private lateinit var textCadastrar: TextView

    private lateinit var checkboxRememberLogin: CheckBox


    private lateinit var sharedPreferences: android.content.SharedPreferences
    private val PREF_NAME = "TecPlusPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        editEmail = findViewById(R.id.editTextEmail)
        editPass = findViewById(R.id.editTextPassword)
        ivTogglePassword = findViewById(R.id.ivToggleSenhaAtual)
        buttonLogin = findViewById(R.id.buttonLogin)
        textCadastrar = findViewById(R.id.textCadastrar)
        checkboxRememberLogin = findViewById(R.id.checkboxRememberLogin)

        val isRemembered = sharedPreferences.getBoolean("loginSalvo", false)
        checkboxRememberLogin.isChecked = isRemembered

        if (isRemembered) {
            val savedEmail = sharedPreferences.getString("emailSalvo", "")
            editEmail.setText(savedEmail)
        } else {
            editEmail.setText("")
            editPass.setText("")
        }



        ivTogglePassword.setOnClickListener {
            if (editPass.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_eye_on)
            } else {
                editPass.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            editPass.setSelection(editPass.text.length)
        }

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
            if (senhaSalva == null) {
                showCustomToast("Usuário não cadastrado", false)
            } else if (senhaSalva == pass) {
                val editor = sharedPreferences.edit()
                editor.putString("ultimoEmail", email)
                if (checkboxRememberLogin.isChecked) {
                    editor.putBoolean("loginSalvo", true)
                    editor.putString("emailSalvo", email)
                } else {
                    editor.putBoolean("loginSalvo", false)
                    editor.remove("emailSalvo")
                }
                editor.apply()
                showCustomToast("Login bem-sucedido", true)
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            } else {
                showCustomToast("Senha incorreta", false)
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
        val ivToggleNovaSenha = dialogView.findViewById<ImageView>(R.id.ivToggleNovaSenha)
        val ivToggleConfirmarSenha = dialogView.findViewById<ImageView>(R.id.ivToggleConfirmarSenha)
        val btnCadastrar = dialogView.findViewById<Button>(R.id.buttonCadastrarUsuario)
        val btnCancelar = dialogView.findViewById<Button>(R.id.buttonCancelarCadastro)
        val editConfirmarSenha = dialogView.findViewById<EditText>(R.id.editConfirmarSenha)

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        ivToggleNovaSenha.setOnClickListener {
            if (editNovaSenha.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editNovaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggleNovaSenha.setImageResource(R.drawable.ic_eye_on)
            } else {
                editNovaSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggleNovaSenha.setImageResource(R.drawable.ic_eye_off)
            }
            editNovaSenha.setSelection(editNovaSenha.text.length)
        }

        // 👁 Mostrar/ocultar CONFIRMAR SENHA
        ivToggleConfirmarSenha.setOnClickListener {
            if (editConfirmarSenha.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggleConfirmarSenha.setImageResource(R.drawable.ic_eye_on)
            } else {
                editConfirmarSenha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggleConfirmarSenha.setImageResource(R.drawable.ic_eye_off)
            }
            editConfirmarSenha.setSelection(editConfirmarSenha.text.length)
        }

        btnCadastrar.setOnClickListener {
            val novoEmail = editNovoEmail.text.toString().trim()
            val novaSenha = editNovaSenha.text.toString()

            if (!isValidEmail(novoEmail)) {
                showCustomToast("Email inválido", false)
                return@setOnClickListener
            }

            if (!isSenhaSegura(novaSenha)) {
                showCustomToast("A senha precisa ter ao menos 8 caracteres, incluir número e caractere especial.", false)
                return@setOnClickListener
            }

            val confirmarSenha = editConfirmarSenha.text.toString()

            if (novaSenha != confirmarSenha) {
                showCustomToast("As senhas não são iguais", false)
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

    // Regras de senha: mínimo 8, pelo menos um número e um caractere especial
    fun isSenhaSegura(senha: String): Boolean {
        val temNumero = senha.any { it.isDigit() }
        val temEspecial = senha.any { "!@#\$%^&*()_+-=[]{},.<>?|\\/".contains(it) }
        return senha.length >= 8 && temNumero && temEspecial
    }

    // Toast customizado para feedback
    fun showCustomToast(mensagem: String, isSuccess: Boolean = true) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.toast_custom, null)
        val textToast = layout.findViewById<TextView>(R.id.textToast)
        val iconToast = layout.findViewById<ImageView>(R.id.iconToast)

        textToast.text = mensagem
        iconToast.setImageResource(
            if (isSuccess) R.drawable.ic_check_24 else R.drawable.ic_close_24
        )
        textToast.setTextColor(android.graphics.Color.parseColor("#000000"))

        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}
