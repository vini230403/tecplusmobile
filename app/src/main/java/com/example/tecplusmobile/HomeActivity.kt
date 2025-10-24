package com.example.tecplusmobile

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent


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

        lateinit var adapter: ChamadoAdapter  // Declara aqui

        adapter = ChamadoAdapter(
            this,
            chamadosUsuario,
            onEditarClick = { position -> showEditarChamadoDialog(position) },
            onExcluirClick = { position ->
                showConfirmarExclusaoDialog {
                    chamadosUsuario.removeAt(position)
                    adapter.notifyDataSetChanged()  // Agora adapter existe
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
            "Problema na impressora",
            "Wifi não conecta",
            "Tela do computador preta",
            "Outro..."
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, problemas)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        btnEnviar.setOnClickListener {
            val problemaSelecionado = spinner.selectedItem.toString()
            val descricao = editDescricao.text.toString().trim()
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
            "Problema na impressora",
            "Wifi não conecta",
            "Tela do computador preta",
            "Outro..."
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, problemas)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        val chamadoCompleto = chamadosUsuario[posicaoEditar]
        val statusIndex = chamadoCompleto.lastIndexOf("(Em Análise)")
        val textoChamado = if (statusIndex != -1) chamadoCompleto.substring(0, statusIndex).trim() else chamadoCompleto
        val split = textoChamado.split(" - ", limit = 2)
        val opcaoSelecionada = split.getOrNull(0) ?: problemas[0]
        val descricaoAntiga = split.getOrNull(1) ?: ""
        spinner.setSelection(problemas.indexOf(opcaoSelecionada).takeIf { it >= 0 } ?: 0)
        editDescricao.setText(descricaoAntiga)

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        btnSalvar.setOnClickListener {
            val problemaSelecionado = spinner.selectedItem.toString()
            val descricao = editDescricao.text.toString().trim()
            val chamadoTexto = if (descricao.isEmpty())
                "$problemaSelecionado (Em Análise)"
            else
                "$problemaSelecionado - $descricao (Em Análise)"
            chamadosUsuario[posicaoEditar] = chamadoTexto
            salvarChamados()
            chamadoAdapter.notifyDataSetChanged()
            dialog.dismiss()
            dialogMeusChamados?.dismiss()  // Fecha o modal "Meus Chamados"
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
            showCustomToast("Funcionalidade em breve!", false)
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

    private fun getEmailUsuarioLogado(): String {
        val prefs = getSharedPreferences("TecPlusPrefs", MODE_PRIVATE)
        return prefs.getString("ultimoEmail", "") ?: ""
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
