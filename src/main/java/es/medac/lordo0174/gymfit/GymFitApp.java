package es.medac.lordo0174.gymfit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * GymFit Manager 3.0: pantalla de alta de socios rediseñada (DI, Temas 6 y 7).
 * La interfaz está en alta-socio.fxml, que se puede abrir con Scene Builder.
 */
public class GymFitApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(GymFitApp.class.getResource("alta-socio.fxml"));
        Scene scene = new Scene(loader.load());

        AltaSocioController controlador = loader.getController();
        controlador.configurarVentana(stage);

        stage.setTitle("GymFit Manager · Alta de socios");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
