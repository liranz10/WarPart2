package BL;

import static BL.GUIWarGameImpl.eDirection.LEFT;
import static BL.GUIWarGameImpl.eDirection.RIGHT;
import static Entities.War.create;
import static Entities.War.timeSinceGameStartedInSeconds;
import static Entities.WarInformation.eCALLER_FUNCTION.INCREMENT_MISSILES_LAUNCHERS_DESTRUCTED;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import Entities.DestructedLauncher;
import Entities.Destructor;
import Entities.Destructor_;
import Entities.Launcher;
import Entities.Missile;
import Entities.War;
import GameServer.WarListener;
import GameServer.WarServer;
import Interfaces.AnimationParametersInterface;
import Interfaces.AnimationsInterface;
import Interfaces.WarGameInterface;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GUIWarGameImpl extends Application implements WarGameInterface, AnimationsInterface,WarListener {

    public enum eDirection { LEFT, RIGHT }
    @JsonIgnore
    private boolean guiLoadedSuccesfully;
    @JsonIgnore
    private War war;
    @JsonIgnore
	private WarServer warServer;
    
    
    @JsonIgnore
    private Stage stage;
    @FXML
    private TextField missileHit;
    @FXML
    private TextField missileLaunched;
    @FXML
    private TextField missileDestructed;
    @FXML
    private TextField totalDamage;
    @FXML
    private TextField missileLaunchersDestructed;
    @FXML
    private TextField missileMissed;
    @FXML
    private GridPane mainGrid;
    @JsonIgnore
    private final int MAX_NUMBER_OF_ROWS = 8;
    @JsonIgnore
    public int numOfLaunchers = 0;
    @JsonIgnore
    public int numOfLaunchersDestructors = 0;
    @JsonIgnore
    private int numOfMissileDestructors = 0;
    @JsonIgnore
    public AtomicInteger indexOfLauncher = new AtomicInteger(0);
    @JsonIgnore
    public ShowAnimationObserver showAnimationObserver = new ShowAnimationObserver();

    public GUIWarGameImpl() {
        try (Scanner scanner = new Scanner(System.in)) {
            this.war = create(scanner, false);
            war.getWarInformation().addObserver(new StatisticsUpdater());

        }
        warServer = new WarServer();
		Thread warThread = new Thread(warServer);
		warServer.registerListener(this);
		warThread.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/WarGameUI.fxml"));
        loader.setController(this);
        Parent root = loader.load();
        Scene scene = new Scene(root, 690, 625);
        scene.getStylesheets().add("resources/style.css");
        mainGrid.getStyleClass().add("mainGrid");
        primaryStage.setTitle("War Game");
        primaryStage.setScene(scene);

        stage = primaryStage;

        primaryStage.setOnCloseRequest(s -> {
                Platform.exit();
                System.exit(0);
        });

        missileHit.setEditable(false);
        missileLaunched.setEditable(false);
        missileDestructed.setEditable(false);
        totalDamage.setEditable(false);
        missileLaunchersDestructed.setEditable(false);
        missileMissed.setEditable(false);

        primaryStage.show();

        housesAnimation();

        guiLoadedSuccesfully = true;

        Platform.runLater(() -> createGuiAnimation());

        war.startGame(showAnimationObserver);
    }

    @Override
    @FXML
    public void addMissileLauncher() {
        if (numOfLaunchers >= MAX_NUMBER_OF_ROWS) {
            showAlert("Missile Launchers",
                    "You have reached the maximum amount of missile launchers !");
        }
        else {
            numOfLaunchers++;
            war.addMissileLauncher();
            addMissileLauncherAnimation(numOfLaunchers - 1);
        }
    }

    @Override
    @FXML
    public void addMissileLauncherDestructor() {
        if (numOfLaunchersDestructors >= MAX_NUMBER_OF_ROWS) {
            showAlert("Missile Launchers destructors",
                    "You have reached the maximum amount of missile launchers destructors !");
        }
        else {
            numOfLaunchersDestructors++;
            war.addMissileLauncherDestructor();
            List<Destructor_> lst = war.getMissileLauncherDestructors().getDestructor();
            addMissileLauncherDestructorAnimation( numOfLaunchersDestructors - 1, lst.get(lst.size() - 1));
        }

    }

    @Override
    @FXML
    public void addMissileDestructor() {
        if (numOfMissileDestructors >= MAX_NUMBER_OF_ROWS) {
            showAlert("Missile Destructors",
                    "You have reached the maximum amount of missile destructors !");
        }
        else {
            numOfMissileDestructors++;
            war.addMissileDestructor();
            addMissileDestructorAnimation(numOfMissileDestructors - 1);
        }
    }

    @FXML
    public void handleLaunchAMissile() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Launch a missile");
        dialog.setHeaderText("");
        dialog.setContentText("Please enter missile destination:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            launchAMissile(result.get());
        }
    }

    @Override
    public void launchAMissile(String destination) {
        war.launchAMissile(destination);
    }

    @Override
    @FXML
    public void destructAMissile() {
        war.destructAMissile();
    }

    @Override
    @FXML
    public void destructAMissileLauncher() {
        war.destructAMissileLauncher();
    }

    @Override
    @FXML
    public void showStatistics() {
        missileHit.setText(String.valueOf(war.getWarInformation().getMissilesHit()));
        missileLaunched.setText(String.valueOf(war.getWarInformation().getMissilesLaunched()));
        missileDestructed.setText(String.valueOf(war.getWarInformation().getMissilesDestructed()));
        totalDamage.setText(String.valueOf(war.getWarInformation().getTotalEconomicDamage()));
        missileLaunchersDestructed.setText(String.valueOf(war.getWarInformation().getMissilesLaunchersDestructed()));
        missileMissed.setText(String.valueOf(war.getWarInformation().getMissilesMissed()));
    }

    public static void showAlert(String relatedField,String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText(relatedField);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void gameOver() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Congratulations");
        alert.setHeaderText("you won !");
        alert.setContentText("all missile launchers were destructed ! you won !");
        alert.showAndWait();
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void housesAnimation() {
        Platform.runLater(() -> {
            String paths[] = {
                    "resources/Pictures/house1.png",
                    "resources/Pictures/house2.png"
            };

            for (int i = 0; i < MAX_NUMBER_OF_ROWS; i++) {
                Image image = new Image(paths[i % paths.length]);
                ImageView iv = new ImageView(image);
                iv.setFitHeight(mainGrid.getCellBounds(0, i).getHeight() - 10);
                iv.setFitWidth(mainGrid.getCellBounds(0, i).getWidth() - 10);
                mainGrid.add(iv, 0, i);
            }
        });
    }

    @Override
    public void addMissileLauncherAnimation(int index) {
        Image image = new Image("resources/Pictures/launcher.png");
        ImageView iv = new ImageView(image);
        iv.setFitHeight(mainGrid.getCellBounds(4, index).getHeight());
        iv.setFitWidth(mainGrid.getCellBounds(4, index).getWidth() - 30);
        mainGrid.add(iv, 4, index);
    }

    @Override
    public void addMissileLauncherDestructorAnimation(int index, Destructor_ destructor) {

        Image image;

        if (destructor.getType().toLowerCase().contains("ship")) {
            image = new Image("resources/Pictures/ship.png");
        }
        else if (destructor.getType().toLowerCase().contains("tank")) {
            image = new Image("resources/Pictures/tank.png");
        }
        else {
            image = new Image("resources/Pictures/plane.png");
        }

        ImageView iv = new ImageView(image);
        iv.setFitHeight(mainGrid.getCellBounds(1, index).getHeight() - 10);
        iv.setFitWidth(mainGrid.getCellBounds(1, index).getWidth() - 10);
        mainGrid.add(iv, 1, index);
    }

    @Override
    public void addMissileDestructorAnimation(int index) {
        Image image = new Image("resources/Pictures/missiledestructor.png");
        ImageView iv = new ImageView(image);
        iv.setFitHeight(mainGrid.getCellBounds(2, index).getHeight() - 10);
        iv.setFitWidth(mainGrid.getCellBounds(2, index).getWidth() - 10);
        mainGrid.add(iv, 2, index);
    }

    @Override
    public void createGuiAnimation() {
        List<Destructor> md = war.getMissileDestructors().getDestructor();
        List<Destructor_> mld = war.getMissileLauncherDestructors().getDestructor();
        List<Launcher> ml = war.getMissileLaunchers().getLauncher();

        numOfMissileDestructors = 0;
        numOfLaunchersDestructors = 0;
        numOfLaunchers = 0;

        mainGrid.getChildren().clear();
        housesAnimation();

        for (int i = 0 ; i < md.size(); i++){
           addMissileDestructorAnimation(i);
           numOfMissileDestructors++;
        }

        for(int i = 0 ; i < mld.size() ; i++) {
           addMissileLauncherDestructorAnimation(i, mld.get(i));
           numOfLaunchersDestructors++;
        }

        for(int i = 0 ; i < ml.size() ; i++) {
           addMissileLauncherAnimation(i);
           numOfLaunchers++;
        }
    }

    @Override
    public void boomAnimation(double targetX, double targetY) {
        final int SIZE = 50;
        ImageView boom = new ImageView(new Image("resources/Pictures/boom.png"));
        boom.setFitHeight(SIZE + 10);
        boom.setFitWidth(SIZE + 10);

        mainGrid.getChildren().add(boom);
        TranslateTransition tf = new TranslateTransition(Duration.seconds(1), boom);
        tf.setFromX(targetX);
        tf.setFromY(targetY);
        tf.setToX(targetX);
        tf.setToY(targetY);
        tf.play();
        tf.setOnFinished((ActionEvent other) -> mainGrid.getChildren().remove(boom));
    }

    @Override
    public void missileAnimation(GUIWarGameImpl.eDirection direction, AnimationParametersInterface api) {
        final int SIZE = 50;
        final int LEFT_ANGLE = 315;
        final int RIGHT_ANGLE = 135;
        double x, y;

        ImageView image = new ImageView(new Image("resources/Pictures/missile.png"));
        image.setFitWidth(SIZE);
        image.setFitHeight(SIZE);
        image.setRotate(direction == LEFT ? LEFT_ANGLE : RIGHT_ANGLE);

        image.setX(mainGrid.getWidth());
        image.setY(mainGrid.getHeight());

        mainGrid.getChildren().add(image);
        TranslateTransition tt = new TranslateTransition(Duration.seconds(api.delay()), image);
        tt.setFromX(direction == LEFT ? mainGrid.getWidth() : mainGrid.getCellBounds(1, 0).getWidth());
        tt.setFromY(direction == LEFT ? (numOfLaunchers * SIZE) * Math.random() : (numOfLaunchersDestructors * SIZE)  * Math.random());

        if(direction == LEFT) {
            x = 0;
            y = (mainGrid.getHeight() - 30) * Math.random();
        }
        else {
            final int DISTANCE_OF_OBJECT_FROM_EDGE = 100;
            x = mainGrid.getWidth() - DISTANCE_OF_OBJECT_FROM_EDGE;
            y = mainGrid.getCellBounds(4, indexOfLauncher.get() % MAX_NUMBER_OF_ROWS).getMinY();
        }

        tt.setToX(x);
        tt.setToY(y);

        if (api instanceof Missile) {
            new Thread(new MissileFollowerThread(tt, (Missile)api, image)).start();
        }

        tt.playFromStart();

        tt.setOnFinished((ActionEvent event) -> {
            if (!(api instanceof Missile) || (api instanceof Missile && !((Missile)api).isDestructed())) {
                mainGrid.getChildren().remove(image);
                if(direction == LEFT && api.success()) {
                    Platform.runLater(() -> boomAnimation(x, y) );
                }
            }
        });
    }

    @Override
    public void destructLauncherAnimation(int launcherIndex) {
        final int SIZE = 50;

        ImageView image = new ImageView(new Image("resources/Pictures/x.png"));
        image.setFitWidth(SIZE);
        image.setFitHeight(SIZE);

        image.setFitHeight(mainGrid.getCellBounds(4, launcherIndex % MAX_NUMBER_OF_ROWS).getHeight() - 10);
        image.setFitWidth(mainGrid.getCellBounds(4, launcherIndex % MAX_NUMBER_OF_ROWS).getWidth() - 10);
        mainGrid.add(image, 4, launcherIndex % MAX_NUMBER_OF_ROWS);

        if(launcherIndex == MAX_NUMBER_OF_ROWS - 1) {
            war.getWarInformation().Destructor_info("game over ! end of game "+ timeSinceGameStartedInSeconds());
            war.getWarInformation().Destructorinfo("game over ! end of game "+ timeSinceGameStartedInSeconds());
            war.getWarInformation().Launcherinfo("game over ! end of game "+ timeSinceGameStartedInSeconds());

            gameOver();
            war.setGameOver(true);
        }
    }

    @SuppressWarnings("deprecation")
    public class StatisticsUpdater implements Observer {

        @Override
        @SuppressWarnings("deprecation")
        public void update(Observable o, Object arg) {
            if (guiLoadedSuccesfully) {
                Platform.runLater(() -> showStatistics());
            }
            if (arg == INCREMENT_MISSILES_LAUNCHERS_DESTRUCTED) {
                Platform.runLater(() -> {
                    destructLauncherAnimation(war.getWarInformation().getMissilesLaunchersDestructed() - 1);
                    indexOfLauncher.incrementAndGet();
                });
            }
        }
    }

    @SuppressWarnings("deprecation")
    public class ShowAnimationObserver implements Observer {

        @Override
        @SuppressWarnings("deprecation")
        public void update(Observable o, Object arg) {

            AnimationParametersInterface api = (AnimationParametersInterface) arg;

            if (arg instanceof DestructedLauncher) {
                Platform.runLater(() -> missileAnimation(RIGHT, api));
            }
            else if(arg instanceof Missile) {
                Platform.runLater(() -> missileAnimation(LEFT, api));
            }
        }
    }

    public class MissileFollowerThread implements Runnable {

        private TranslateTransition translateTransition;
        private Missile missile;
        private ImageView image;

        public MissileFollowerThread(TranslateTransition translateTransition, Missile missile, ImageView image) {
            this.translateTransition = translateTransition;
            this.missile = missile;
            this.image = image;
        }

        @Override
        public void run() {

            while(missile.isFlying()) {
                synchronized (missile) {
                    try {
                        missile.wait(missile.getFlyTime());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (missile.isDestructed()) {
                    Platform.runLater(() -> {
                        translateTransition.setDelay(Duration.seconds(1));
                        image.setImage(new Image("/resources/Pictures/bombedmissile.png"));
                        translateTransition.setOnFinished((ActionEvent e) -> mainGrid.getChildren().remove(image));
                    });
                    break;
                }
            }

        }
    }

	@Override
	public void addMissileLauncherEvent() {
		Platform.runLater(() -> {
		addMissileLauncher();
		});
	}

	@Override
	public void launchMissileEvent(String destination) {
		Platform.runLater(() -> {
			launchAMissile(destination);
			});

	}

	@Override
	public void destructMissileEvent() {
		Platform.runLater(() -> {
			destructAMissile();
			});


	}

}
