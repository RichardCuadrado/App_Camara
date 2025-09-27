package com.example.myapplication02.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication02.databinding.ItemFotoBinding
import com.example.myapplication02.model.FotoEntity
import coil.load

class FotoAdapter(private var items: List<FotoEntity>) :
    RecyclerView.Adapter<FotoAdapter.VH>() {

    inner class VH(private val binding: ItemFotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(f: FotoEntity) {
            // Mostrar coordenadas (si existen)
            binding.tvCoords.text =
                "Lat: ${f.latitude ?: "N/A"}\nLon: ${f.longitude ?: "N/A"}"

            // Mostrar fecha y hora
            binding.tvFechaHora.text = f.fechaHora

            // Mostrar imagen usando Coil
            binding.imgFoto.load(Uri.parse(f.uri)) {
                crossfade(true)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemFotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    fun update(newItems: List<FotoEntity>) {
        items = newItems
        notifyDataSetChanged()
    }
}
