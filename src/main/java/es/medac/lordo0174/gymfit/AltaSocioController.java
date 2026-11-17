package es.medac.lordo0174.gymfit;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigInteger;
import java.text.Collator;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Controlador de la pantalla de alta de socios (alta-socio.fxml).
 * Pautas aplicadas: prevención de errores, mensajes que dicen qué ha pasado y cómo
 * arreglarlo, confirmación y deshacer en las acciones destructivas, y estado siempre visible.
 */
public class AltaSocioController {

    private static final Locale ESPANOL = Locale.of("es", "ES");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter LECTURA_FECHA =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final int EDAD_MINIMA = 14;
    private static final int EDAD_MAXIMA = 110;
    private static final int EDAD_MAXIMA_JOVEN = 25;
    private static final int EDAD_MINIMA_SENIOR = 65;
    private static final String SOLO_LETRAS = "\\p{L}[\\p{L} '\\-]*";

    private static final List<String> PROVINCIAS = List.of(
            "A Coruña", "Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila", "Badajoz",
            "Barcelona", "Bizkaia", "Burgos", "Cáceres", "Cádiz", "Cantabria", "Castellón", "Ceuta",
            "Ciudad Real", "Córdoba", "Cuenca", "Gipuzkoa", "Girona", "Granada", "Guadalajara", "Huelva",
            "Huesca", "Illes Balears", "Jaén", "La Rioja", "Las Palmas", "León", "Lleida", "Lugo",
            "Madrid", "Málaga", "Melilla", "Murcia", "Navarra", "Ourense", "Palencia", "Pontevedra",
            "Salamanca", "Santa Cruz de Tenerife", "Segovia", "Sevilla", "Soria", "Tarragona", "Teruel",
            "Toledo", "Valencia", "Valladolid", "Zamora", "Zaragoza");

    /** Las 34 clases del gimnasio. Las 8 primeras son las más pedidas y tienen casilla propia. */
    private static final List<String> CLASES = List.of(
            "Spinning", "Yoga", "Pilates", "Zumba", "Body Pump", "Cross Training", "Boxeo", "Natación",
            "Aeróbic", "Aquagym", "Bachata", "Body Attack", "Body Balance", "Body Combat", "Cardio Box",
            "Ciclo indoor sénior", "Core", "Danza del vientre", "Defensa personal", "Entrenamiento funcional",
            "Estiramientos", "GAP", "Gimnasia de mantenimiento", "HIIT", "Hipopresivos", "Judo", "Karate",
            "Kickboxing", "Natación infantil", "Power Yoga", "Salsa", "Step", "Tai Chi", "TRX");

    private static final PseudoClass FILA_BAJA = PseudoClass.getPseudoClass("baja");
    private static final PseudoClass FILA_NUEVA = PseudoClass.getPseudoClass("nuevo");

    // ---------- Nodos del FXML ----------
    @FXML private BorderPane raiz;
    @FXML private ToolBar barraHerramientas;
    @FXML private HBox barraEstado;
    @FXML private CheckMenuItem verBarraHerramientas;
    @FXML private CheckMenuItem verBarraEstado;
    @FXML private CheckMenuItem verSociosBaja;
    @FXML private MenuItem menuEditar;
    @FXML private MenuItem menuRenovar;
    @FXML private MenuItem menuCongelar;
    @FXML private MenuItem menuHistorial;
    @FXML private MenuItem menuBaja;
    @FXML private MenuItem ctxEditar;
    @FXML private MenuItem ctxRenovar;
    @FXML private MenuItem ctxCongelar;
    @FXML private MenuItem ctxHistorial;
    @FXML private MenuItem ctxBaja;
    @FXML private Button botonBajaHerramientas;

    @FXML private Label tituloFormulario;
    @FXML private HBox aviso;
    @FXML private Region iconoAviso;
    @FXML private Label tituloAviso;
    @FXML private Label textoAviso;
    @FXML private TextField campoNombre;
    @FXML private TextField campoApellido1;
    @FXML private TextField campoApellido2;
    @FXML private DatePicker campoFecha;
    @FXML private ToggleGroup grupoSexo;
    @FXML private ComboBox<String> campoProvincia;
    @FXML private TextField campoIban;
    @FXML private ToggleGroup grupoCuota;
    @FXML private CheckBox claseSpinning;
    @FXML private CheckBox claseYoga;
    @FXML private CheckBox clasePilates;
    @FXML private CheckBox claseZumba;
    @FXML private CheckBox claseBodyPump;
    @FXML private CheckBox claseCrossTraining;
    @FXML private CheckBox claseBoxeo;
    @FXML private CheckBox claseNatacion;
    @FXML private Label otrasClases;
    @FXML private Button botonGuardar;

    @FXML private TextField campoBuscar;
    @FXML private Label contadorSocios;
    @FXML private TableView<Socio> tablaSocios;
    @FXML private TableColumn<Socio, String> colNombre;
    @FXML private TableColumn<Socio, LocalDate> colFecha;
    @FXML private TableColumn<Socio, String> colProvincia;
    @FXML private TableColumn<Socio, String> colCuota;
    @FXML private TableColumn<Socio, String> colClases;
    @FXML private TableColumn<Socio, String> colEstado;
    @FXML private Label mensajeEstado;

    // ---------- Estado ----------
    private final ObservableList<Socio> socios = FXCollections.observableArrayList(
            s -> new Observable[]{s.estadoProperty(), s.nombreProperty(), s.apellido1Property(), s.apellido2Property()});
    private FilteredList<Socio> sociosFiltrados;
    private final Map<String, CheckBox> casillasClases = new LinkedHashMap<>();
    private final Set<String> otrasClasesElegidas = new LinkedHashSet<>();
    private final List<Node> camposConError = new ArrayList<>();
    private Node primerCampoConError;
    private Socio socioEnEdicion;
    private Socio socioRecienGuardado;
    private Runnable accionDeshacer;
    private String descripcionDeshacer;
    private String textoFechaNoValido;
    private final StringBuilder busquedaProvincia = new StringBuilder();
    private long ultimaTeclaProvincia;

    private record Problema(Node campo, String texto) { }

    private record EstadoFormulario(String nombre, String apellido1, String apellido2, String fecha, Toggle sexo,
                                    String provincia, Toggle cuota, List<String> clases, String iban) { }

    // =====================================================================
    // Inicialización
    // =====================================================================

    @FXML
    private void initialize() {
        configurarCampos();
        configurarTabla();
        configurarMenusYBarras();
        cargarSociosDeEjemplo();
        ocultarAviso();
        Platform.runLater(campoNombre::requestFocus);
    }

    /** Lo llama GymFitApp: pide confirmación al cerrar si el formulario tiene datos sin guardar. */
    public void configurarVentana(Stage stage) {
        stage.setOnCloseRequest(evento -> {
            if (!confirmarDescartarFormulario("cerrar el programa")) {
                evento.consume();
            }
        });
    }

    private void configurarCampos() {
        List<String> provincias = new ArrayList<>(PROVINCIAS);
        provincias.sort(Collator.getInstance(ESPANOL));
        campoProvincia.getItems().setAll(provincias);
        campoProvincia.addEventHandler(KeyEvent.KEY_TYPED, this::saltarAProvincia);

        campoFecha.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate fecha) {
                if (fecha == null) {
                    return textoFechaNoValido == null ? "" : textoFechaNoValido;
                }
                return FORMATO_FECHA.format(fecha);
            }

            @Override
            public LocalDate fromString(String texto) {
                LocalDate fecha = leerFecha(texto);
                // Si el texto no es una fecha se conserva tal cual para avisar al guardar (no se borra).
                textoFechaNoValido = (fecha == null && texto != null && !texto.isBlank()) ? texto.trim() : null;
                return fecha;
            }
        });
        campoFecha.setDayCellFactory(selector -> new DateCell() {
            @Override
            public void updateItem(LocalDate dia, boolean vacio) {
                super.updateItem(dia, vacio);
                setDisable(vacio || dia.isAfter(LocalDate.now()));
            }
        });

        campoIban.focusedProperty().addListener((obs, teniaFoco, tieneFoco) -> {
            if (!tieneFoco) {
                campoIban.setText(formatearIban(campoIban.getText()));
            }
        });

        for (CheckBox casilla : List.of(claseSpinning, claseYoga, clasePilates, claseZumba,
                claseBodyPump, claseCrossTraining, claseBoxeo, claseNatacion)) {
            casillasClases.put(casilla.getText(), casilla);
        }

        // En cuanto se corrige un campo marcado en rojo, deja de estarlo.
        for (TextField campo : List.of(campoNombre, campoApellido1, campoApellido2, campoIban)) {
            campo.textProperty().addListener((obs, antes, ahora) -> quitarError(campo));
        }
        campoFecha.getEditor().textProperty().addListener((obs, antes, ahora) -> quitarError(campoFecha));
        campoFecha.valueProperty().addListener((obs, antes, ahora) -> quitarError(campoFecha));
        campoProvincia.valueProperty().addListener((obs, antes, ahora) -> quitarError(campoProvincia));
        grupoCuota.selectedToggleProperty().addListener((obs, antes, ahora) ->
                grupoCuota.getToggles().forEach(t -> quitarError((Node) t)));
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(fila -> {
            Socio s = fila.getValue();
            return Bindings.createStringBinding(s::getNombreCompleto,
                    s.nombreProperty(), s.apellido1Property(), s.apellido2Property());
        });
        colFecha.setCellValueFactory(fila -> fila.getValue().fechaNacimientoProperty());
        colFecha.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null ? null : FORMATO_FECHA.format(fecha));
            }
        });
        colProvincia.setCellValueFactory(fila -> fila.getValue().provinciaProperty());
        colCuota.setCellValueFactory(fila -> fila.getValue().cuotaProperty());
        colClases.setCellValueFactory(fila -> Bindings.createStringBinding(
                () -> String.join(", ", fila.getValue().getClases()), fila.getValue().clasesProperty()));
        colEstado.setCellValueFactory(fila -> fila.getValue().estadoProperty());

        tablaSocios.setRowFactory(tabla -> {
            TableRow<Socio> fila = new TableRow<>() {
                @Override
                protected void updateItem(Socio socio, boolean vacia) {
                    super.updateItem(socio, vacia);
                    boolean hay = !vacia && socio != null;
                    pseudoClassStateChanged(FILA_BAJA, hay && Socio.BAJA.equals(socio.getEstado()));
                    pseudoClassStateChanged(FILA_NUEVA, hay && socio == socioRecienGuardado);
                }
            };
            fila.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !fila.isEmpty()) {
                    onEditarFicha();
                }
            });
            return fila;
        });

        sociosFiltrados = new FilteredList<>(socios, s -> true);
        SortedList<Socio> ordenados = new SortedList<>(sociosFiltrados);
        ordenados.comparatorProperty().bind(tablaSocios.comparatorProperty());
        tablaSocios.setItems(ordenados);

        campoBuscar.textProperty().addListener((obs, antes, ahora) -> aplicarFiltro());
        verSociosBaja.selectedProperty().addListener((obs, antes, ahora) -> aplicarFiltro());
        socios.addListener((ListChangeListener<Socio>) cambio -> actualizarContador());
    }

    private void configurarMenusYBarras() {
        barraHerramientas.visibleProperty().bind(verBarraHerramientas.selectedProperty());
        barraHerramientas.managedProperty().bind(barraHerramientas.visibleProperty());
        barraEstado.visibleProperty().bind(verBarraEstado.selectedProperty());
        barraEstado.managedProperty().bind(barraEstado.visibleProperty());

        // Las acciones sobre un socio solo se activan si hay uno seleccionado (prevención de errores).
        BooleanBinding sinSeleccion = tablaSocios.getSelectionModel().selectedItemProperty().isNull();
        for (MenuItem opcion : List.of(menuEditar, menuRenovar, menuCongelar, menuHistorial, menuBaja,
                ctxEditar, ctxRenovar, ctxCongelar, ctxHistorial, ctxBaja)) {
            opcion.disableProperty().bind(sinSeleccion);
        }
        botonBajaHerramientas.disableProperty().bind(sinSeleccion);
    }

    private void cargarSociosDeEjemplo() {
        Socio luis = new Socio("Luis", "Martín", "Ortega", LocalDate.of(2001, 7, 21), "Hombre",
                "Sevilla", "Básica", List.of("Spinning"), "");
        luis.setEstado(Socio.BAJA);
        socios.addAll(
                new Socio("Ana", "Torres", "Vidal", LocalDate.of(1999, 3, 12), "Mujer",
                        "Sevilla", "Premium", List.of("Yoga", "Pilates"), "ES91 2100 0418 4502 0005 1332"),
                luis,
                new Socio("Marta", "Ruiz", "Delgado", LocalDate.of(1988, 11, 30), "Mujer",
                        "Huelva", "Plus", List.of("Zumba"), ""),
                new Socio("José", "Navarro", "Gil", LocalDate.of(2005, 2, 2), "Hombre",
                        "Málaga", "Joven", List.of("Cross Training", "Boxeo"), ""));
    }

    // =====================================================================
    // Formulario: guardar, validar y avisar
    // =====================================================================

    @FXML
    private void onGuardar() {
        limpiarErrores();
        List<Problema> problemas = validar();
        if (!problemas.isEmpty()) {
            mostrarErrores(problemas);
            return;
        }

        String nombre = limpio(campoNombre.getText());
        String apellido1 = limpio(campoApellido1.getText());
        String apellido2 = limpio(campoApellido2.getText());
        LocalDate fecha = leerFecha(campoFecha.getEditor().getText());
        String sexo = grupoSexo.getSelectedToggle() == null ? "" : (String) grupoSexo.getSelectedToggle().getUserData();
        String provincia = campoProvincia.getValue();
        String cuota = (String) grupoCuota.getSelectedToggle().getUserData();
        List<String> clases = clasesElegidas();
        String iban = formatearIban(campoIban.getText());

        Socio socio;
        if (socioEnEdicion == null) {
            socio = new Socio(nombre, apellido1, apellido2, fecha, sexo, provincia, cuota, clases, iban);
            socios.add(socio);
            registrarDeshacer("el alta de " + socio.getNombreCompleto(), () -> socios.remove(socio));
            mostrarAviso(false, "Socio dado de alta",
                    socio.getNombreCompleto() + " ya aparece en la tabla con la cuota " + cuota + ".");
            estado("Alta guardada: " + socio.getNombreCompleto() + ". Ctrl+Z para deshacer.");
        } else {
            socio = socioEnEdicion;
            EstadoFormulario anterior = new EstadoFormulario(socio.getNombre(), socio.getApellido1(),
                    socio.getApellido2(), FORMATO_FECHA.format(socio.getFechaNacimiento()), null,
                    socio.getProvincia(), null, socio.getClases(), socio.getIban());
            String sexoAnterior = socio.getSexo();
            String cuotaAnterior = socio.getCuota();
            socio.actualizar(nombre, apellido1, apellido2, fecha, sexo, provincia, cuota, clases, iban);
            registrarDeshacer("los cambios en la ficha de " + socio.getNombreCompleto(), () ->
                    socio.actualizar(anterior.nombre(), anterior.apellido1(), anterior.apellido2(),
                            leerFecha(anterior.fecha()), sexoAnterior, anterior.provincia(), cuotaAnterior,
                            anterior.clases(), anterior.iban()));
            mostrarAviso(false, "Cambios guardados", "La ficha de " + socio.getNombreCompleto() + " se ha actualizado.");
            estado("Ficha actualizada: " + socio.getNombreCompleto() + ". Ctrl+Z para deshacer.");
        }

        socioRecienGuardado = socio;
        limpiarCampos();
        tablaSocios.refresh();
        tablaSocios.getSelectionModel().select(socio);
        tablaSocios.scrollTo(socio);
        campoNombre.requestFocus();
    }

    private List<Problema> validar() {
        List<Problema> problemas = new ArrayList<>();

        comprobarTexto(problemas, campoNombre, "el nombre", true,
                "Falta el nombre. Escríbelo tal como aparece en el DNI.");
        comprobarTexto(problemas, campoApellido1, "el primer apellido", true,
                "Falta el primer apellido. El segundo apellido va en su propia casilla.");
        comprobarTexto(problemas, campoApellido2, "el segundo apellido", false, null);

        LocalDate fecha = null;
        Integer edad = null;
        String textoFecha = limpio(campoFecha.getEditor().getText());
        if (textoFecha.isEmpty()) {
            problemas.add(new Problema(campoFecha, "Falta la fecha de nacimiento. Escríbela como día/mes/año "
                    + "(por ejemplo, 14/05/2003) o elígela en el calendario."));
        } else {
            fecha = leerFecha(textoFecha);
            if (fecha == null) {
                if (textoFecha.matches("\\d{1,2}[/.-]\\d{1,2}[/.-]\\d{2}")) {
                    problemas.add(new Problema(campoFecha, "En la fecha «" + textoFecha + "» el año tiene 2 cifras. "
                            + "Escríbelo con 4, por ejemplo 14/05/2003."));
                } else {
                    problemas.add(new Problema(campoFecha, "«" + textoFecha + "» no es una fecha válida. "
                            + "Escríbela como día/mes/año, por ejemplo 14/05/2003."));
                }
            } else if (fecha.isAfter(LocalDate.now())) {
                problemas.add(new Problema(campoFecha, "La fecha de nacimiento (" + FORMATO_FECHA.format(fecha)
                        + ") es posterior a hoy. Revisa el año."));
                fecha = null;
            } else {
                edad = Period.between(fecha, LocalDate.now()).getYears();
                if (edad < EDAD_MINIMA) {
                    problemas.add(new Problema(campoFecha, "Con esa fecha el socio tendría " + edad + " años y la edad "
                            + "mínima para darse de alta es " + EDAD_MINIMA + ". Revisa el año."));
                    edad = null;
                } else if (edad > EDAD_MAXIMA) {
                    problemas.add(new Problema(campoFecha, "Con esa fecha el socio tendría " + edad + " años. Revisa el año."));
                    edad = null;
                }
            }
        }

        if (campoProvincia.getValue() == null) {
            problemas.add(new Problema(campoProvincia, "Falta la provincia. Abre la lista y elige una "
                    + "(puedes teclear sus primeras letras para saltar)."));
        }

        Node primeraCuota = (Node) grupoCuota.getToggles().get(0);
        Toggle cuota = grupoCuota.getSelectedToggle();
        if (cuota == null) {
            problemas.add(new Problema(primeraCuota, "Falta el tipo de cuota. Marca una de las cinco opciones."));
        } else if (edad != null) {
            String tipo = (String) cuota.getUserData();
            if ("Joven".equals(tipo) && edad > EDAD_MAXIMA_JOVEN) {
                problemas.add(new Problema((Node) cuota, "La cuota Joven es para socios de " + EDAD_MINIMA + " a "
                        + EDAD_MAXIMA_JOVEN + " años y este socio tiene " + edad + ". Elige otra cuota."));
            } else if ("Senior".equals(tipo) && edad < EDAD_MINIMA_SENIOR) {
                problemas.add(new Problema((Node) cuota, "La cuota Senior es a partir de " + EDAD_MINIMA_SENIOR
                        + " años y este socio tiene " + edad + ". Elige otra cuota."));
            }
        }

        String iban = campoIban.getText() == null ? "" : campoIban.getText().replaceAll("\\s", "").toUpperCase();
        if (!iban.isEmpty()) {
            String siPagaEnRecepcion = " Revísalo, o déjalo vacío si el socio paga en recepción.";
            if (!iban.startsWith("ES")) {
                problemas.add(new Problema(campoIban, "El IBAN debe empezar por ES." + siPagaEnRecepcion));
            } else if (!iban.substring(2).matches("\\d*")) {
                problemas.add(new Problema(campoIban, "Después de ES el IBAN solo lleva números y aquí hay "
                        + "alguna letra o símbolo." + siPagaEnRecepcion));
            } else if (iban.length() != 24) {
                problemas.add(new Problema(campoIban, "El IBAN tiene " + iban.length() + " caracteres y debe tener 24 "
                        + "(ES y 22 números)." + siPagaEnRecepcion));
            } else if (!ibanValido(iban)) {
                problemas.add(new Problema(campoIban, "El IBAN no es correcto: algún número está mal copiado. "
                        + "Compáralo con el documento del banco." + siPagaEnRecepcion.replace(" Revísalo,", "")));
            }
        }

        if (problemas.isEmpty()) {
            String nombreCompleto = normalizar(limpio(campoNombre.getText()) + " " + limpio(campoApellido1.getText())
                    + " " + limpio(campoApellido2.getText()));
            for (Socio s : socios) {
                if (s != socioEnEdicion && normalizar(s.getNombreCompleto()).equals(nombreCompleto)
                        && s.getFechaNacimiento().equals(fecha)) {
                    problemas.add(new Problema(campoNombre, "Ya existe una ficha de «" + s.getNombreCompleto()
                            + "» con la misma fecha de nacimiento. Si es la misma persona, búscala en la tabla "
                            + "y edita su ficha en vez de crear otra."));
                    break;
                }
            }
        }
        return problemas;
    }

    private void comprobarTexto(List<Problema> problemas, TextField campo, String dato, boolean obligatorio,
                                String siFalta) {
        String texto = limpio(campo.getText());
        if (texto.isEmpty()) {
            if (obligatorio) {
                problemas.add(new Problema(campo, siFalta));
            }
        } else if (!texto.matches(SOLO_LETRAS)) {
            problemas.add(new Problema(campo, "En " + dato + " («" + texto + "») hay números o símbolos. "
                    + "Usa solo letras, espacios o guiones."));
        }
    }

    /**
     * Un solo error: el aviso explica qué pasa y cómo arreglarlo.
     * Varios: el aviso dice qué campos son y explica el primero; cada campo en rojo
     * tiene además su explicación en un tooltip. El cursor salta al primer campo.
     */
    private void mostrarErrores(List<Problema> problemas) {
        for (Problema p : problemas) {
            marcarError(p);
        }
        Problema primero = problemas.get(0);
        primerCampoConError = primero.campo();

        if (problemas.size() == 1) {
            mostrarAviso(true, "No se ha podido guardar el socio", primero.texto());
        } else {
            List<String> datos = problemas.stream().map(p -> nombreDe(p.campo())).distinct().toList();
            mostrarAviso(true, "No se ha podido guardar: hay " + problemas.size() + " datos por revisar",
                    "Están en rojo: " + unirConY(datos) + ".\n" + primero.texto());
        }
        estado("No se ha guardado. Corrige los campos en rojo (pasa el ratón por encima para ver qué les pasa).");
        primerCampoConError.requestFocus();
    }

    private void marcarError(Problema problema) {
        Node campo = problema.campo();
        List<Node> nodos = grupoCuota.getToggles().contains(campo)
                ? grupoCuota.getToggles().stream().map(t -> (Node) t).toList()
                : List.of(campo);
        for (Node n : nodos) {
            if (!n.getStyleClass().contains("campo-error")) {
                n.getStyleClass().add("campo-error");
            }
            if (n instanceof Control control) {
                Tooltip explicacion = new Tooltip(problema.texto());
                explicacion.setWrapText(true);
                explicacion.setMaxWidth(360);
                control.setTooltip(explicacion);
            }
            camposConError.add(n);
        }
    }

    private void quitarError(Node campo) {
        campo.getStyleClass().remove("campo-error");
        if (camposConError.remove(campo) && campo instanceof Control control) {
            control.setTooltip(null);
        }
    }

    private void limpiarErrores() {
        for (Node n : List.copyOf(camposConError)) {
            quitarError(n);
        }
        primerCampoConError = null;
        ocultarAviso();
    }

    private String nombreDe(Node campo) {
        if (campo == campoNombre) {
            return "nombre";
        } else if (campo == campoApellido1) {
            return "primer apellido";
        } else if (campo == campoApellido2) {
            return "segundo apellido";
        } else if (campo == campoFecha) {
            return "fecha de nacimiento";
        } else if (campo == campoProvincia) {
            return "provincia";
        } else if (campo == campoIban) {
            return "IBAN";
        }
        return "tipo de cuota";
    }

    private static String unirConY(List<String> partes) {
        if (partes.size() == 1) {
            return partes.get(0);
        }
        return String.join(", ", partes.subList(0, partes.size() - 1)) + " y " + partes.get(partes.size() - 1);
    }

    private void mostrarAviso(boolean esError, String titulo, String texto) {
        aviso.getStyleClass().removeAll("aviso-error", "aviso-ok");
        aviso.getStyleClass().add(esError ? "aviso-error" : "aviso-ok");
        iconoAviso.getStyleClass().removeAll("i-error", "i-ok");
        iconoAviso.getStyleClass().add(esError ? "i-error" : "i-ok");
        tituloAviso.setText(titulo);
        textoAviso.setText(texto);
        aviso.setManaged(true);
        aviso.setVisible(true);
    }

    private void ocultarAviso() {
        aviso.setVisible(false);
        aviso.setManaged(false);
    }

    @FXML
    private void onCerrarAviso() {
        ocultarAviso();
    }

    @FXML
    private void onVaciarFormulario() {
        if (!formularioTieneDatos()) {
            estado("El formulario ya está vacío.");
            return;
        }
        String quien = limpio(campoNombre.getText() + " " + campoApellido1.getText());
        boolean vaciar = confirmarAccionPeligrosa("Vaciar formulario", "¿Vaciar el formulario?",
                "Se borrará lo que has escrito" + (quien.isEmpty() ? "" : " para «" + quien + "»")
                        + ".\nLos socios ya guardados en la tabla no se modifican.",
                "Vaciar formulario", "Seguir editando");
        if (vaciar) {
            EstadoFormulario copia = capturarFormulario();
            Socio editando = socioEnEdicion;
            limpiarFormulario();
            registrarDeshacer("el vaciado del formulario", () -> {
                if (editando != null) {
                    empezarEdicion(editando);
                }
                restaurarFormulario(copia);
            });
            estado("Formulario vaciado. Ctrl+Z para recuperar lo escrito.");
            campoNombre.requestFocus();
        }
    }

    @FXML
    private void onNuevoSocio() {
        if (!confirmarDescartarFormulario("empezar un alta nueva")) {
            return;
        }
        limpiarFormulario();
        estado("Formulario vacío: escribe los datos del nuevo socio.");
        campoNombre.requestFocus();
    }

    @FXML
    private void onMasClases() {
        Map<String, BooleanProperty> marcadas = new LinkedHashMap<>();
        List<String> elegidas = clasesElegidas();
        List<String> ordenadas = new ArrayList<>(CLASES);
        ordenadas.sort(Collator.getInstance(ESPANOL));
        ordenadas.forEach(c -> marcadas.put(c, new SimpleBooleanProperty(elegidas.contains(c))));

        TextField filtro = new TextField();
        filtro.setPromptText("Filtrar, por ejemplo «yoga»");
        FilteredList<String> lista = new FilteredList<>(FXCollections.observableArrayList(marcadas.keySet()));
        filtro.textProperty().addListener((obs, antes, ahora) ->
                lista.setPredicate(c -> normalizar(c).contains(normalizar(ahora))));
        ListView<String> vista = new ListView<>(lista);
        vista.setCellFactory(CheckBoxListCell.forListView(marcadas::get));
        vista.setPrefSize(340, 360);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.initOwner(ventana());
        dialogo.setTitle("Clases del gimnasio");
        dialogo.setHeaderText("Marca todas las clases que quiera hacer el socio");
        ButtonType aplicar = new ButtonType("Aplicar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(aplicar, new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE));
        dialogo.getDialogPane().setContent(new VBox(8, filtro, vista));
        dialogo.getDialogPane().getStylesheets().add(hojaDeEstilos());
        Platform.runLater(filtro::requestFocus);

        dialogo.showAndWait().filter(b -> b == aplicar).ifPresent(b -> {
            otrasClasesElegidas.clear();
            marcadas.forEach((clase, marcada) -> {
                CheckBox casilla = casillasClases.get(clase);
                if (casilla != null) {
                    casilla.setSelected(marcada.get());
                } else if (marcada.get()) {
                    otrasClasesElegidas.add(clase);
                }
            });
            actualizarOtrasClases();
        });
    }

    // =====================================================================
    // Acciones sobre socios de la tabla
    // =====================================================================

    @FXML
    private void onEditarFicha() {
        Socio socio = seleccionado();
        if (socio == null) {
            estado("Selecciona primero un socio en la tabla.");
            return;
        }
        if (socio == socioEnEdicion || !confirmarDescartarFormulario("editar la ficha de " + socio.getNombreCompleto())) {
            return;
        }
        limpiarFormulario();
        empezarEdicion(socio);
        cargarEnFormulario(socio);
        estado("Editando la ficha de " + socio.getNombreCompleto() + ". Pulsa Guardar cambios al terminar.");
        campoNombre.requestFocus();
    }

    @FXML
    private void onRenovarCuota() {
        Socio socio = seleccionado();
        if (socio == null) {
            estado("Selecciona primero un socio en la tabla.");
            return;
        }
        cambiarEstado(socio, Socio.ACTIVO, "la renovación de " + socio.getNombreCompleto(),
                "Cuota de " + socio.getNombreCompleto() + " renovada hasta el "
                        + FORMATO_FECHA.format(LocalDate.now().plusMonths(1)) + ". Ctrl+Z para deshacer.");
    }

    @FXML
    private void onCongelarCuota() {
        Socio socio = seleccionado();
        if (socio == null) {
            estado("Selecciona primero un socio en la tabla.");
            return;
        }
        if (Socio.CONGELADO.equals(socio.getEstado())) {
            estado("La cuota de " + socio.getNombreCompleto() + " ya está congelada. Usa «Renovar cuota» para reactivarla.");
            return;
        }
        cambiarEstado(socio, Socio.CONGELADO, "la congelación de " + socio.getNombreCompleto(),
                "Cuota de " + socio.getNombreCompleto() + " congelada. Ctrl+Z para deshacer.");
    }

    @FXML
    private void onHistorialPagos() {
        Socio socio = seleccionado();
        estado(socio == null ? "Selecciona primero un socio en la tabla."
                : "El historial de pagos de " + socio.getNombreCompleto() + " no está incluido en este prototipo.");
    }

    @FXML
    private void onDarDeBaja() {
        Socio socio = seleccionado();
        if (socio == null) {
            estado("Selecciona primero en la tabla el socio que quieres dar de baja.");
            return;
        }
        if (Socio.BAJA.equals(socio.getEstado())) {
            estado("La ficha de " + socio.getNombreCompleto() + " ya está de baja.");
            return;
        }
        boolean confirmar = confirmarAccionPeligrosa("Dar de baja",
                "¿Dar de baja a " + socio.getNombreCompleto() + "?",
                "Su ficha pasará a «Baja» y no se cobrará la cuota desde el próximo mes.\n"
                        + "No se borra ningún dato: puedes deshacerlo con Ctrl+Z o reactivarla con «Renovar cuota».",
                "Dar de baja", "Cancelar");
        if (confirmar) {
            cambiarEstado(socio, Socio.BAJA, "la baja de " + socio.getNombreCompleto(),
                    "Ficha de " + socio.getNombreCompleto() + " marcada como baja. Ctrl+Z para deshacer.");
        }
    }

    private void cambiarEstado(Socio socio, String nuevoEstado, String descripcion, String mensaje) {
        String anterior = socio.getEstado();
        socio.setEstado(nuevoEstado);
        tablaSocios.refresh();
        registrarDeshacer(descripcion, () -> socio.setEstado(anterior));
        estado(mensaje);
    }

    // =====================================================================
    // Menú Editar, Ver, Ayuda y Archivo
    // =====================================================================

    @FXML
    private void onDeshacer() {
        if (accionDeshacer != null) {
            Runnable accion = accionDeshacer;
            String descripcion = descripcionDeshacer;
            accionDeshacer = null;
            descripcionDeshacer = null;
            accion.run();
            tablaSocios.refresh();
            estado("Deshecho: " + descripcion + ".");
        } else if (enfocado() instanceof TextInputControl campo) {
            campo.undo();
        } else {
            estado("No hay nada que deshacer.");
        }
    }

    @FXML
    private void onCortar() {
        if (enfocado() instanceof TextInputControl campo) {
            campo.cut();
        } else {
            estado("Cortar solo funciona en los campos de texto del formulario.");
        }
    }

    @FXML
    private void onCopiar() {
        if (enfocado() instanceof TextInputControl campo) {
            campo.copy();
            return;
        }
        Socio socio = seleccionado();
        if (socio == null) {
            estado("Selecciona un socio de la tabla o un texto para copiar.");
            return;
        }
        ClipboardContent contenido = new ClipboardContent();
        contenido.putString(String.join("\t", socio.getNombreCompleto(),
                FORMATO_FECHA.format(socio.getFechaNacimiento()), socio.getProvincia(), socio.getCuota(),
                String.join(", ", socio.getClases()), socio.getEstado()));
        Clipboard.getSystemClipboard().setContent(contenido);
        estado("Datos de " + socio.getNombreCompleto() + " copiados al portapapeles.");
    }

    @FXML
    private void onPegar() {
        if (enfocado() instanceof TextInputControl campo) {
            campo.paste();
        } else {
            estado("Haz clic en un campo de texto para pegar en él.");
        }
    }

    @FXML
    private void onBuscar() {
        campoBuscar.requestFocus();
        campoBuscar.selectAll();
    }

    @FXML
    private void onNoDisponible(ActionEvent evento) {
        Object origen = evento.getSource();
        Object nombre = origen instanceof MenuItem m ? m.getUserData()
                : origen instanceof Node n ? n.getUserData() : null;
        estado("«" + (nombre == null ? "Esta opción" : nombre) + "» no está incluida en este prototipo de la pantalla de alta.");
    }

    @FXML
    private void onSalir() {
        if (confirmarDescartarFormulario("salir del programa")) {
            Platform.exit();
        }
    }

    @FXML
    private void onManual() {
        crearAlerta(Alert.AlertType.INFORMATION, "Manual de uso", "Cómo dar de alta a un socio",
                """
                1. Escribe el nombre y los apellidos (cada apellido en su casilla).
                2. Escribe la fecha como día/mes/año (14/05/2003) o elígela en el calendario.
                3. Elige la provincia (teclea sus primeras letras para saltar) y el tipo de cuota.
                4. Marca las clases que quiera hacer. En «Ver las 34 clases…» están todas.
                5. Pulsa Guardar socio, Intro o Ctrl+S.

                Si falta algo o hay un dato mal escrito, un aviso rojo encima del formulario
                te dice cuál es y cómo arreglarlo, y el campo queda marcado en rojo.""").showAndWait();
    }

    @FXML
    private void onAtajos() {
        crearAlerta(Alert.AlertType.INFORMATION, "Atajos de teclado", "Atajos de teclado",
                """
                Ctrl+N    Nuevo socio
                Ctrl+S    Guardar socio (también Intro)
                Ctrl+F    Buscar socio
                Ctrl+Z    Deshacer la última acción
                F2        Editar la ficha seleccionada
                Ctrl+R    Renovar la cuota seleccionada
                Ctrl+H    Historial de pagos
                F1        Manual de uso
                Ctrl+Q    Salir

                «Dar de baja» no tiene atajo a propósito: así no se hace sin querer.""").showAndWait();
    }

    @FXML
    private void onAcercaDe() {
        crearAlerta(Alert.AlertType.INFORMATION, "Acerca de GymFit Manager", "GymFit Manager 3.0",
                "Rediseño de la pantalla de alta de socios.\n"
                        + "Práctica de Desarrollo de Interfaces (Temas 6 y 7).\n"
                        + "Dorin Daniel Radulescu Toea.\n\n"
                        + "Iconos: Material Icons de Google (licencia Apache 2.0).").showAndWait();
    }

    // =====================================================================
    // Utilidades
    // =====================================================================

    private void saltarAProvincia(KeyEvent evento) {
        String tecla = evento.getCharacter();
        if (tecla == null || tecla.isEmpty() || Character.isISOControl(tecla.charAt(0))) {
            return;
        }
        long ahora = System.currentTimeMillis();
        if (ahora - ultimaTeclaProvincia > 1000) {
            busquedaProvincia.setLength(0);
        }
        ultimaTeclaProvincia = ahora;
        busquedaProvincia.append(tecla);
        String buscado = normalizar(busquedaProvincia.toString());
        for (String provincia : campoProvincia.getItems()) {
            if (normalizar(provincia).startsWith(buscado)) {
                campoProvincia.getSelectionModel().select(provincia);
                if (campoProvincia.getSkin() instanceof ComboBoxListViewSkin<?> piel
                        && piel.getPopupContent() instanceof ListView<?> lista) {
                    lista.scrollTo(campoProvincia.getSelectionModel().getSelectedIndex());
                }
                break;
            }
        }
        evento.consume();
    }

    private void aplicarFiltro() {
        String texto = normalizar(campoBuscar.getText());
        boolean conBajas = verSociosBaja.isSelected();
        sociosFiltrados.setPredicate(s -> (conBajas || !Socio.BAJA.equals(s.getEstado()))
                && normalizar(s.getNombreCompleto()).contains(texto));
        actualizarContador();
    }

    private void actualizarContador() {
        long activos = socios.stream().filter(s -> Socio.ACTIVO.equals(s.getEstado())).count();
        if (sociosFiltrados != null && sociosFiltrados.size() != socios.size()) {
            contadorSocios.setText("Mostrando " + sociosFiltrados.size() + " de " + socios.size());
        } else {
            contadorSocios.setText(socios.size() + " socios · " + activos + " activos");
        }
    }

    private void registrarDeshacer(String descripcion, Runnable accion) {
        descripcionDeshacer = descripcion;
        accionDeshacer = accion;
    }

    private void empezarEdicion(Socio socio) {
        socioEnEdicion = socio;
        tituloFormulario.setText("Editar ficha");
        botonGuardar.setText("Guardar cambios");
    }

    private void cargarEnFormulario(Socio s) {
        campoNombre.setText(s.getNombre());
        campoApellido1.setText(s.getApellido1());
        campoApellido2.setText(s.getApellido2());
        textoFechaNoValido = null;
        campoFecha.setValue(s.getFechaNacimiento());
        seleccionarPorDato(grupoSexo, s.getSexo());
        campoProvincia.setValue(s.getProvincia());
        seleccionarPorDato(grupoCuota, s.getCuota());
        marcarClases(s.getClases());
        campoIban.setText(s.getIban());
    }

    private EstadoFormulario capturarFormulario() {
        return new EstadoFormulario(campoNombre.getText(), campoApellido1.getText(), campoApellido2.getText(),
                campoFecha.getEditor().getText(), grupoSexo.getSelectedToggle(), campoProvincia.getValue(),
                grupoCuota.getSelectedToggle(), clasesElegidas(), campoIban.getText());
    }

    private void restaurarFormulario(EstadoFormulario f) {
        campoNombre.setText(f.nombre());
        campoApellido1.setText(f.apellido1());
        campoApellido2.setText(f.apellido2());
        LocalDate fecha = leerFecha(f.fecha());
        textoFechaNoValido = fecha == null && !limpio(f.fecha()).isEmpty() ? f.fecha() : null;
        campoFecha.setValue(fecha);
        campoFecha.getEditor().setText(f.fecha());
        grupoSexo.selectToggle(f.sexo());
        campoProvincia.setValue(f.provincia());
        grupoCuota.selectToggle(f.cuota());
        marcarClases(f.clases());
        campoIban.setText(f.iban());
    }

    /** Vacía los campos y vuelve al modo «Alta de socio». También oculta el aviso. */
    private void limpiarFormulario() {
        limpiarErrores();
        limpiarCampos();
    }

    /** Vacía los campos sin tocar el aviso (para mantener visible el «Socio dado de alta»). */
    private void limpiarCampos() {
        campoNombre.clear();
        campoApellido1.clear();
        campoApellido2.clear();
        textoFechaNoValido = null;
        campoFecha.setValue(null);
        campoFecha.getEditor().clear();
        grupoSexo.selectToggle(null);
        campoProvincia.getSelectionModel().clearSelection();
        campoProvincia.setValue(null);
        grupoCuota.selectToggle(null);
        marcarClases(List.of());
        campoIban.clear();
        socioEnEdicion = null;
        tituloFormulario.setText("Alta de socio");
        botonGuardar.setText("Guardar socio");
    }

    private boolean formularioTieneDatos() {
        return !limpio(campoNombre.getText()).isEmpty() || !limpio(campoApellido1.getText()).isEmpty()
                || !limpio(campoApellido2.getText()).isEmpty() || !limpio(campoFecha.getEditor().getText()).isEmpty()
                || grupoSexo.getSelectedToggle() != null || campoProvincia.getValue() != null
                || grupoCuota.getSelectedToggle() != null || !clasesElegidas().isEmpty()
                || !limpio(campoIban.getText()).isEmpty();
    }

    private boolean confirmarDescartarFormulario(String accion) {
        if (!formularioTieneDatos()) {
            return true;
        }
        return confirmarAccionPeligrosa("Datos sin guardar", "Hay datos sin guardar en el formulario",
                "Si continúas para " + accion + ", se perderá lo que has escrito.",
                "Descartar y continuar", "Seguir editando");
    }

    private void marcarClases(List<String> clases) {
        otrasClasesElegidas.clear();
        casillasClases.forEach((clase, casilla) -> casilla.setSelected(clases.contains(clase)));
        for (String clase : clases) {
            if (!casillasClases.containsKey(clase)) {
                otrasClasesElegidas.add(clase);
            }
        }
        actualizarOtrasClases();
    }

    private List<String> clasesElegidas() {
        return CLASES.stream()
                .filter(c -> casillasClases.containsKey(c) ? casillasClases.get(c).isSelected()
                        : otrasClasesElegidas.contains(c))
                .toList();
    }

    private void actualizarOtrasClases() {
        otrasClases.setText(otrasClasesElegidas.isEmpty() ? ""
                : "También: " + String.join(", ", otrasClasesElegidas));
        otrasClases.setVisible(!otrasClasesElegidas.isEmpty());
        otrasClases.setManaged(!otrasClasesElegidas.isEmpty());
    }

    private static void seleccionarPorDato(ToggleGroup grupo, String dato) {
        grupo.selectToggle(grupo.getToggles().stream()
                .filter(t -> t.getUserData() != null && t.getUserData().equals(dato))
                .findFirst().orElse(null));
    }

    private Socio seleccionado() {
        return tablaSocios.getSelectionModel().getSelectedItem();
    }

    private Node enfocado() {
        return raiz.getScene() == null ? null : raiz.getScene().getFocusOwner();
    }

    private Stage ventana() {
        return (Stage) raiz.getScene().getWindow();
    }

    private String hojaDeEstilos() {
        return AltaSocioController.class.getResource("estilos.css").toExternalForm();
    }

    private void estado(String mensaje) {
        mensajeEstado.setText(LocalTime.now().format(FORMATO_HORA) + " · " + mensaje);
    }

    private Alert crearAlerta(Alert.AlertType tipo, String titulo, String cabecera, String texto,
                              ButtonType... botones) {
        Alert alerta = new Alert(tipo, texto, botones);
        alerta.initOwner(ventana());
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecera);
        alerta.getDialogPane().getStylesheets().add(hojaDeEstilos());
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return alerta;
    }

    /** Confirmación de una acción destructiva: el botón seguro («Cancelar») es el que responde a Intro. */
    private boolean confirmarAccionPeligrosa(String titulo, String cabecera, String texto,
                                             String textoAccion, String textoCancelar) {
        ButtonType accion = new ButtonType(textoAccion, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType(textoCancelar, ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = crearAlerta(Alert.AlertType.WARNING, titulo, cabecera, texto, accion, cancelar);
        Button botonAccion = (Button) alerta.getDialogPane().lookupButton(accion);
        Button botonCancelar = (Button) alerta.getDialogPane().lookupButton(cancelar);
        botonAccion.getStyleClass().add("boton-peligro-relleno");
        botonAccion.setDefaultButton(false);
        botonCancelar.setDefaultButton(true);
        alerta.setOnShown(e -> botonCancelar.requestFocus());
        return alerta.showAndWait().orElse(cancelar) == accion;
    }

    static LocalDate leerFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim().replaceAll("[.\\-\\s]+", "/"), LECTURA_FECHA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    static String formatearIban(String texto) {
        if (texto == null) {
            return "";
        }
        String limpio = texto.replaceAll("\\s", "").toUpperCase();
        return limpio.replaceAll("(.{4})(?!$)", "$1 ");
    }

    static boolean ibanValido(String iban) {
        String reordenado = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeros = new StringBuilder();
        for (char c : reordenado.toCharArray()) {
            numeros.append(Character.isLetter(c) ? String.valueOf(c - 'A' + 10) : String.valueOf(c));
        }
        return new BigInteger(numeros.toString()).mod(BigInteger.valueOf(97)).intValue() == 1;
    }

    private static String limpio(String texto) {
        return texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).trim();
    }
}
