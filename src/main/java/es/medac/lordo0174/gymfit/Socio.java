package es.medac.lordo0174.gymfit;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.util.List;

/** Ficha de un socio tal y como se muestra en la tabla. */
public class Socio {

    public static final String ACTIVO = "Activo";
    public static final String CONGELADO = "Congelado";
    public static final String BAJA = "Baja";

    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty apellido1 = new SimpleStringProperty();
    private final StringProperty apellido2 = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> fechaNacimiento = new SimpleObjectProperty<>();
    private final StringProperty sexo = new SimpleStringProperty();
    private final StringProperty provincia = new SimpleStringProperty();
    private final StringProperty cuota = new SimpleStringProperty();
    private final ObjectProperty<List<String>> clases = new SimpleObjectProperty<>(List.of());
    private final StringProperty iban = new SimpleStringProperty("");
    private final StringProperty estado = new SimpleStringProperty(ACTIVO);

    public Socio(String nombre, String apellido1, String apellido2, LocalDate fechaNacimiento,
                 String sexo, String provincia, String cuota, List<String> clases, String iban) {
        actualizar(nombre, apellido1, apellido2, fechaNacimiento, sexo, provincia, cuota, clases, iban);
    }

    public final void actualizar(String nombre, String apellido1, String apellido2, LocalDate fechaNacimiento,
                                 String sexo, String provincia, String cuota, List<String> clases, String iban) {
        this.nombre.set(nombre);
        this.apellido1.set(apellido1);
        this.apellido2.set(apellido2 == null ? "" : apellido2);
        this.fechaNacimiento.set(fechaNacimiento);
        this.sexo.set(sexo);
        this.provincia.set(provincia);
        this.cuota.set(cuota);
        this.clases.set(List.copyOf(clases));
        this.iban.set(iban == null ? "" : iban);
    }

    public String getNombreCompleto() {
        return (getNombre() + " " + getApellido1() + " " + getApellido2()).trim();
    }

    public String getNombre() { return nombre.get(); }
    public String getApellido1() { return apellido1.get(); }
    public String getApellido2() { return apellido2.get(); }
    public LocalDate getFechaNacimiento() { return fechaNacimiento.get(); }
    public String getSexo() { return sexo.get(); }
    public String getProvincia() { return provincia.get(); }
    public String getCuota() { return cuota.get(); }
    public List<String> getClases() { return clases.get(); }
    public String getIban() { return iban.get(); }
    public String getEstado() { return estado.get(); }
    public void setEstado(String nuevoEstado) { estado.set(nuevoEstado); }

    public StringProperty nombreProperty() { return nombre; }
    public StringProperty apellido1Property() { return apellido1; }
    public StringProperty apellido2Property() { return apellido2; }
    public ObjectProperty<LocalDate> fechaNacimientoProperty() { return fechaNacimiento; }
    public StringProperty provinciaProperty() { return provincia; }
    public StringProperty cuotaProperty() { return cuota; }
    public ObjectProperty<List<String>> clasesProperty() { return clases; }
    public StringProperty estadoProperty() { return estado; }
}
