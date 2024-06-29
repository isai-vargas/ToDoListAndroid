package com.example.milistadetareas;

/**
 * Clase que representa un ítem de tarea con una descripción y una ruta opcional de imagen.
 */
public class Item {
    private String task;         // Descripción de la tarea
    private String imagePath;    // Ruta de la imagen asociada a la tarea

    /**
     * Constructor para inicializar un nuevo ítem de tarea con descripción y ruta de imagen.
     *
     * @param task      Descripción de la tarea
     * @param imagePath Ruta de la imagen de la tarea
     */
    public Item(String task, String imagePath) {
        this.task = task;
        this.imagePath = imagePath;
    }

    /**
     * Método para obtener la descripción de la tarea.
     *
     * @return Descripción de la tarea
     */
    public String getTask() {
        return task;
    }

    /**
     * Método para establecer la descripción de la tarea.
     *
     * @param task Nueva descripción de la tarea
     */
    public void setTask(String task) {
        this.task = task;
    }

    /**
     * Método para obtener la ruta de la imagen asociada a la tarea.
     *
     * @return Ruta de la imagen de la tarea
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Método para establecer la ruta de la imagen asociada a la tarea.
     *
     * @param imagePath Nueva ruta de la imagen de la tarea
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
