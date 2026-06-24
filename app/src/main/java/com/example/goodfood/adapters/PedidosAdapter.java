package com.example.goodfood.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.goodfood.R;
import com.example.goodfood.models.Plato;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    private List<Map<String, Object>> listaPedidos;
    private List<Plato> todosLosPlatos;
    private boolean esAdmin;

    public PedidosAdapter(List<Map<String, Object>> listaPedidos, List<Plato> todosLosPlatos, boolean esAdmin) {
        this.listaPedidos = listaPedidos;
        this.todosLosPlatos = todosLosPlatos;
        this.esAdmin = esAdmin;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        Map<String, Object> pedido = listaPedidos.get(position);

        String idCompleto = (String) pedido.get("id_documento");
        String idCorto = (idCompleto != null && idCompleto.length() > 6) ? idCompleto.substring(0, 6).toUpperCase() : "XXXXXX";

        // Si es admin, le mostramos el nombre del cliente al lado del ID del pedido
        String nombreCli = (String) pedido.get("nombreCliente");
        if (esAdmin && nombreCli != null) {
            holder.tvId.setText("Pedido #" + idCorto + " - " + nombreCli);
        } else {
            holder.tvId.setText("Pedido #" + idCorto);
        }

        Double total = 0.0;
        if (pedido.get("total") instanceof Long) {
            total = ((Long) pedido.get("total")).doubleValue();
        } else if (pedido.get("total") instanceof Double) {
            total = (Double) pedido.get("total");
        }
        holder.tvTotal.setText(String.format(Locale.US, "$%.2f", total));

        String estado = (String) pedido.get("estado");
        if (estado == null) estado = "pendiente";
        holder.tvEstado.setText(estado.toUpperCase());

        if (estado.equalsIgnoreCase("pendiente")) {
            holder.tvEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E65100")));
        } else if (estado.equalsIgnoreCase("en preparación")) {
            holder.tvEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0288D1")));
        } else if (estado.equalsIgnoreCase("entregado")) {
            holder.tvEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2E7D32")));
        } else if (estado.equalsIgnoreCase("cancelado")) {
            holder.tvEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#757575")));
        }

        holder.tvFecha.setText("Tocá para ver el detalle técnico");

        holder.itemView.setOnClickListener(v -> {
            Map<String, Long> itemsCompra = (Map<String, Long>) pedido.get("items");
            if (itemsCompra != null && !itemsCompra.isEmpty()) {
                android.content.Context ctx = holder.itemView.getContext();
                com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet = new com.google.android.material.bottomsheet.BottomSheetDialog(ctx);

                LinearLayout layoutContenedor = new LinearLayout(ctx);
                layoutContenedor.setOrientation(LinearLayout.VERTICAL);
                layoutContenedor.setPadding(48, 60, 48, 60);
                layoutContenedor.setBackgroundColor(android.graphics.Color.parseColor("#FAFAFA"));

                TextView tvTitulo = new TextView(ctx);
                tvTitulo.setText("Productos del Pedido #" + idCorto);
                tvTitulo.setTextSize(20);
                tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTitulo.setTextColor(android.graphics.Color.parseColor("#0A4D34"));
                tvTitulo.setPadding(0, 10, 0, 20);
                layoutContenedor.addView(tvTitulo);

                View lineaDivisoria = new View(ctx);
                LinearLayout.LayoutParams paramsLinea = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
                paramsLinea.setMargins(0, 0, 0, 30);
                lineaDivisoria.setLayoutParams(paramsLinea);
                lineaDivisoria.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"));
                layoutContenedor.addView(lineaDivisoria);

                RecyclerView rvDetalle = new RecyclerView(ctx);
                rvDetalle.setLayoutManager(new LinearLayoutManager(ctx));
                rvDetalle.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    List<String> ids = new ArrayList<>(itemsCompra.keySet());

                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
                        View f = LayoutInflater.from(p.getContext()).inflate(R.layout.item_carrito, p, false);
                        return new RecyclerView.ViewHolder(f) {};
                    }

                    @Override
                    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
                        String pId = ids.get(pos);
                        long cant = itemsCompra.get(pId);
                        TextView name = h.itemView.findViewById(R.id.tvNombrePlatoCarrito);
                        TextView count = h.itemView.findViewById(R.id.tvCantidadCarrito);
                        TextView sub = h.itemView.findViewById(R.id.tvSubtotalPlato);
                        ImageView img = h.itemView.findViewById(R.id.imgPlatoCarrito);

                        ImageView btnRestar = h.itemView.findViewById(R.id.btnRestarCantidad);
                        ImageView btnEliminar = h.itemView.findViewById(R.id.btnEliminarPlato);
                        if (btnRestar != null) btnRestar.setVisibility(View.GONE);
                        if (btnEliminar != null) btnEliminar.setVisibility(View.GONE);

                        Plato platoEncontrado = null;
                        for (Plato p : todosLosPlatos) {
                            if ((p.getId() != null && p.getId().equals(pId)) || (p.getNombre() != null && p.getNombre().equals(pId))) {
                                platoEncontrado = p;
                                break;
                            }
                        }
                        if (platoEncontrado != null) {
                            name.setText(platoEncontrado.getNombre());
                            count.setText(String.format(Locale.US, "Cantidad: %d x $%.2f", cant, platoEncontrado.getPrecio()));
                            sub.setText(String.format(Locale.US, "$%.2f", platoEncontrado.getPrecio() * cant));
                            Glide.with(h.itemView.getContext()).load(platoEncontrado.getUrlImagen()).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.stat_notify_error).into(img);
                        } else {
                            name.setText("Plato cod: " + pId);
                            count.setText("Cantidad: " + cant);
                            sub.setText("$0.00");
                        }
                    }
                    @Override
                    public int getItemCount() { return ids.size(); }
                });
                layoutContenedor.addView(rvDetalle);

                // LOGICA SI ES ADMIN O CLIENTE
                String estadoActual = (String) pedido.get("estado");
                if (estadoActual == null) estadoActual = "pendiente";

                if (esAdmin) {
                    // PANEL DE ADMINISTRADOR: Crear los 3 botones de gestión de estados
                    TextView tvAdminLabel = new TextView(ctx);
                    tvAdminLabel.setText("PANEL DE CONTROL ADMIN - Cambiar Estado:");
                    tvAdminLabel.setTextSize(13);
                    tvAdminLabel.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvAdminLabel.setPadding(0, 40, 0, 15);
                    layoutContenedor.addView(tvAdminLabel);

                    LinearLayout layoutBotonesAdmin = new LinearLayout(ctx);
                    layoutBotonesAdmin.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams paramsFila = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutBotonesAdmin.setLayoutParams(paramsFila);

                    // Parámetro para estilos de los botones
                    LinearLayout.LayoutParams paramsBotonIndividual = new LinearLayout.LayoutParams(0, 110, 1.0f);
                    paramsBotonIndividual.setMargins(6, 0, 6, 0);

                    // Botón 1: En preparación (Azul)
                    Button btnPrep = new Button(ctx);
                    btnPrep.setLayoutParams(paramsBotonIndividual);
                    btnPrep.setText("Cocina");
                    btnPrep.setTextSize(11);
                    btnPrep.setTextColor(android.graphics.Color.WHITE);
                    btnPrep.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#0288D1")));
                    if(estadoActual.equalsIgnoreCase("en preparación")) btnPrep.setEnabled(false);
                    btnPrep.setOnClickListener(b -> actualizarEstadoFirebase(idCompleto, "en preparación", bottomSheet, ctx));
                    layoutBotonesAdmin.addView(btnPrep);

                    // Botón 2: Entregado (Verde)
                    Button btnEntregado = new Button(ctx);
                    btnEntregado.setLayoutParams(paramsBotonIndividual);
                    btnEntregado.setText("Entregar");
                    btnEntregado.setTextSize(11);
                    btnEntregado.setTextColor(android.graphics.Color.WHITE);
                    btnEntregado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2E7D32")));
                    if(estadoActual.equalsIgnoreCase("entregado")) btnEntregado.setEnabled(false);
                    btnEntregado.setOnClickListener(b -> actualizarEstadoFirebase(idCompleto, "entregado", bottomSheet, ctx));
                    layoutBotonesAdmin.addView(btnEntregado);

                    // Botón 3: Cancelado (Gris)
                    Button btnCancelAdmin = new Button(ctx);
                    btnCancelAdmin.setLayoutParams(paramsBotonIndividual);
                    btnCancelAdmin.setText("Cancelar");
                    btnCancelAdmin.setTextSize(11);
                    btnCancelAdmin.setTextColor(android.graphics.Color.WHITE);
                    btnCancelAdmin.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#757575")));
                    if(estadoActual.equalsIgnoreCase("cancelado")) btnCancelAdmin.setEnabled(false);
                    btnCancelAdmin.setOnClickListener(b -> actualizarEstadoFirebase(idCompleto, "cancelado", bottomSheet, ctx));
                    layoutBotonesAdmin.addView(btnCancelAdmin);

                    layoutContenedor.addView(layoutBotonesAdmin);

                } else {
                    // ROL CLIENTE: Sigue teniendo solo su botón de Cancelar (siempre que esté pendiente)
                    if (estadoActual.equalsIgnoreCase("pendiente")) {
                        Button btnCancelarCliente = new Button(ctx);
                        LinearLayout.LayoutParams paramsBtn = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 130);
                        paramsBtn.setMargins(0, 40, 0, 10);
                        btnCancelarCliente.setLayoutParams(paramsBtn);
                        btnCancelarCliente.setText("Cancelar Pedido");
                        btnCancelarCliente.setTextColor(android.graphics.Color.WHITE);
                        btnCancelarCliente.setTypeface(null, android.graphics.Typeface.BOLD);
                        btnCancelarCliente.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D32F2F")));
                        btnCancelarCliente.setOnClickListener(view -> actualizarEstadoFirebase(idCompleto, "cancelado", bottomSheet, ctx));
                        layoutContenedor.addView(btnCancelarCliente);
                    }
                }

                bottomSheet.setContentView(layoutContenedor);
                bottomSheet.show();
            }
        });
    }

    // Metodo unico auxiliar para actualizar los cambios en la base de datos
    private void actualizarEstadoFirebase(String idDoc, String nuevoEstado, com.google.android.material.bottomsheet.BottomSheetDialog bs, android.content.Context c) {
        FirebaseFirestore.getInstance()
                .collection("pedidos")
                .document(idDoc)
                .update("estado", nuevoEstado)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(c, "Estado actualizado a: " + nuevoEstado.toUpperCase(), Toast.LENGTH_SHORT).show();
                    bs.dismiss();
                    if (c instanceof com.example.goodfood.activities.PedidosActivity) {
                        ((com.example.goodfood.activities.PedidosActivity) c).recreate();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(c, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return listaPedidos.size();
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvEstado, tvFecha, tvTotal;
        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvIdPedido);
            tvEstado = itemView.findViewById(R.id.tvEstadoPedido);
            tvFecha = itemView.findViewById(R.id.tvFechaPedido);
            tvTotal = itemView.findViewById(R.id.tvTotalPedido);
        }
    }
}