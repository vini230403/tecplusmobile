package com.example.tecplusmobile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var chamadoAdapter: ChamadoAdapter
    private var chamadosUsuario = mutableListOf<String>()
    private lateinit var listViewChamados: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        listViewChamados = findViewById(R.id.listViewChamados)

        chamadoAdapter = ChamadoAdapter(this, chamadosUsuario) { position ->
            mostrarDialogEditarChamado(position, chamadoAdapter)
        }
        listViewChamados.adapter = chamadoAdapter

        findViewById<LinearLayout>(R.id.layoutAbrirChamado).setOnClickListener {
            showNewChamadoDialog()
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
        val chamadosSalvar = prefs.getStringSet("$email-chamados", emptySet()) ?: emptySet()
        chamadosUsuario.clear()
        chamadosUsuario.addAll(chamadosSalvar)
        chamadoAdapter.notifyDataSetChanged()
    }

    private fun salvarChamados() {
        val email = getEmailUsuarioLogado()
        val prefs = getSharedPreferences("TecPlusPrefs", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putStringSet("$email-chamados", chamadosUsuario.toSet())
        editor.apply()
    }

    private fun abrirDialogMeusChamados() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_meus_chamados, null)
        val listView = dialogView.findViewById<ListView>(R.id.listViewMeusChamados)

        lateinit var dialogAdapter: ChamadoAdapter

        dialogAdapter = ChamadoAdapter(this, chamadosUsuario) { position ->
            mostrarDialogEditarChamado(position, dialogAdapter)
        }
        listView.adapter = dialogAdapter

        val btnFechar = dialogView.findViewById<Button>(R.id.buttonFecharMeusChamados)

        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        btnFechar.setOnClickListener {
            dialog.dismiss()
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            AlertDialog.Builder(this)
                .setTitle("Excluir Chamado")
                .setMessage("Tem certeza que deseja excluir este chamado?")
                .setIcon(R.drawable.ic_close_24)
                .setPositiveButton("Excluir") { _, _ ->
                    chamadosUsuario.removeAt(position)
                    dialogAdapter.notifyDataSetChanged()
                    salvarChamados()
                    showCustomToast("Chamado removido", true)
                    if (chamadosUsuario.isEmpty()) {
                        dialog.dismiss()
                        showCustomToast("Nenhum chamado aberto", false)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }

        dialog.show()
    }

    private fun mostrarDialogEditarChamado(position: Int, adapter: ChamadoAdapter) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_chamado, null)
        val editDescricao = dialogView.findViewById<EditText>(R.id.editDescricaoEditar)

        val chamadoCompleto = chamadosUsuario[position]
        val statusIndex = chamadoCompleto.lastIndexOf("(Em Análise)")
        val descricaoOriginal = if (statusIndex != -1) {
            chamadoCompleto.substring(0, statusIndex).trim()
        } else {
            chamadoCompleto
        }

        editDescricao.setText(descricaoOriginal)

        val dialogEditar = AlertDialog.Builder(this)
            .setTitle("Editar Chamado")
            .setView(dialogView)
            .setIcon(R.drawable.ic_check_24)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialogEditar.setOnShowListener {
            val btnSalvar = dialogEditar.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSalvar.setOnClickListener {
                val textoEditado = editDescricao.text.toString().trim()
                if (textoEditado.isNotEmpty()) {
                    chamadosUsuario[position] = "$textoEditado (Em Análise)"
                    adapter.notifyDataSetChanged()
                    salvarChamados()
                    showCustomToast("Chamado atualizado", true)
                    dialogEditar.dismiss()
                } else {
                    showCustomToast("Descrição não pode ser vazia", false)
                }
            }
        }

        dialogEditar.show()
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
        return prefs.getString("ultimoEmail", "(desconhecido)") ?: "(desconhecido)"
    }

    private fun showNewChamadoDialog() {
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
            .setIcon(R.drawable.ic_check_24)
            .create()

        btnEnviar.setOnClickListener {
            val problemaSelecionado = spinner.selectedItem.toString()
            val descricao = editDescricao.text.toString().trim()
            val chamadoTexto = if (descricao.isEmpty())
                "$problemaSelecionado (Em Análise)"
            else
                "$problemaSelecionado - $descricao (Em Análise)"
            chamadosUsuario.add(chamadoTexto)
            salvarChamados()
            chamadoAdapter.notifyDataSetChanged()
            showCustomToast("Chamado enviado", true)
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    public fun showCustomToast(mensagem: String, isSuccess: Boolean = true) {
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
