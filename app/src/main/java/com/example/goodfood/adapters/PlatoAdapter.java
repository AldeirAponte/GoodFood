package com.example.goodfood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.goodfood.R;
import com.example.goodfood.models.Plato;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
        holder.tvPrecio.setText(String.format(Locale.US, "$%.2f", plato.getPrecio()));
        holder.tvRating.setText(plato.getRating());
        holder.tvTagTipo.setText(plato.getTipo());
        holder.tvTagTiempo.setText(plato.getTiempo());

        // Carga eficiente de la imagen con Glide usando tu ID original 'imgPlato'
        Glide.with(holder.itemView.getContext())
                .load(plato.getUrlImagen())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.imgPlato);

        // Tu lógica original del carrito usando 'btnAgregar'
        holder.btnAgregar.setOnClickListener(v -> {
            // Lógica del carrito
        });
    }

    @Override
    public int getItemCount() {
        return listaPlatos.size();
    }

    public static class PlatoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDescripcion, tvPrecio, tvRating, tvTagTipo, tvTagTiempo;
        ImageView imgPlato, btnAgregar; // Volvieron a tus nombres estables

        public PlatoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombrePlato);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionPlato);
            tvPrecio = itemView.findViewById(R.id.tvPrecioPlato);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvTagTipo = itemView.findViewById(R.id.tvTagTipo);
            tvTagTiempo = itemView.findViewById(R.id.tvTagTiempo);
            imgPlato = itemView.findViewById(R.id.imgPlato);
            btnAgregar = itemView.findViewById(R.id.btnAgregarCarrito);
        }
    }
}