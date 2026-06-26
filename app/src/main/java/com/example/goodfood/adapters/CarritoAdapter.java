package com.example.goodfood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.goodfood.CarritoManager;
import com.example.goodfood.R;
import com.example.goodfood.models.Plato;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

    private List<String> listaIdsElegidos;
    private Map<String, Integer> mapaCantidades;
    private List<Plato> todosLosPlatos;
    private OnCarritoModificadoListener listener;

    // Creamos una interfaz para que CarritoActivity sepa cuándo recalcular el total
    public interface OnCarritoModificadoListener {
        void onCarritoModificado();
    }

    public CarritoAdapter(Map<String, Integer> itemsCarrito, List<Plato> todosLosPlatos, OnCarritoModificadoListener listener) {
        this.mapaCantidades = itemsCarrito;
        this.listaIdsElegidos = new ArrayList<>(itemsCarrito.keySet());
        this.todosLosPlatos = todosLosPlatos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrito, parent, false);
        return new CarritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
        String platoId = listaIdsElegidos.get(position);
        Integer cantidadObj = mapaCantidades.get(platoId);
        int cantidad = (cantidadObj != null) ? cantidadObj : 0;

        Plato platoEncontrado = encontrarPlatoPorId(platoId);

        if (platoEncontrado != null) {
            holder.tvNombre.setText(platoEncontrado.getNombre());
            holder.tvCantidad.setText("Cantidad: " + cantidad);

            double subtotal = platoEncontrado.getPrecio() * cantidad;
            holder.tvSubtotal.setText(String.format(Locale.US, "$%.2f", subtotal));

            Glide.with(holder.itemView.getContext())
                    .load(platoEncontrado.getUrlImagen())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.imgPlato);
        }

        // LOGICA DEL BOTÓN RESTAR (-)
        holder.btnRestar.setOnClickListener(v -> {
            if (cantidad > 1) {
                // Si hay más de uno, restamos 1 en el Manager
                CarritoManager.getInstance().getItems().put(platoId, cantidad - 1);
            } else {
                // Si queda solo 1, al restarle se elimina del carrito por completo
                CarritoManager.getInstance().getItems().remove(platoId);
                listaIdsElegidos.remove(position);
            }
            actualizarInterfaz();
        });

        // LOGICA DEL BOTÓN ELIMINAR (Tachito)
        holder.btnEliminar.setOnClickListener(v -> {
            CarritoManager.getInstance().getItems().remove(platoId);
            listaIdsElegidos.remove(position);
            actualizarInterfaz();
        });
    }

    private void actualizarInterfaz() {
        // si hay algun cambio actualiza lo que se esta viendo
        notifyDataSetChanged();
        if (listener != null) {
            // Le avisa a CarritoActivity que recalcule el total o muestre "Carrito Vacío"
            listener.onCarritoModificado();
        }
    }

    @Override
    public int getItemCount() {
        return listaIdsElegidos.size();
    }

    private Plato encontrarPlatoPorId(String id) {
        for (Plato p : todosLosPlatos) {
            if ((p.getId() != null && p.getId().equals(id)) || (p.getNombre() != null && p.getNombre().equals(id))) {
                return p;
            }
        }
        return null;
    }

    public static class CarritoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCantidad, tvSubtotal;
        ImageView imgPlato;
        ImageView btnRestar, btnEliminar;

        public CarritoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombrePlatoCarrito);
            tvCantidad = itemView.findViewById(R.id.tvCantidadCarrito);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotalPlato);
            imgPlato = itemView.findViewById(R.id.imgPlatoCarrito);
            btnRestar = itemView.findViewById(R.id.btnRestarCantidad);
            btnEliminar = itemView.findViewById(R.id.btnEliminarPlato);
        }
    }
}