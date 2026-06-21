package com.example.goodfood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        holder.tvPrecio.setText("$" + plato.getPrecio());

        // Por ahora dejamos la imagen por defecto, luego manejamos URLs con hilos o librerías
        holder.btnAgregar.setOnClickListener(v -> {
            // Acá irá la lógica del carrito
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
}