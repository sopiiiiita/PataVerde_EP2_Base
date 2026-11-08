package cl.duoc.pataverde.repository;

import cl.duoc.pataverde.model.Gato;
import cl.duoc.pataverde.model.Mascota;
import cl.duoc.pataverde.model.Perro;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MascotaRepository {

    private static final String PERROS_FILE = "perros.json";
    private static final String GATOS_FILE = "gatos.json";
    private final Gson gson;

    public MascotaRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public List<Perro> cargarPerros() throws IOException {
        File file = new File(PERROS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Perro>>() {}.getType();
            List<Perro> perros = gson.fromJson(reader, listType);
            return perros != null ? perros : new ArrayList<>();
        }
    }

    public List<Gato> cargarGatos() throws IOException {
        File file = new File(GATOS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Gato>>() {}.getType();
            List<Gato> gatos = gson.fromJson(reader, listType);
            return gatos != null ? gatos : new ArrayList<>();
        }
    }

    public List<Mascota> cargarTodas() throws IOException {
        List<Mascota> todas = new ArrayList<>();
        todas.addAll(cargarPerros());
        todas.addAll(cargarGatos());
        return todas;
    }

    public void guardarTodas(List<Mascota> mascotas) throws IOException {
        List<Perro> perros = new ArrayList<>();
        List<Gato> gatos = new ArrayList<>();

        for (Mascota m : mascotas) {
            if (m instanceof Perro) {
                perros.add((Perro) m);
            } else if (m instanceof Gato) {
                gatos.add((Gato) m);
            }
        }

        try (FileWriter writer = new FileWriter(PERROS_FILE)) {
            gson.toJson(perros, writer);
        }

        try (FileWriter writer = new FileWriter(GATOS_FILE)) {
            gson.toJson(gatos, writer);
        }
    }
}