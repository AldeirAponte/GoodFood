package com.example.goodfood.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.example.goodfood.activities.AgregarPlatoActivity;
import com.example.goodfood.models.Plato;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class PlatoAdapter extends RecyclerView.Adapter<PlatoAdapter.PlatoViewHolder> {

    private List<Plato> listaPlatos;
    private String rolUsuario = "cliente";

    // Constructor viejo por si se llama desde otra pantalla
    public PlatoAdapter(List<Plato> listaPlatos) {
        this.listaPlatos = listaPlatos;
    }

    // constructor para el rol del usuario desde MainActivity
    public PlatoAdapter(List<Plato> listaPlatos, String rolUsuario) {
        this.listaPlatos = listaPlatos;
        this.rolUsuario = rolUsuario;
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

        // edicion de los platos (SOLO ADMIN)
        if ("admin".equals(rolUsuario)) {
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Modificar Producto")
                        .setMessage("¿Querés editar los datos de \"" + plato.getNombre() + "\"?")
                        .setPositiveButton("Editar", (dialog, which) -> {
                            // Abrimos AgregarPlatoActivity mandando el objeto a editar
                            Intent intent = new Intent(context, AgregarPlatoActivity.class);
                            intent.putExtra("plato_a_editar", plato);
                            context.startActivity(intent);
                        })
                        .setNegativeButton("Volver", null)
                        .show();
                return true; // Retornamos true para indicar que activamos el evento del clic sostenido
            });
        } else {
            // Desactivamos el clic sostenido para los clientes
            holder.itemView.setOnLongClickListener(null);
        }

        // mostramos solo el boton borrar para el admin
        if ("admin".equals(rolUsuario)) {
            holder.btnEliminarPlato.setVisibility(View.VISIBLE);

            // logica para borrar el plato de Firestore
            holder.btnEliminarPlato.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("¿Eliminar plato?")
                        .setMessage("¿Estás seguro de que querés borrar \"" + plato.getNombre() + "\"? Esta acción no se puede deshacer.")
                        .setPositiveButton("Eliminar", (dialog, which) -> {

                            int currentPos = holder.getAdapterPosition(); // getAdapterPosition por seguridad se usa en hilos
                            if (currentPos == RecyclerView.NO_POSITION) return;

                            if (plato.getId() != null && !plato.getId().isEmpty()) {
                                FirebaseFirestore.getInstance()
                                        .collection("platos")
                                        .document(plato.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(context, "Plato eliminado correctamente", Toast.LENGTH_SHORT).show();
                                            listaPlatos.remove(currentPos);
                                            notifyItemRemoved(currentPos);
                                            notifyItemRangeChanged(currentPos, listaPlatos.size());
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(context, "Error: El plato no tiene un ID válido.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        } else {
            holder.btnEliminarPlato.setVisibility(View.GONE);
        }

        // logica del carrito
        holder.btnAgregar.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_cantidad_plato, null);

            TextView tvCantidad = dialogView.findViewById(R.id.tvCantidad);
            ImageView btnMenos = dialogView.findViewById(R.id.btnMenos);
            ImageView btnMas = dialogView.findViewById(R.id.btnMas);
            Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmarAgregar);

            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .create();

            final int[] cantidadContador = {1};
            tvCantidad.setText(String.valueOf(cantidadContador[0]));

            btnMenos.setOnClickListener(v1 -> {
                if (cantidadContador[0] > 1) {
                    cantidadContador[0]--;
                    tvCantidad.setText(String.valueOf(cantidadContador[0]));
                }
            });

            btnMas.setOnClickListener(v2 -> {
                cantidadContador[0]++;
                tvCantidad.setText(String.valueOf(cantidadContador[0]));
            });

            btnConfirmar.setOnClickListener(v3 -> {
                dialog.dismiss();
                String platoId = plato.getId() != null ? plato.getId() : plato.getNombre();
                CarritoManager.getInstance().agregarProducto(platoId, cantidadContador[0]);
                Toast.makeText(context, cantidadContador[0] + " x " + plato.getNombre() + " agregado/s", Toast.LENGTH_SHORT).show();

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
        View btnEliminarPlato;

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
            btnEliminarPlato = itemView.findViewById(R.id.btnEliminarPlato);
        }
    }
}