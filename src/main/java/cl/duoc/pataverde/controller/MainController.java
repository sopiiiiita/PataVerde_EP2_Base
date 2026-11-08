package cl.duoc.pataverde.controller;

import cl.duoc.pataverde.model.Gato;
import cl.duoc.pataverde.model.Mascota;
import cl.duoc.pataverde.model.Perro;
import cl.duoc.pataverde.repository.MascotaRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class MainController {

    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEdad;
    @FXML private TextField txtPeso;

    // Campos Perro
    @FXML private VBox boxPerro;
    @FXML private TextField txtRaza;
    @FXML private CheckBox chkVacunado;
    @FXML private CheckBox chkAdopcion;

    // Campos Gato
    @FXML private VBox boxGato;
    @FXML private CheckBox chkExterior;

    // Tabla y Botones
    @FXML private TableView<Mascota> tblMascotas;
    @FXML private TableColumn<Mascota, String> colTipo;
    @FXML private TableColumn<Mascota, String> colNombre;
    @FXML private TableColumn<Mascota, Integer> colEdad;
    @FXML private TableColumn<Mascota, Double> colPeso;
    @FXML private TableColumn<Mascota, String> colCosto;

    @FXML private Label lblMensaje;

    private final MascotaRepository repository = new MascotaRepository();
    private final ObservableList<Mascota> listaMascotas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarComboTipo();
        configurarTabla();
        cargarDatosGuardados();
    }

    private void configurarComboTipo() {
        cbTipo.getItems().addAll("Seleccione...", "Perro", "Gato");
        cbTipo.getSelectionModel().selectFirst();
        actualizarHabilitacionCampos();

        cbTipo.valueProperty().addListener((obs, oldVal, newVal) -> actualizarHabilitacionCampos());
    }

    private void actualizarHabilitacionCampos() {
        String tipoSeleccionado = cbTipo.getValue();
        boolean esPerro = "Perro".equals(tipoSeleccionado);
        boolean esGato = "Gato".equals(tipoSeleccionado);

        boxPerro.setDisable(!esPerro);
        boxGato.setDisable(!esGato);
    }

    private void configurarTabla() {
        colTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipo()));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        colCosto.setCellValueFactory(data -> {
            double costo = data.getValue().calcularCostoConsulta();
            return new SimpleStringProperty(formatoMoneda.format(costo).replace("CLP", "").trim());
        });

        tblMascotas.setItems(listaMascotas);
    }

    private void cargarDatosGuardados() {
        try {
            listaMascotas.addAll(repository.cargarTodas());
        } catch (IOException e) {
            mostrarMensajeError("Error al cargar los datos guardados.");
        }
    }

    @FXML
    private void handleRegistrar() {
        lblMensaje.setText("");

        String tipo = cbTipo.getValue();
        if (tipo == null || "Seleccione...".equals(tipo)) {
            mostrarMensajeError("Tipo: debe seleccionar un tipo de mascota (Perro o Gato).");
            return;
        }

        String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        if (nombre.isEmpty()) {
            mostrarMensajeError("Nombre: no puede estar vacío ni contener solo espacios.");
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(txtEdad.getText().trim());
            if (edad < 0 || edad > 30) {
                mostrarMensajeError("Edad: debe ser un número entero entre 0 y 30.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("Edad: debe ser un número entero entre 0 y 30.");
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(txtPeso.getText().trim().replace(',', '.'));
            if (peso <= 0) {
                mostrarMensajeError("Peso (kg): debe ser un número mayor que cero.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarMensajeError("Peso (kg): debe ser un número válido mayor que cero.");
            return;
        }

        Mascota nuevaMascota = null;

        if ("Perro".equals(tipo)) {
            String raza = txtRaza.getText() != null ? txtRaza.getText().trim() : "";
            if (raza.isEmpty()) {
                mostrarMensajeError("Raza: no puede estar vacía ni contener solo espacios.");
                return;
            }

            Perro perro = new Perro(nombre, edad, peso, raza, chkVacunado.isSelected());
            if (chkAdopcion.isSelected()) {
                perro.marcarDisponibleParaAdopcion();
            }
            nuevaMascota = perro;
        } else if ("Gato".equals(tipo)) {
            nuevaMascota = new Gato(nombre, edad, peso, chkExterior.isSelected());
        }

        if (nuevaMascota != null) {
            try {
                listaMascotas.add(nuevaMascota);
                repository.guardarTodas(listaMascotas);
                mostrarMensajeExito(nuevaMascota.getNombre() + " (" + nuevaMascota.getTipo() + ") registrado correctamente.");
                limpiarFormulario();
            } catch (IOException e) {
                mostrarMensajeError("Error al guardar la información en los archivos.");
            }
        }
    }

    @FXML
    private void handleEliminar() {
        Mascota seleccionada = tblMascotas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarMensajeError("Debe seleccionar una mascota de la tabla para eliminarla.");
            return;
        }

        try {
            listaMascotas.remove(seleccionada);
            repository.guardarTodas(listaMascotas);
            mostrarMensajeExito(seleccionada.getNombre() + " (" + seleccionada.getTipo() + ") eliminado correctamente.");
        } catch (IOException e) {
            mostrarMensajeError("Error al actualizar los archivos JSON.");
        }
    }

    private void limpiarFormulario() {
        cbTipo.getSelectionModel().selectFirst();
        txtNombre.clear();
        txtEdad.clear();
        txtPeso.clear();
        txtRaza.clear();
        chkVacunado.setSelected(false);
        chkAdopcion.setSelected(false);
        chkExterior.setSelected(false);
    }

    private void mostrarMensajeError(String mensaje) {
        lblMensaje.setStyle("-fx-text-fill: #d9534f; -fx-font-weight: bold;");
        lblMensaje.setText(mensaje);
    }

    private void mostrarMensajeExito(String mensaje) {
        lblMensaje.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        lblMensaje.setText(mensaje);
    }
}