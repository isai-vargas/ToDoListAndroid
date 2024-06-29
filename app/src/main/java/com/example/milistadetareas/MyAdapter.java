package com.example.milistadetareas;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

/**
 * Adaptador para la lista de tareas en un RecyclerView.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private final List<Item> itemList;      // Lista de elementos (tareas)
    private final Context context;          // Contexto de la aplicación
    private final MainActivity mainActivity;// Actividad principal para la interacción

    /**
     * Constructor del adaptador.
     *
     * @param itemList Lista de elementos (tareas)
     * @param context  Contexto de la aplicación
     */
    public MyAdapter(List<Item> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
        this.mainActivity = (MainActivity) context; // Obtener la instancia de MainActivity
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el diseño de cada elemento de la lista
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);
        return new ViewHolder(view); // Devolver una nueva instancia de ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);

        // Configurar la descripción de la tarea
        holder.taskText.setText(item.getTask());

        // Cargar la imagen si existe una ruta válida
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            File imgFile = new File(item.getImagePath());
            if (imgFile.exists()) {
                holder.taskImage.setImageURI(Uri.fromFile(imgFile));
                holder.taskImage.setVisibility(View.VISIBLE); // Mostrar ImageView si hay imagen válida
            } else {
                holder.taskImage.setVisibility(View.GONE); // Ocultar ImageView si no hay imagen válida
            }
        } else {
            holder.taskImage.setVisibility(View.GONE); // Ocultar ImageView si no hay ruta de imagen
        }

        // Configurar el botón eliminar tarea
        holder.buttonDeleteTask.setOnClickListener(v -> {
            removeItem(position); // Eliminar el elemento de la lista
            mainActivity.saveTasks(); // Guardar la lista actualizada en MainActivity
        });

        // Configurar el botón editar tarea
        holder.buttonEditTask.setOnClickListener(v -> mainActivity.showEditTaskDialog(item, position));
    }

    @Override
    public int getItemCount() {
        return itemList.size(); // Devolver el tamaño de la lista de elementos
    }

    /**
     * Método para añadir un nuevo elemento a la lista.
     *
     * @param item Elemento a añadir
     */
    public void addItem(Item item) {
        itemList.add(item); // Agregar elemento a la lista
        notifyItemInserted(itemList.size() - 1); // Notificar al RecyclerView de la inserción
    }

    /**
     * Método para actualizar un elemento en la lista.
     *
     * @param position Posición del elemento a actualizar
     * @param item     Nuevo elemento
     */
    public void updateItem(int position, Item item) {
        itemList.set(position, item); // Actualizar elemento en la lista
        notifyItemChanged(position); // Notificar al RecyclerView del cambio en esa posición
    }

    /**
     * Método para eliminar un elemento de la lista.
     *
     * @param position Posición del elemento a eliminar
     */
    public void removeItem(int position) {
        itemList.remove(position); // Eliminar elemento de la lista
        notifyItemRemoved(position); // Notificar al RecyclerView de la eliminación en esa posición
        notifyItemRangeChanged(position, itemList.size()); // Notificar al RecyclerView del cambio en el rango de elementos
        Toast.makeText(context, R.string.task_deleted, Toast.LENGTH_SHORT).show(); // Mostrar mensaje de eliminación
    }

    /**
     * Clase ViewHolder que contiene las vistas de cada elemento de la lista.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView taskText;        // TextView para la descripción de la tarea
        public ImageView taskImage;      // ImageView para la imagen de la tarea
        public Button buttonDeleteTask;  // Botón para eliminar la tarea
        public Button buttonEditTask;    // Botón para editar la tarea

        /**
         * Constructor de ViewHolder.
         *
         * @param itemView Vista de cada elemento de la lista
         */
        public ViewHolder(View itemView) {
            super(itemView);
            taskText = itemView.findViewById(R.id.task_text);        // Asignar TextView de la descripción de la tarea
            taskImage = itemView.findViewById(R.id.task_image);      // Asignar ImageView de la imagen de la tarea
            buttonDeleteTask = itemView.findViewById(R.id.button_delete_task);  // Asignar botón de eliminar tarea
            buttonEditTask = itemView.findViewById(R.id.button_edit_task);      // Asignar botón de editar tarea
        }
    }
}
