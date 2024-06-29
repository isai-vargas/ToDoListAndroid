package com.example.milistadetareas;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Actividad principal que gestiona la lista de tareas.
 */
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_CODE = 100;

    private MyAdapter myAdapter;                    // Adaptador para la lista de tareas
    private List<Item> itemList;                    // Lista de tareas
    private SharedPreferences sharedPreferences;    // Preferencias compartidas para almacenar la lista de tareas
    private Gson gson;                              // Instancia de Gson para serializar/deserializar objetos
    private String currentPhotoPath;                 // Ruta de la foto capturada actualmente
    private EditText editTextTask;                   // Referencia al EditText para actualizar la tarea

    private static final String SHARED_PREFS_KEY = "shared_prefs";
    private static final String TASK_LIST_KEY = "task_list";

    // Lanzador de resultados para captura de fotos
    private ActivityResultLauncher<Intent> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización del lanzador de resultados para captura de fotos
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (currentPhotoPath != null) {
                            // Actualización de la tarea en proceso de creación con la nueva foto
                            editTextTask.setTag(currentPhotoPath);
                            Toast.makeText(this, "Foto capturada y agregada con éxito", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Error al capturar la foto", Toast.LENGTH_LONG).show();
                    }
                });

        sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        gson = new Gson();
        itemList = loadTasks(); // Cargar la lista de tareas desde SharedPreferences

        // Configuración del RecyclerView y el adaptador
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new MyAdapter(itemList, this); // Pasar referencia de MainActivity al adaptador
        recyclerView.setAdapter(myAdapter);

        // Configuración del botón para agregar tarea
        Button buttonAddTask = findViewById(R.id.button_add_task);
        buttonAddTask.setOnClickListener(v -> showAddTaskDialog());

        // Solicitar permisos si no están concedidos
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_PERMISSIONS_CODE);
        }
    }

    /**
     * Verifica si todos los permisos necesarios están concedidos.
     *
     * @return true si todos los permisos están concedidos, false de lo contrario
     */
    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Manejo de la respuesta de solicitud de permisos
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (allPermissionsGranted()) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permisos no concedidos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Método para iniciar la captura de una foto.
     */
    public void capturePhoto() {
        // Verificar si todos los permisos están concedidos
        if (!allPermissionsGranted()) {
            Toast.makeText(this, "Permisos de cámara y almacenamiento necesarios", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_PERMISSIONS_CODE);
            return;
        }

        // Intent para capturar una imagen con la cámara
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile(); // Crear un archivo para almacenar la imagen capturada
            } catch (IOException ex) {
                Log.e("MainActivity", "Error al crear el archivo de imagen", ex);
                Toast.makeText(this, "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.milistadetareas.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                currentPhotoPath = photoFile.getAbsolutePath();
                takePictureLauncher.launch(takePictureIntent); // Iniciar la actividad de captura de fotos
            } else {
                Toast.makeText(this, "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No se pudo encontrar una aplicación para capturar fotos", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Método para crear un archivo de imagen en el almacenamiento externo.
     *
     * @return El archivo creado para la imagen
     * @throws IOException Si ocurre un error al crear el archivo
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Método para mostrar el diálogo de agregar tarea.
     */
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        editTextTask = dialogView.findViewById(R.id.edit_text_task);
        Button buttonAddPhoto = dialogView.findViewById(R.id.button_add_photo);
        buttonAddPhoto.setOnClickListener(v -> capturePhoto()); // Capturar foto al hacer clic en el botón

        builder.setTitle(R.string.add_task)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String task = editTextTask.getText().toString().trim();
                    String photoPath = (String) editTextTask.getTag(); // Obtener la ruta de la foto asociada

                    if (!task.isEmpty()) {
                        // Crear un nuevo objeto Item con la tarea y la ruta de la imagen
                        Item newItem = new Item(task, photoPath != null ? photoPath : ""); // Asegurarse de no pasar null
                        myAdapter.addItem(newItem); // Agregar nueva tarea al adaptador
                        saveTasks(); // Guardar la lista actualizada en SharedPreferences

                        // Notificar al adaptador que se ha agregado un nuevo elemento
                        myAdapter.notifyItemInserted(itemList.size() - 1);

                        Toast.makeText(MainActivity.this, R.string.task_added, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "La tarea no puede estar vacía", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Método para mostrar el diálogo de edición de tarea.
     *
     * @param item     Objeto Item que se va a editar
     * @param position Posición del elemento en la lista
     */
    public void showEditTaskDialog(Item item, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText editTextTask = dialogView.findViewById(R.id.edit_text_task);
        editTextTask.setText(item.getTask()); // Establecer texto inicial con la tarea existente
        editTextTask.setTag(item.getImagePath()); // Establecer la ruta de la imagen asociada

        Button buttonAddPhoto = dialogView.findViewById(R.id.button_add_photo);
        buttonAddPhoto.setOnClickListener(v -> capturePhoto()); // Capturar foto al hacer clic en el botón

        builder.setTitle(R.string.edit_task)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String task = editTextTask.getText().toString().trim();
                    String photoPath = (String) editTextTask.getTag();

                    if (!task.isEmpty()) {
                        item.setTask(task); // Actualizar la tarea en el objeto Item
                        item.setImagePath(photoPath != null ? photoPath : ""); // Asegurarse de no pasar null
                        myAdapter.updateItem(position, item); // Actualizar la tarea en la lista del adaptador
                        saveTasks(); // Guardar la lista actualizada en SharedPreferences

                        Toast.makeText(MainActivity.this, R.string.task_updated, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "La tarea no puede estar vacía", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Método para cargar la lista de tareas desde SharedPreferences.
     *
     * @return Lista de objetos Item cargada desde SharedPreferences
     */
    private List<Item> loadTasks() {
        String json = sharedPreferences.getString(TASK_LIST_KEY, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Item>>() {}.getType();
            return gson.fromJson(json, type); // Deserializar la lista desde JSON
        } else {
            return new ArrayList<>(); // Devolver una lista vacía si no hay datos guardados
        }
    }

    /**
     * Método para guardar la lista de tareas en SharedPreferences.
     */
    public void saveTasks() {
        String json = gson.toJson(itemList); // Serializar la lista a JSON
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TASK_LIST_KEY, json);
        editor.apply(); // Aplicar los cambios en SharedPreferences
    }
}
