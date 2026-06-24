package com.example.goodfood.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.goodfood.CarritoManager;
import com.example.goodfood.R;
import com.example.goodfood.MainActivity;
import com.example.goodfood.models.Plato;
import java.util.List;
import java.util.Locale;

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
        Context context = holder.itemView.getContext();

        holder.tvNombre.setText(plato.getNombre());
        holder.tvDescripcion.setText(plato.getDescripcion());
        holder.tvPrecio.setText(String.format(Locale.US, "$%.2f", plato.getPrecio()));
        holder.tvRating.setText(plato.getRating());
        holder.tvTagTipo.setText(plato.getTipo());
        holder.tvTagTiempo.setText(plato.getTiempo());

        // Carga eficiente de la imagen con Glide
        Glide.with(context)
                .load(plato.getUrlImagen())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.imgPlato);

        // Logica del carrito integrada con Diálogo dinámico
        holder.btnAgregar.setOnClickListener(v -> {
            // Inflar la vista del diálogo personalizado
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_cantidad_plato, null);

            TextView tvCantidad = dialogView.findViewById(R.id.tvCantidad);
            ImageView btnMenos = dialogView.findViewById(R.id.btnMenos);
            ImageView btnMas = dialogView.findViewById(R.id.btnMas);
            Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmarAgregar);

            // Armar y crear el AlertDialog flotante
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .create();

            // Usamos un array de un elemento para mutar la cantidad dentro de los clics lambda
            final int[] cantidadContador = {1};
            tvCantidad.setText(String.valueOf(cantidadContador[0]));

            // Botón restar porción
            btnMenos.setOnClickListener(v1 -> {
                if (cantidadContador[0] > 1) {
                    cantidadContador[0]--;
                    tvCantidad.setText(String.valueOf(cantidadContador[0]));
                }
            });

            // Botón sumar porción
            btnMas.setOnClickListener(v2 -> {
                cantidadContador[0]++;
                tvCantidad.setText(String.valueOf(cantidadContador[0]));
            });

            // Confirmar y meter al CarritoManager global
            btnConfirmar.setOnClickListener(v3 -> {
                dialog.dismiss();

                // pasar el nombre/referencia única
                String platoId = plato.getId() != null ? plato.getId() : plato.getNombre();

                // Sumamos la selección al manager en memoria
                CarritoManager.getInstance().agregarProducto(platoId, cantidadContador[0]);

                Toast.makeText(context, cantidadContador[0] + " x " + plato.getNombre() + " agregado/s", Toast.LENGTH_SHORT).show();

                // Forzar a la MainActivity a actualizar el circulito indicador rojo (0) en vivo
                if (context instanceof MainActivity) {
                    ((MainActivity) context).actualizarInterfazCarrito();
                }
            });

            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return listaPlatos.size();
    }

    public static class PlatoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDescripcion, tvPrecio, tvRating, tvTagTipo, tvTagTiempo;
        ImageView imgPlato, btnAgregar;

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