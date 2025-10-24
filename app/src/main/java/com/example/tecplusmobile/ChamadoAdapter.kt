package com.example.tecplusmobile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView

class ChamadoAdapter(
    private val context: Context,
    private val chamados: MutableList<String>,
    private val onEditarClick: (position: Int) -> Unit,
    private val onExcluirClick: (position: Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = chamados.size
    override fun getItem(position: Int): Any = chamados[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_chamado_edit, parent, false)

        val textChamado = view.findViewById<TextView>(R.id.textChamado)
        val btnEditar = view.findViewById<ImageButton>(R.id.buttonEditChamado)
        val btnExcluir = view.findViewById<ImageButton>(R.id.buttonRemoveChamado)

        textChamado.text = chamados[position]

        btnEditar.setOnClickListener {
            onEditarClick(position)
        }

        btnExcluir.setOnClickListener {
            onExcluirClick(position)
        }

        return view
    }
}
