package com.example.tecplusmobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var chamadoAdapter: ChamadoAdapter
    private var chamadosUsuario = mutableListOf<String>()
    private lateinit var listViewChamados: ListView
    private var dialogMeusChamados: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        listViewChamados = findViewById(R.id.listViewChamados)

        chamadoAdapter = ChamadoAdapter(
            this,
            chamadosUsuario,
            onEditarClick = { position -> showEditarChamadoDialog(position) },
            onExcluirClick = { position ->
                showConfirmarExclusaoDialog {
                    excluirChamado(position)
                }
            }
        )
        listViewChamados.adapter = chamadoAdapter

        findViewById<LinearLayout>(R.id.layoutAbrirChamado).setOnClickListener {
            showAbreChamadoDialog()
        }

        findViewById<LinearLayout>(R.id.layoutMeusChamados).setOnClickListener {
            if (chamadosUsuario.isEmpty()) {
                showCustomToast("Você ainda não abriu chamados", false)
            } else {
                abrirDialogMeusChamados()
            }
        }

        findViewById<ImageView>(R.id.imgPerfil).setOnClickListener {
            abrirDialogPerfil()
        }

        carregarChamadosSalvos()
    }

    private fun carregarChamadosSalvos() {
        val email = getEmailUsuarioLogado()
        val prefs = getSharedPreferences("TecPlusPrefs", MODE_PRIVATE)
        val chamadosSalvos = prefs.getStringSet("$email-chamados", emptySet()) ?: emptySet()
        chamadosUsuario.clear()
        chamadosUsuario.addAll(chamadosSalvos)
        chamadoAdapter.notifyDataSetChanged()
    }

    private fun salvarChamados() {
        val email = getEmailUsuarioLogado()
        val prefs = getSharedPreferences("TecPlusPrefs", MODE_PRIVATE)
        prefs.edit().putStringSet("$email-chamados", chamadosUsuario.toSet()).apply()
    }

    private fun abrirDialogMeusChamados() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_meus_chamados, null)
        val listView = dialogView.findViewById<ListView>(R.id.listViewMeusChamados)


        lateinit var adapter: ChamadoAdapter

        adapter = ChamadoAdapter(
            this,
            chamadosUsuario,
            onEditarClick = { position -> showEditarChamadoDialog(position) },
            onExcluirClick = { position ->
                showConfirmarExclusaoDialog {
                    chamadosUsuario.removeAt(position)
                    adapter.notifyDataSetChanged()
                    salvarChamados()
                    showCustomToast("Chamado excluído com sucesso", true)
                }
            }
        )
        listView.adapter = adapter

        dialogMeusChamados = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        val btnFechar = dialogView.findViewById<Button>(R.id.buttonFecharMeusChamados)
        btnFechar.setOnClickListener {
            dialogMeusChamados?.dismiss()
        }

        dialogMeusChamados?.show()
    }


    private fun excluirChamado(position: Int) {
        chamadosUsuario.removeAt(position)
        salvarChamados()
        chamadoAdapter.notifyDataSetChanged()
        showCustomToast("Chamado excluído com sucesso", true)
    }

    fun showAbreChamadoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_abre_chamado_estilizado, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerProblemas)
        val editDescricao = dialogView.findViewById<EditText>(R.id.editDescricao)
        val btnEnviar = dialogView.findViewById<Button>(R.id.buttonEnviarChamado)
        val btnCancelar = dialogView.findViewById<Button>(R.id.buttonCancelarAbrirChamado)

        val problemas = listOf(
            "Selecione um problema",
            "Problema na impressora",
            "Wifi não conecta",
            "Tela do computador preta",
            "Outro..."
        )

        val spinnerAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_selected, // layout customizado para o item selecionado
            problemas
        )
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item) // layout para os itens da lista dropdown
        spinner.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        btnEnviar.setOnClickListener {
            val problemaSelecionado = spinner.selectedItem.toString()
            val descricao = editDescricao.text.toString().trim()

            // Verifica se selecionou uma opção válida
            if (spinner.selectedItemPosition == 0) {
                showCustomToast("Por favor, selecione um tipo de problema.", false)
                return@setOnClickListener
            }

            val chamadoTexto = if (descricao.isEmpty())
                "$problemaSelecionado (Em Análise)"
            else
                "$problemaSelecionado - $descricao (Em Análise)"

            chamadosUsuario.add(chamadoTexto)
            showCustomToast("Chamado enviado", true)

            salvarChamados()
            chamadoAdapter.notifyDataSetChanged()
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showEditarChamadoDialog(posicaoEditar: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_chamado, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerEditarChamado)
        val editDescricao = dialogView.findViewById<EditText>(R.id.editDescricaoEditar)
        val btnSalvar = dialogView.findViewById<Button>(R.id.buttonSalvarEdicao)
        val btnCancelar = dialogView.findViewById<Button>(R.id.buttonCancelarEdicao)

        val problemas = listOf(
            "Selecione um problema",
            "Problema na impressora",
            "Wifi não conecta",
            "Tela do computador preta",
            "Outro..."
        )

        val spinnerAdapter = ArrayAdapter(
            this,
            R.layout.spinner_item_selected,   // layout para texto do item selecionado (azul)
            problemas
        )
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item)  // layout para itens do dropdown
        spinner.adapter = spinnerAdapter

        // Definir a seleção do spinner com base no chamado atual
        val chamadoCompleto = chamadosUsuario[posicaoEditar]
        val statusIndex = chamadoCompleto.lastIndexOf("(Em Análise)")
        val textoChamado = if (statusIndex != -1) chamadoCompleto.substring(0, statusIndex).trim() else chamadoCompleto
        val split = textoChamado.split(" - ", limit = 2)
        val opcaoSelecionada = split.getOrNull(0) ?: problemas[0]
        spinner.setSelection(problemas.indexOf(opcaoSelecionada).takeIf { it >= 0 } ?: 0)

        // Colocar descrição antiga no EditText
        val descricaoAntiga = split.getOrNull(1) ?: ""
        editDescricao.setText(descricaoAntiga)

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        btnSalvar.setOnClickListener {
            val problemaSelecionado = spinner.selectedItem.toString()
            val descricao = editDescricao.text.toString().trim()

            if (spinner.selectedItemPosition == 0) {
                showCustomToast("Por favor, selecione um problema válido.", false)
                return@setOnClickListener
            }

            val chamadoTexto = if (descricao.isEmpty())
                "$problemaSelecionado (Em Análise)"
            else
                "$problemaSelecionado - $descricao (Em Análise)"
            chamadosUsuario[posicaoEditar] = chamadoTexto
            salvarChamados()
            chamadoAdapter.notifyDataSetChanged()
            dialog.dismiss()
            dialogMeusChamados?.dismiss()
            showCustomToast("Chamado atualizado com sucesso", true)
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showConfirmarExclusaoDialog(onConfirm: () -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirmar_exclusao, null)
        val btnSim = dialogView.findViewById<Button>(R.id.btnConfirmarExclusao)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarExclusao)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnSim.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun abrirDialogPerfil() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_perfil, null)
        val textEmailDialog = dialogView.findViewById<TextView>(R.id.textEmailDialog)
        val btnSuporte = dialogView.findViewById<Button>(R.id.buttonPerfilSuporte)
        val btnTrocarSenha = dialogView.findViewById<Button>(R.id.buttonPerfilTrocarSenha)
        val btnSair = dialogView.findViewById<Button>(R.id.buttonPerfilSair)
        val btnCloseDialog = dialogView.findViewById<ImageView>(R.id.btnCloseDialog)

        val emailUsuario = getEmailUsuarioLogado()
        textEmailDialog.text = emailUsuario

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        btnCloseDialog.setOnClickListener {
            dialog.dismiss()
        }

        btnSuporte.setOnClickListener {
            showCustomToast("Área de suporte em breve!", false)
        }
        btnTrocarSenha.setOnClickListener {
            abrirDialogTrocarSenha()
        }
        btnSair.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        dialog.show()
    }

    private fun abrirDialogTrocarSenha() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_trocar_senha, null)
        val editSenhaAtual = dialogView.findViewById<EditText>(R.id.editSenhaAtual)
        val editNovaSenha = dialogView.findViewById<EditText>(R.id.editTrocaSenha)
        val editConfirmaSenha = dialogView.findViewById<EditText>(R.id.editTrocaConfirmaSenha)
        val btnSalvar = dialogView.findViewById<Button>(R.id.buttonSalvarSenhaNova)
        val btnCancelar = dialogView.findViewById<Button>(R.id.buttonCancelarTrocaSenha)
        val ivSenhaAtual = dialogView.findViewById<ImageView>(R.id.ivToggleSenhaAtual)
        val ivNovaSenha = dialogView.findViewById<ImageView>(R.id.ivToggleNovaSenha)
        val ivConfirmaSenha = dialogView.findViewById<ImageView>(R.id.ivToggleConfirmarSenha)

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        // Ícones de olho
        ivSenhaAtual.setOnClickListener { alternarVisibilidadeSenha(editSenhaAtual, ivSenhaAtual) }
        ivNovaSenha.setOnClickListener { alternarVisibilidadeSenha(editNovaSenha, ivNovaSenha) }
        ivConfirmaSenha.setOnClickListener { alternarVisibilidadeSenha(editConfirmaSenha, ivConfirmaSenha) }

        btnSalvar.setOnClickListener {
            val senhaAtual = editSenhaAtual.text.toString()
            val novaSenha = editNovaSenha.text.toString()
            val confirmaSenha = editConfirmaSenha.text.toString()

            val prefs = getSharedPreferences("TecPlusPrefs", MODE_PRIVATE)
            val email = prefs.getString("ultimoEmail", "") ?: ""
            val senhaSalva = prefs.getString(email, "")

            if (senhaAtual.isEmpty() || novaSenha.isEmpty() || confirmaSenha.isEmpty()) {
                showCustomToast("Preencha todos os campos", false)
                return@setOnClickListener
            }

            if (senhaAtual != senhaSalva) {
                showCustomToast("Senha atual incorreta", false)
                return@setOnClickListener
            }

            if (!isSenhaSegura(novaSenha)) {
                showCustomToast("A senha precisa ter ao menos 8 caracteres, incluir número e caractere especial.", false)
                return@setOnClickListener
            }

            if (novaSenha != confirmaSenha) {
                showCustomToast("As senhas não são iguais", false)
                return@setOnClickListener
            }

            prefs.edit().putString(email, novaSenha).apply()
            showCustomToast("Senha atualizada com sucesso", true)
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun alternarVisibilidadeSenha(editText: EditText, icon: ImageView) {
        if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            icon.setImageResource(R.drawable.ic_eye_on)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            icon.setImageResource(R.drawable.ic_eye_off)
        }
        editText.setSelection(editText.text.length)
    }

    private fun getEmailUsuarioLogado(): String {
        val prefs = getSharedPreferences("TecPlusPrefs", MODE_PRIVATE)
        return prefs.getString("ultimoEmail", "") ?: ""
    }

    fun isSenhaSegura(senha: String): Boolean {
        val temNumero = senha.any { it.isDigit() }
        val temEspecial = senha.any { "!@#\$%^&*()_+-=[]{},.<>?|\\/".contains(it) }
        return senha.length >= 8 && temNumero && temEspecial
    }

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
