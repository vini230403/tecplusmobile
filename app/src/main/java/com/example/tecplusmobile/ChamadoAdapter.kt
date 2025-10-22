package com.example.tecplusmobile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class ChamadoAdapter(
    private val context: Context,
    private val chamados: MutableList<String>,
    private val onEditClick: (position: Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = chamados.size

    override fun getItem(position: Int): String = chamados[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_chamado_edit, parent, false)

        val textChamado = view.findViewById<TextView>(R.id.textChamado)
        val buttonRemove = view.findViewById<ImageButton>(R.id.buttonRemoveChamado)
        val buttonEdit = view.findViewById<ImageButton>(R.id.buttonEditChamado)

        textChamado.text = getItem(position)

        buttonRemove.setOnClickListener {
            chamados.removeAt(position)
            notifyDataSetChanged()
            if (context is HomeActivity) {
                context.showCustomToast("Chamado removido", true)
            }
        }

        buttonEdit.setOnClickListener {
            onEditClick(position)
        }

        return view
    }
}
