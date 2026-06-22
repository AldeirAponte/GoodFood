/*package com.example.goodfood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // 🌟 IMPORTANTE: Asegurate de que este import se agregue
import com.example.goodfood.R;
import com.example.goodfood.models.Plato;
import java.util.List;

public class PlatoAdapter extends RecyclerView.Adapter<PlatoAdapter.PlatoViewHolder> {

    private List<Plato> listaPlatos;

    public PlatoAdapter(List<Plato> listaPlatos) {
        this.listaPlatos = listaPlatos;
    }

    @NonNull
    @Override
    public PlatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plato, parent, false);
        return new PlatoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatoViewHolder holder, int position) {
        Plato plato = listaPlatos.get(position);
        holder.tvNombre.setText(plato.getNombre());
        holder.tvDescripcion.setText(plato.getDescripcion());
        holder.tvPrecio.setText(String.format("$%.2f", plato.getPrecio()));

        // 🌟 CORRECCIÓN DE IMAGEN: Glide descarga de forma eficiente la URL en segundo plano
        Glide.with(holder.itemView.getContext())
                .load(plato.getUrlImagen()) // Extrae la URL de Cloudinary/API guardada en tu objeto Plato
                .placeholder(android.R.drawable.ic_menu_gallery) // Imagen temporal mientras descarga
                .error(android.R.drawable.stat_notify_error)      // Imagen si la URL está rota o no hay internet
                .into(holder.imgPlato);

        // Lógica del carrito
        holder.btnAgregar.setOnClickListener(v -> {
            // Acá irá la lógica del carrito más adelante
        });
    }

    @Override
    public int getItemCount() {
        return listaPlatos.size();
    }

    public static class PlatoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDescripcion, tvPrecio;
        ImageView imgPlato;
        Button btnAgregar;

        public PlatoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombrePlato);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionPlato);
            tvPrecio = itemView.findViewById(R.id.tvPrecioPlato);
            imgPlato = itemView.findViewById(R.id.imgPlato);
            btnAgregar = itemView.findViewById(R.id.btnAgregarCarrito);
        }
    }
}*/