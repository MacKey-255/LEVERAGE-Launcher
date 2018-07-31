package leverage.gui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;
import leverage.Console;
import leverage.Kernel;
import leverage.OSArch;
import leverage.Settings;
import leverage.auth.Authentication;
import leverage.auth.user.User;
import leverage.auth.user.UserType;
import leverage.client.AntiCheat;
import leverage.exceptions.AuthenticationException;
import leverage.exceptions.DownloaderException;
import leverage.exceptions.GameLauncherException;
import leverage.game.GameLauncher;
import leverage.game.download.Downloader;
import leverage.game.profile.Profile;
import leverage.game.profile.ProfileType;
import leverage.game.profile.Profiles;
import leverage.game.version.VersionMeta;
import leverage.game.version.VersionType;
import leverage.game.version.Versions;
import leverage.game.version.asset.TexturePreview;
import leverage.gui.lang.Language;
import leverage.util.Urls;
import leverage.util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainFX {

    // Listado de Componentes en el FXML

    @FXML private Label progressText, newsLabel, optimizeLabel, skinsLabel, settingsLabel, launchOptionsLabel,
            keepLauncherOpen, outputLog, enableSnapshots, historicalVersions, launcherOptimize, enableReopen,
            advancedSettings, resolutionLabel, gameDirLabel, javaExecLabel, javaArgsLabel, accountButton,
            switchAccountButton, languageButton, newsTitle, newsText, slideBack, slideForward, rotateRight,
            rotateLeft, versionLabel, usernameLabel, passwordLabel, existingLabel, launcherSettings,
            nameLabel, profileVersionLabel, iconLabel, helpButton, gameVersion, playersServer,
            authenticationLabel, authServer, poweredLabel, profileRamLabel, profileRamAssignateLabel;
    @FXML private Button playButton, deleteButton, changeIcon, logoutButton,
            loginButton, registerButton, loginExisting, cancelButton, saveButton, selectSkin,
            exportLogs, deleteCache, deleteLogs, deleteCrash, deleteSave, deleteConfig, deleteExtra,
            deleteShader, deleteResources, profilePopupButton;
    @FXML private Tab loginTab, newsTab, optimizeTab, skinsTab,
            settingsTab, launchOptionsTab, profileEditorTab;
    @FXML private ProgressBar progressBar;
    @FXML private TabPane contentPane;
    @FXML private ListView<Label> languagesList, profileList, profilePopupList;
    @FXML private ListView<ImageView> iconList;
    @FXML private VBox progressPane, existingPanel, playPane, newsContainer;
    @FXML private HBox tabMenu, slideshowBox;
    @FXML private TextField username, profileName,javaExec, gameDir, javaArgs,
            resH, resW;
    @FXML private PasswordField password;
    @FXML private Slider profileRam;
    @FXML private ComboBox<User> existingUsers;
    @FXML private ComboBox<VersionMeta> versionList;
    @FXML private StackPane versionBlock, javaArgsBlock, javaExecBlock, iconBlock, ramBlock;
    @FXML private ImageView profileIcon, slideshow, skinPreview;
    @FXML private RadioButton authLeverage, authOffline;
    @FXML private Hyperlink forgotPasswordLink;

    //

    private Kernel kernel;
    private Console console;
    private Settings settings;
    private Stage stage;
    private Scene mainScene;
    private final List<Slide> slides = new ArrayList<>();
    private int currentSlide;
    private int currentPreview; // 0 = front / 1 = right / 2 = back / 3 = left
    private final Image[] skinPreviews = new Image[4];
    private Image skin, alex, steve;
    private boolean texturesLoaded;
    private boolean iconListLoaded, versionListLoaded, languageListLoaded, loadingTextures, profileListLoaded,
            profileListPopupLoaded;

    /**
     * Initializes all required stuff from the GUI
     * @param k The Kernel instance
     * @param s The Stage instance
     */
    public final void initialize(Kernel k, Stage s, Scene scene, final Scene browser) {
        // Require to exit using Platform.exit()
        Platform.setImplicitExit(false);

        // Configuracion Basica
        kernel = k;
        console = k.getConsole();
        settings = k.getSettings();
        stage = s;
        mainScene = scene;

        // Asignando Version y Creador
        versionLabel.setText(Kernel.KERNEL_BUILD_NAME);
        poweredLabel.setText(Kernel.KERNEL_CREATOR_NAME);

        // Cargando Sistema de Noticias
        slideshowBox.setVisible(false);
        slideshowBox.setManaged(false);
        newsTitle.setText("Cargando Noticias...");
        newsText.setText("Por favor espere un Momento..");
        loadSlideshow();

        // Refrescando Login
        refreshSession();

        // Preparando Lista de Lenguajes
        languageButton.setText(settings.getSupportedLocales().get(settings.getLocale()));

        // Actualizando Configuraciones de Labels
        toggleLabel(keepLauncherOpen, settings.getKeepLauncherOpen());
        toggleLabel(outputLog, settings.getShowGameLog());
        toggleLabel(enableSnapshots, settings.getEnableSnapshots());
        toggleLabel(enableReopen, settings.getEnableReopen());
        toggleLabel(historicalVersions, settings.getEnableHistorical());
        toggleLabel(advancedSettings, settings.getEnableAdvanced());

        // Preparando Seleccionador
        resW.setEditable(true);
        resH.setEditable(true);

        // Verificar si el Usuario esta OFFLINE
        if (Kernel.USE_LOCAL) {
            playButton.setMinWidth(290);
        }

        // Cargar Elementos Graficos
        localizeElements();

        // Validar Perfil Seleccionado
        validateSelectedProfile();

        //Manual component resize binding to fix JavaFX maximize bug
        TimerTask newsResize = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!MainFX.this.stage.isMaximized()) {
                            Window w = MainFX.this.mainScene.getWindow();
                            if (w == null) {
                                w = browser.getWindow();
                            }
                            if (w != null) {
                                settings.setLauncherHeight(w.getHeight());
                                settings.setLauncherWidth(w.getWidth());
                            }
                        }
                        double computedHeight = MainFX.this.newsContainer.heightProperty().doubleValue()  * 0.7;
                        double computedWidth = MainFX.this.newsContainer.widthProperty().doubleValue()  * 0.7;
                        if (MainFX.this.slideshow.getImage() != null) {
                            if (computedHeight > MainFX.this.slideshow.getImage().getHeight()) {
                                MainFX.this.slideshow.setFitHeight(MainFX.this.slideshow.getImage().getHeight());
                            } else {
                                MainFX.this.slideshow.setFitHeight(computedHeight);
                            }
                            if (computedWidth > MainFX.this.slideshow.getImage().getWidth()) {
                                MainFX.this.slideshow.setFitWidth(MainFX.this.slideshow.getImage().getWidth());
                            } else {
                                MainFX.this.slideshow.setFitWidth(computedWidth);
                            }
                        }
                    }
                });
            }
        };
        Timer resize = new Timer();
        resize.schedule(newsResize, 0, 25);

        //Close popups on resize
        mainScene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                MainFX.this.checkPopups();
            }
        });
        mainScene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                MainFX.this.checkPopups();
            }
        });
    }

    /**
     * Cargar Listado de Lenguajes
     */
    private void loadLanguages() {
        console.print("Cargando Lenguajes...");
        HashMap<String, String> supportedLocales = settings.getSupportedLocales();
        ObservableList<Label> languageListItems = FXCollections.observableArrayList();
        for (String key : supportedLocales.keySet()) {
            Image i = new Image("/leverage/gui/textures/flags/flag_" + key + ".png");
            Label l = new Label(supportedLocales.get(key), new ImageView(i));
            l.setId(key);
            languageListItems.add(l);
        }
        languagesList.setItems(languageListItems);
        console.print("Lenguajes Cargados.");
    }

    /**
     * Cargar Anuncios de Advertencia al Usuario (Ej: Esta Baneado)
     */
    private void fetchAds() {
        User user = kernel.getAuthentication().getSelectedUser();
        if (user.getType() != UserType.OFFLINE) {
            String profileID = user.getSelectedProfile();
            String adsCheck = Urls.urlDataProfileId(profileID);
            String response = Utils.readURL(adsCheck);
            if (!response.isEmpty()) {
                String[] chunks = response.split(":");
                console.print(chunks[1]);
                if(chunks[0].equals("BAN") || chunks[0].equals("NICKBAN") || chunks[0].equals("IP")) {
                    // Si es de Ban entonces Cierra el Launcher
                    kernel.showAlert(Alert.AlertType.ERROR, "Baneado del Servidor", chunks[1]);
                    kernel.exitSafely();
                    return;
                }
                // Mostrar Mensaje
                kernel.showAlert(Alert.AlertType.WARNING, null, chunks[1]);
                console.print("Anuncios Cargados.");
            } else {
                console.print("No existe Informacion en los Anuncios.");
            }
        } else {
            console.print("Anuncios no disponibles.");
        }
    }

    /**
     * Actualizar/Cargar Componentes Graficos
     */
    private void localizeElements() {
        helpButton.setText(Language.get(2));
        logoutButton.setText(Language.get(3));
        newsLabel.setText(Language.get(4));
        optimizeLabel.setText(Language.get(106));
        launcherOptimize.setText(Language.get(121));
        skinsLabel.setText(Language.get(5));
        settingsLabel.setText(Language.get(6));
        launchOptionsLabel.setText(Language.get(7));
        if (kernel.getGameLauncher().isRunning()) {
            playButton.setText(Language.get(14));
        } else {
            if (Kernel.USE_LOCAL) {
                playButton.setText(Language.get(79));
            } else {
                playButton.setText(Language.get(12));
            }
        }
        updateAuthServer();
        usernameLabel.setText(Language.get(18));
        passwordLabel.setText(Language.get(19));
        loginButton.setText(Language.get(20));
        loginExisting.setText(Language.get(20));
        registerButton.setText(Language.get(21));
        changeIcon.setText(Language.get(24));
        exportLogs.setText(Language.get(27));
        profileRamLabel.setText(Language.get(129));
        launcherSettings.setText(Language.get(45));
        keepLauncherOpen.setText(Language.get(46));
        outputLog.setText(Language.get(47));
        enableSnapshots.setText(Language.get(48));
        enableReopen.setText(Language.get(136));
        historicalVersions.setText(Language.get(49));
        advancedSettings.setText(Language.get(50));
        saveButton.setText(Language.get(52));
        cancelButton.setText(Language.get(53));
        deleteButton.setText(Language.get(54));
        nameLabel.setText(Language.get(63));
        profileVersionLabel.setText(Language.get(64));
        resolutionLabel.setText(Language.get(65));
        gameDirLabel.setText(Language.get(66));
        javaExecLabel.setText(Language.get(67));
        javaArgsLabel.setText(Language.get(68));
        existingLabel.setText(Language.get(85));
        switchAccountButton.setText(Language.get(86));
        selectSkin.setText(Language.get(87));
        iconLabel.setText(Language.get(92));
        deleteCache.setText(Language.get(94));
        deleteLogs.setText(Language.get(107));
        deleteCrash.setText(Language.get(108));
        deleteSave.setText(Language.get(112));
        deleteConfig.setText(Language.get(109));
        deleteExtra.setText(Language.get(110));
        deleteShader.setText(Language.get(113));
        deleteResources.setText(Language.get(111));
        forgotPasswordLink.setText(Language.get(97));
        profileName.setPromptText(Language.get(98));
        authenticationLabel.setText(Language.get(99));
        onlineList();
        profileRamAssignateLabel.setText(String.valueOf((int) profileRam.getValue()) + " MB");
        if (slides.isEmpty()) {
            newsTitle.setText(Language.get(102));
            newsText.setText(Language.get(103));
        }
        updateGameVersion();
    }

    /**
     * Cargar Texturas del Skins del Usuario
     */
    private void loadTextures() {
        console.print("Cargando Modulo de Texturas...");
        if (loadingTextures) {
            return;
        }
        if(kernel.getAuthentication().getSelectedUser().getType() == UserType.OFFLINE) {
            selectSkin.setDisable(true);
        }

        alex = new Image("/leverage/gui/textures/alex.png");
        steve = new Image("/leverage/gui/textures/steve.png");
        User selected = kernel.getAuthentication().getSelectedUser();

        if (selected.getType() == UserType.OFFLINE) {
            console.print("Cargando Texturas...");
            skin = alex;
            updatePreview();
            console.print("Texturas Cargadas.");
            return ;
        }

        console.print("Cargando Texturas...");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    console.print("Cargando Texturas de Skins...");
                    loadingTextures = true;
                    if (alex == null || steve == null) {
                        //Load placeholder skins
                        alex = new Image("/leverage/gui/textures/alex.png");
                        steve = new Image("/leverage/gui/textures/steve.png");
                    }

                    String skinPath = Urls.skinsPathProfileId(selected.getUsername().replace("leverage://", "").toLowerCase());
                    skin = new Image(Utils.readCachedStream(skinPath));
                    if (skin == null || skin.getHeight() == 0) {
                        skin = steve;
                    } else if (skin.getHeight() == 0) {
                        skin = alex;
                    }
                    alex = skin;

                    texturesLoaded = true;
                    console.print("Texturas Cargadas.");
                    MainFX.this.updatePreview();

                    selectSkin.setDisable(false);
                    //selectCape.setDisable(false);
                } catch (Exception ex) {
                    console.print("Fallido la carga de Skin del Servidor.");
                    skin = alex;
                    updatePreview();
                    ex.printStackTrace(console.getWriter());
                }
                loadingTextures = false;
            }
        });
        t.start();
    }

    /**
     * Actualizar Vistas del Skins
     */
    private void updatePreview() {
        skinPreviews[0] = TexturePreview.resampleImage(TexturePreview.generateFront(skin, null, true), 10);
        skinPreviews[1] = TexturePreview.resampleImage(TexturePreview.generateRight(skin, null), 10);
        skinPreviews[2] = TexturePreview.resampleImage(TexturePreview.generateBack(skin, null, true), 10);
        skinPreviews[3] = TexturePreview.resampleImage(TexturePreview.generateLeft(skin, null), 10);
        skinPreview.setImage(skinPreviews[currentPreview]);
    }

    /**
     * Subir de Skins
     * @param file File to be submited. Null if it's a deletion.
     */
    private void submitChange(File file) {
        String url = Urls.CHANGESKIN_URL;
        Map<String, String> params = new HashMap<>();
        params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
        params.put("Client-Token", kernel.getAuthentication().getClientToken());
        byte[] data = null;
        if (file != null) {
            console.print(file.length() + " - Tamaño del Skins Subido");
            if (file.length() > 131072) {
                console.print("Tamaño del Skins excedido, maximo 128KB ");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(105));
                    }
                });
                return;
            }
            try {
                data = Files.readAllBytes(file.toPath());
                params.put("Skin-Type", "alex");
                params.put("Content-Type", "image/png");
            } catch (Exception ex) {
                console.print("Fallida la Lectura de Texturas.");
                ex.printStackTrace(console.getWriter());
            }
        }
        try {
            String r = Utils.sendPost(url, data, params);
            String text;
            if (!"OK".equals(r)) {
                if (file != null) {
                    text = Language.get(42);
                } else {
                    text = Language.get(33);
                }
                console.print("Fallido " + (file != null ? "el cambio" : "la eliminacion") + " de Skins.");
                final String finalText = text;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        kernel.showAlert(Alert.AlertType.ERROR, null, finalText);
                        selectSkin.setDisable(false);
                    }
                });
                return;
            }
            if (file != null) {
                text = Language.get(40);
            } else {
                text = Language.get(34);
            }
            console.print("Skin " + (file != null ? "cambiado" : "borrado") + " con exito!!");
            final String finalText = text;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    kernel.showAlert(Alert.AlertType.INFORMATION, null, finalText);
                    selectSkin.setDisable(false);
                    loadTextures();
                }
            });
        } catch (IOException ex) {
            console.print("Fallido el Envio del Skins.");
            ex.printStackTrace(console.getWriter());
        }
    }

    /**
     * Accion para Subir Skins
     */
    @FXML private void changeSkin() {
        final File selected = selectFile(Language.get(44), "*.png", "open");
        if (selected != null) {
            selectSkin.setDisable(true);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    MainFX.this.submitChange(selected);
                }
            });
            t.start();
        }
    }

    /**
     * Cargador de Noticias
     */
    private void loadSlideshow() {
        console.print("Cargando Slides de Noticias...");

        try {
            String newsURL = Urls.newsUrl;
            String response = Utils.readURL(newsURL);
            if (response.isEmpty()) {
                console.print("El Servidor no ha devuelto nunguna Noticia.");
                return;
            }
            JSONObject root = new JSONObject(response);
            JSONArray entries = root.getJSONArray("entries");
            for (int i = 0; i < entries.length(); i++) {
                JSONObject entry = entries.getJSONObject(i);
                boolean isDemo = false;
                JSONArray tags = entry.getJSONArray("tags");
                for (int j = 0; j < tags.length(); j++) {
                    if ("demo".equalsIgnoreCase(tags.getString(j))) {
                        isDemo = true;
                        break;
                    }
                }
                if (isDemo)
                    continue;
                JSONObject content = entry.getJSONObject("content").getJSONObject("en-us");
                Slide s = new Slide(content.getString("action"), Urls.media + content.getString("image"), content.getString("title"), content.getString("text"));
                slides.add(s);
            }
        } catch (Exception ex) {
            newsTitle.setText(Language.get(80));
            newsText.setText(Language.get(101));
            console.print("No se ha podido Cargar los Datos de las Noticias.");
            ex.printStackTrace(console.getWriter());
            return;
        }
        if (!slides.isEmpty()) {
            slideshowBox.setVisible(true);
            slideshowBox.setManaged(true);
            Slide s = slides.get(0);
            Image i = s.getImage();
            if (i != null) {
                slideshow.setImage(s.getImage());
            }
            newsTitle.setText(s.getTitle());
            newsText.setText(s.getText());
        } else {
            newsTitle.setText(Language.get(102));
            newsText.setText(Language.get(103));
        }
        console.print("Slides de Noticias cargado.");
    }

    public void onlineList() {
        String response = Utils.readURL(Urls.onlineData);
        if (!response.isEmpty()) {
            console.print("Cargando listado de Usuarios Conectados al Servidor.");
            JSONObject object = new JSONObject(response);
            if(!object.getBoolean("error")) {
                JSONObject obj = object.getJSONObject("request");
                String text = obj.getInt("numplayers") + " / " + obj.getInt("maxplayers") + " " + obj.getString("online");
                // Mostrar Onlines
                playersServer.setText(text);
            }

        } else {
            console.print("No hay Usuarios conectados..");
            String text = Language.get(133) + Language.get(134);
            playersServer.setText(text);
        }
    }

    /**
     * Changes the news slide
     * @param e The trigger event
     */
    @FXML public final void changeSlide(MouseEvent e) {
        if (slides.isEmpty()) {
            //No slides
            return;
        }
        Label source = (Label)e.getSource();
        if (source == slideBack) {
            if (currentSlide == 0) {
                currentSlide = slides.size() - 1;
            } else {
                currentSlide--;
            }
        } else if (source == slideForward) {
            if (currentSlide == slides.size() - 1) {
                currentSlide = 0;
            } else {
                currentSlide++;
            }
        }
        final Slide s = slides.get(currentSlide);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Image i = s.getImage();
                if (i != null) {
                    slideshow.setImage(i);
                }
            }
        });
        t.start();
        newsTitle.setText(s.getTitle());

        newsText.setText(s.getText());
    }

    /**
     * Performs an action when a slide is clicked
     */
    @FXML public final void performSlideAction() {
        if (slides.isEmpty()) {
            //No slides
            return;
        }
        Slide s = slides.get(currentSlide);
        kernel.getHostServices().showDocument(Urls.leverage);
    }

    /**
     * Rotates the skin preview
     * @param e The trigger event
     */
    @FXML public final void rotatePreview(MouseEvent e) {
        Label src = (Label)e.getSource();
        if (src == rotateRight) {
            if (currentPreview < 3) {
                currentPreview++;
                skinPreview.setImage(skinPreviews[currentPreview]);
            } else {
                currentPreview = 0;
                skinPreview.setImage(skinPreviews[currentPreview]);
            }
        } else if (src == rotateLeft) {
            if (currentPreview > 0) {
                currentPreview--;
                skinPreview.setImage(skinPreviews[currentPreview]);
            } else {
                currentPreview = 3;
                skinPreview.setImage(skinPreviews[currentPreview]);
            }
        }
    }

    /**
     * Loads profiles list items
     */
    private void loadProfileList() {
        console.print("Cargando Lista de Perfiles...");
        ObservableList<Label> profileListItems = getProfileList();

        //Add "Add New Profile" item
        profileListItems.add(0, new Label(Language.get(51), new ImageView(new Image("/leverage/gui/textures/add.png"))));
        profileList.setItems(profileListItems);
        profileListLoaded = true;
        console.print("Lista de Perfiles Cargada.");
    }

    /**
     * Loads profiles popup list items
     */
    private void loadProfileListPopup() {
        console.print("Cargando Lista de Perfiles popup...");
        ObservableList<Label> profileListItems = getProfileList();
        profilePopupList.setItems(profileListItems);
        profileListPopupLoaded = true;
        console.print("Lista de Perfiles popup Cargado.");
    }

    /**
     * Generates an ObservableList of Labels representing each profile
     * @return The profiles ObservableList
     */
    private ObservableList<Label> getProfileList() {
        ObservableList<Label> profileListItems = FXCollections.observableArrayList();
        Profiles ps = kernel.getProfiles();
        Label l;
        ImageView iv;
        String text;
        for (Profile p : ps.getProfiles()) {
            if (p.getType() == ProfileType.SNAPSHOT && !settings.getEnableSnapshots()) {
                continue;
            }
            switch (p.getType()) {
                case RELEASE:
                    iv = new ImageView(kernel.getProfileIcon("Grass"));
                    text = Language.get(59);
                    break;
                case SNAPSHOT:
                    iv = new ImageView(kernel.getProfileIcon("Crafting_Table"));
                    text = Language.get(60);
                    break;
                default:
                    text = p.hasName() ? p.getName() : Language.get(70);
                    String pi = p.hasIcon() ? p.getIcon() : "Furnace";
                    iv = new ImageView(kernel.getProfileIcon(pi));
                    break;
            }
            iv.setFitWidth(68);
            iv.setFitHeight(68);
            l = new Label(text, iv);
            //Fetch Minecraft version used by the profile
            VersionMeta verID;
            switch (p.getType()) {
                case CUSTOM:
                    Versions versions = kernel.getVersions();
                    verID = p.hasVersion() ? p.getVersionID() : versions.getLatestRelease();
                    break;
                case RELEASE:
                    verID = kernel.getVersions().getLatestRelease();
                    break;
                default:
                    verID = kernel.getVersions().getLatestSnapshot();
                    break;
            }
            l.setId(p.getID());
            if (verID != null) {
                //If profile has any known version just show it below the profile name
                if (verID.getType() == VersionType.SNAPSHOT && !settings.getEnableSnapshots()) {
                    continue;
                }
                if ((verID.getType() == VersionType.OLD_ALPHA || verID.getType() == VersionType.OLD_BETA) && !settings.getEnableHistorical()) {
                    continue;
                }
                l.setText(l.getText() + '\n' + verID.getID());
            }
            if (ps.getSelectedProfile().equals(p)) {
                l.getStyleClass().add("selectedProfile");
            }
            profileListItems.add(l);
        }
        return profileListItems;
    }

    /**
     * Updates the selected minecraft version indicator
     */
    private void updateGameVersion() {
        Profile p = kernel.getProfiles().getSelectedProfile();
        if (p != null) {
            switch (p.getType()) {
                case RELEASE:
                    gameVersion.setText(Language.get(26));
                    break;
                case SNAPSHOT:
                    gameVersion.setText(Language.get(32));
                    break;
                default:
                    if (p.isLatestRelease()) {
                        gameVersion.setText(Language.get(26));
                    } else if (p.isLatestSnapshot()) {
                        gameVersion.setText(Language.get(32));
                    } else if (p.hasVersion()) {
                        VersionMeta version = p.getVersionID();
                        gameVersion.setText("Minecraft " + version.getID());
                    }
                    break;
            }
        }  else {
            gameVersion.setText("");
        }
    }

    /**
     * Loads the profile icons
     */
    private void loadIcons() {
        console.print("Cargando Iconos...");
        ObservableList<ImageView> icons = FXCollections.observableArrayList();
        Set<String> keys = kernel.getIcons().keySet();
        for (String key : keys) {
            if (!key.equals("Crafting_Table") && !key.equals("Grass")) {
                ImageView imv = new ImageView(kernel.getProfileIcon(key));
                imv.setFitHeight(68);
                imv.setFitWidth(68);
                imv.setId(key);
                icons.add(imv);
            }
        }
        iconList.setItems(icons);
        console.print("Iconos Cargados.");
    }

    /**
     * Validates the selected profile according to the constraints
     */
    private void validateSelectedProfile() {
        Profiles ps = kernel.getProfiles();

        //Check if selected profile passes the current settings
        Profile selected = ps.getSelectedProfile();
        VersionMeta selectedVersion = selected.getVersionID();

        if (selected.getType() == ProfileType.SNAPSHOT && !settings.getEnableSnapshots()) {
            ps.setSelectedProfile(ps.getReleaseProfile());
        } else if (selected.getType() == ProfileType.CUSTOM) {
            VersionType type = selectedVersion.getType();
            if (type == VersionType.SNAPSHOT && !settings.getEnableSnapshots()) {
                ps.setSelectedProfile(ps.getReleaseProfile());
            } else if (type == VersionType.OLD_ALPHA && !settings.getEnableHistorical()) {
                ps.setSelectedProfile(ps.getReleaseProfile());
            } else if (type == VersionType.OLD_BETA && !settings.getEnableHistorical()) {
                ps.setSelectedProfile(ps.getReleaseProfile());
            }
        }

        updateGameVersion();
    }

    /**
     * Selects the selected profile from the list
     */
    @FXML private void selectProfile() {
        if (profilePopupList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        //Select profile and refresh list
        kernel.getProfiles().setSelectedProfile(kernel.getProfiles().getProfile(profilePopupList.getSelectionModel().getSelectedItem().getId()));
        updateGameVersion();
        SingleSelectionModel<Tab> selection = contentPane.getSelectionModel();
        Tab selectedTab = selection.getSelectedItem();
        if (selectedTab == launchOptionsTab) {
            loadProfileList();
        } else {
            profileListLoaded = false;
        }
        profileListPopupLoaded = false;
        profilePopupList.setVisible(false);
        kernel.saveProfiles();
    }

    /**
     * Downloads and launches the game
     */
    @FXML public final void launchGame() {
        progressPane.setVisible(true);
        playPane.setVisible(false);
        progressBar.setProgress(0);
        progressText.setText("");
        final Downloader d = kernel.getDownloader();
        final GameLauncher gl = kernel.getGameLauncher();
        if (!Kernel.USE_LOCAL) {
            AntiCheat anti = new AntiCheat(kernel);

            // Anticheat en Accion
            anti.compare();                 // Genera y envia datos al Servidor
            if(!anti.isAccept()) {
                // Mostrar Mensaje
                kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(137));
                console.print("Cliente no concuerda con el Servidor.");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        playPane.setVisible(true);
                        progressPane.setVisible(false);
                        playButton.setText(Language.get(12));
                        profilePopupButton.setDisable(false);
                        playButton.setDisable(false);
                    }
                });
                return;
            } else {
                // Enviar Conexion RCON
                //try {
                    try {
                        AntiCheat.addWhiteList(kernel.getAuthentication().getSelectedUser().getAccessToken());
                    } catch (IOException e) {
                        // Mostrar Mensaje
                        kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(138));
                        console.print("Cliente no registrado en el Servidor.");
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressPane.setVisible(false);
                                playPane.setVisible(true);
                                playButton.setText(Language.get(12));
                                playButton.setDisable(false);
                                profilePopupButton.setDisable(false);
                            }
                        });
                        return;
                    }
                /*} catch (IOException e) {
                    // No se puede Conectar con el Server
                    console.print("Error de Conexion al Servidor con RCON");
                    kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(138));
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progressPane.setVisible(false);
                            playPane.setVisible(true);
                            playButton.setText(Language.get(12));
                            playButton.setDisable(false);
                            profilePopupButton.setDisable(false);
                        }
                    });
                    return ;
                } catch (AuthenticationException e) {
                    console.print("Error de Conexion al Servidor con RCON");
                    e.printStackTrace();
                }*/
            }
        }

        //Keep track of the progress
        final TimerTask progressTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        MainFX.this.progressBar.setProgress(d.getProgress());
                        MainFX.this.progressText.setText(Language.get(13) + ' ' + d.getCurrentFile() + "...");
                    }
                });
            }
        };

        Thread runThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Begin download and game launch task
                try {
                    Timer timer = new Timer();
                    timer.schedule(progressTask, 0, 25);
                    d.download();
                    timer.cancel();
                    timer.purge();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progressText.setText(Language.get(78));
                            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                        }
                    });
                    gl.launch(MainFX.this);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progressPane.setVisible(false);
                            playPane.setVisible(true);
                            playButton.setText(Language.get(12));
                            playButton.setDisable(true);
                            profilePopupButton.setDisable(true);
                        }
                    });

                    if (!settings.getKeepLauncherOpen()) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                MainFX.this.stage.hide();
                            }
                        });
                    }
                } catch (DownloaderException e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            kernel.showAlert(Alert.AlertType.ERROR, Language.get(83), Language.get(84));
                        }
                    });
                    console.print("Failed to perform game download task");
                    e.printStackTrace(console.getWriter());
                } catch (GameLauncherException e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            kernel.showAlert(Alert.AlertType.ERROR, Language.get(81), Language.get(82));
                        }
                    });
                    if (!Kernel.USE_LOCAL) {
                        AntiCheat.removeWhiteList(kernel.getAuthentication().getSelectedUser().getDisplayName());
                        /*try {
                        } catch (IOException e1) {
                            console.print("Error de Conexion al Servidor con RCON");
                            kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(139));
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    progressPane.setVisible(false);
                                    playPane.setVisible(true);
                                    playButton.setText(Language.get(12));
                                    playButton.setDisable(false);
                                    profilePopupButton.setDisable(false);
                                }
                            });
                        } catch (AuthenticationException ex) {
                            console.print("Error de Conexion al Servidor con RCON");
                            ex.printStackTrace();
                        }*/
                    }
                    console.print("Failed to perform game launch task");
                    e.printStackTrace(console.getWriter());
                }
            }
        });
        console.print("Ejecutando Minecraft");
        runThread.start();
    }

    /**
     * Callback from Game Launcher
     * @param error True if an error happened during launch
     */
    public final void gameEnded(final boolean error) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (error) {
                    kernel.showAlert(Alert.AlertType.ERROR, Language.get(16), Language.get(15));
                }
                if (!settings.getEnableReopen()) {
                    kernel.exitSafely();
                }
                playButton.setDisable(false);
                profilePopupButton.setDisable(false);
                if (Kernel.USE_LOCAL) {
                    playButton.setText(Language.get(79));
                } else {
                    playButton.setText(Language.get(12));

                    AntiCheat.removeWhiteList(kernel.getAuthentication().getSelectedUser().getDisplayName());
                    /*try {
                    } catch (IOException e) {
                        console.print("Error de Conexion al Servidor con RCON");
                        kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(139));
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressPane.setVisible(false);
                                playPane.setVisible(true);
                                playButton.setText(Language.get(12));
                                playButton.setDisable(false);
                                profilePopupButton.setDisable(false);
                            }
                        });
                    } catch (AuthenticationException e) {
                        console.print("Error de Conexion al Servidor con RCON");
                        e.printStackTrace();
                    }*/
                }
            }
        });
    }

    public final void show() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                MainFX.this.stage.show();
                playButton.setDisable(false);
                profilePopupButton.setDisable(false);
                if (Kernel.USE_LOCAL) {
                    playButton.setText(Language.get(79));
                } else {
                    playButton.setText(Language.get(12));
                }
                AntiCheat.removeWhiteList(kernel.getAuthentication().getSelectedUser().getAccessToken());
            }
        });
    }

    /**
     * Shows the language list
     */
    @FXML public final void showLanguages(Event e) {
        e.consume();
        if (languagesList.isVisible()) {
            languagesList.setVisible(false);
        } else {
            if (!languageListLoaded) {
                loadLanguages();
                languageListLoaded = true;
            }
            languagesList.setVisible(true);
        }
    }

    public final void checkPopups() {
        if (languagesList.isVisible()) {
            languagesList.setVisible(false);
        }
        if (switchAccountButton.isVisible()) {
            switchAccountButton.setVisible(false);
        }
        if (profilePopupList.isVisible()) {
            profilePopupList.setVisible(false);
        }
        if (iconList.isVisible()) {
            iconList.setVisible(false);
        }
    }

    /**
     * Deselects the current user and allows to select another
     */
    @FXML public final void switchAccount() {
        if (switchAccountButton.isVisible()) {
            switchAccountButton.setVisible(false);
        }
        Authentication a = kernel.getAuthentication();
        kernel.closeWeb();
        a.setSelectedUser(null);
        kernel.saveProfiles();
        showLoginPrompt(true);
        updateExistingUsers();
    }

    /**
     * Shows the profile popup list
     */
    @FXML public final void showProfiles() {
        if (profilePopupList.isVisible()) {
            profilePopupList.setVisible(false);
        } else {
            if (!profileListPopupLoaded) {
                loadProfileListPopup();
            }
            Bounds b = playButton.localToScene(playButton.getBoundsInLocal());
            profilePopupList.setTranslateX(b.getMinX() - 100);
            profilePopupList.setTranslateY(b.getMinY() - 180);
            profilePopupList.setVisible(true);
            profilePopupList.getSelectionModel().clearSelection();
        }
    }

    /**
     * Shows the profile editor profile icons
     */
    @FXML public final void showIcons(Event e) {
        e.consume();
        if (iconList.isVisible()) {
            iconList.setVisible(false);
        } else {
            if (!iconListLoaded) {
                loadIcons();
                iconListLoaded = true;
            }
            //Calculate change icon button position on scene
            Bounds b = changeIcon.localToScene(changeIcon.getBoundsInLocal());
            iconList.setTranslateX(b.getMinX());
            iconList.setTranslateY(b.getMaxY());
            iconList.setVisible(true);
        }
    }

    /**
     * Shows the Switch Account option when the user label is clicked
     */
    @FXML public final void showAccountOptions(Event e) {
        e.consume();
        if (switchAccountButton.isVisible()) {
            switchAccountButton.setVisible(false);
        } else {
            switchAccountButton.setVisible(true);
        }
    }

    /**
     * Switched the selected tab according to the clicked label
     * @param e The trigger event
     */
    @FXML public final void switchTab(Event e) {
        switchTab(e.getSource());
    }

    /**
     * Switched the selected tab according to the source
     * @param source The object that trigger the change
     */
    private void switchTab(Object source) {
        SingleSelectionModel<Tab> selection = contentPane.getSelectionModel();
        Tab oldTab = selection.getSelectedItem();
        if (oldTab == newsTab) {
            newsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == optimizeTab) {
            optimizeLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == skinsTab) {
            skinsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == settingsTab) {
            settingsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == launchOptionsTab && source != profileEditorTab) {
            launchOptionsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == profileEditorTab) {
            //Show play button
            if (!kernel.getDownloader().isDownloading()) {
                playPane.setVisible(true);
            }
            launchOptionsLabel.getStyleClass().remove("selectedItem");
        } else if (oldTab == loginTab) {
            newsLabel.getStyleClass().remove("selectedItem");
            skinsLabel.getStyleClass().remove("selectedItem");
            settingsLabel.getStyleClass().remove("selectedItem");
            launchOptionsLabel.getStyleClass().remove("selectedItem");
        }
        if (source == newsLabel) {
            newsLabel.getStyleClass().add("selectedItem");
            selection.select(newsTab);
        } else if (source == optimizeLabel) {
            optimizeLabel.getStyleClass().add("selectedItem");
            selection.select(optimizeTab);
        } else if (source == skinsLabel) {
            skinsLabel.getStyleClass().add("selectedItem");
            selection.select(skinsTab);
            if (!texturesLoaded) {
                loadTextures();
            }
        } else if (source == settingsLabel) {
            settingsLabel.getStyleClass().add("selectedItem");
            selection.select(settingsTab);
        } else if (source == launchOptionsLabel) {
            launchOptionsLabel.getStyleClass().add("selectedItem");
            selection.select(launchOptionsTab);
            if (!profileListLoaded) {
                loadProfileList();
            }
            profileList.getSelectionModel().clearSelection();
        } else if (source == profileEditorTab) {
            //Hide play button
            playPane.setVisible(false);
            selection.select(profileEditorTab);
        }
    }

    /**
     * Hides any open popup that triggers this method
     * @param e The event trigger
     */
    @FXML public final void hidePopup(Event e) {
        Node ls = (Node)e.getSource();
        if (ls.isVisible()) {
            ls.setVisible(false);
        }
    }

    /**
     * Updates the selected language
     */
    @FXML public final void updateLanguage() {
        if (languagesList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        Label selected = languagesList.getSelectionModel().getSelectedItem();
        languageButton.setText(selected.getText());
        settings.setLocale(selected.getId());
        languagesList.setVisible(false);
        SingleSelectionModel<Tab> selection = contentPane.getSelectionModel();
        Tab selectedTab = selection.getSelectedItem();
        if (selectedTab == launchOptionsTab) {
            loadProfileList();
        } else {
            profileListLoaded = false;
        }
        if (selectedTab == profileEditorTab) {
            loadVersionList();
        } else {
            versionListLoaded = false;
        }
        profileListPopupLoaded = false;
        localizeElements();
    }

    /**
     * Updates the selected icon
     */
    @FXML public final void updateIcon() {
        if (iconList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        ImageView selected = iconList.getSelectionModel().getSelectedItem();
        profileIcon.setImage(selected.getImage());
        profileIcon.setId(selected.getId());
        iconList.setVisible(false);
    }

    /**
     * Prepares the editor with the selected profile or with a new one
     */
    @FXML public final void loadEditor() {
        if (profileList.getSelectionModel().getSelectedIndex() == -1) {
            //Nothing has been selected
            return;
        }
        if (!versionListLoaded) {
            loadVersionList();
        }
        if (profileList.getSelectionModel().getSelectedIndex() == 0) {
            profileName.setEditable(true);
            profileName.setText("");
            deleteButton.setVisible(false);
            versionBlock.setVisible(true);
            versionBlock.setManaged(true);
            iconBlock.setVisible(true);
            iconBlock.setManaged(true);
            versionList.getSelectionModel().select(0);
            profileIcon.setImage(kernel.getProfileIcon("Furnace"));
            profileIcon.setId("Furnace");
            if (settings.getEnableAdvanced()) {
                javaExecBlock.setVisible(true);
                javaExecBlock.setManaged(true);
                javaArgsBlock.setVisible(true);
                javaArgsBlock.setManaged(true);
                ramBlock.setVisible(false);
                ramBlock.setManaged(false);
                toggleEditorOption(javaExecLabel, false);
                javaExec.setText(Utils.getJavaDir());
                toggleEditorOption(javaArgsLabel, false);
                StringBuilder jA = new StringBuilder(15);
                if (Utils.getOSArch() == OSArch.OLD) {
                    jA.append("-Xmx1G");
                } else {
                    jA.append("-Xmx2G");
                }
                jA.append(" -Xmn128M");
                javaArgs.setText(jA.toString());
            } else {
                javaExecBlock.setVisible(false);
                javaExecBlock.setManaged(false);
                javaArgsBlock.setVisible(false);
                javaArgsBlock.setManaged(false);
                ramBlock.setVisible(true);
                ramBlock.setManaged(true);
            }
            toggleEditorOption(resolutionLabel, false);
            resW.setText(String.valueOf(854));
            resH.setText(String.valueOf(480));
            toggleEditorOption(gameDirLabel, false);
            gameDir.setText(Utils.getWorkingDirectory().getAbsolutePath());
        } else {
            Label selectedElement = profileList.getSelectionModel().getSelectedItem();
            if (selectedElement != null) {
                Profile p = kernel.getProfiles().getProfile(selectedElement.getId());
                if (p.getType() != ProfileType.CUSTOM) {
                    profileName.setEditable(false);
                    deleteButton.setVisible(false);
                    if (p.getType() == ProfileType.RELEASE) {
                        profileName.setText(Language.get(59));
                        profileIcon.setImage(kernel.getProfileIcon("Grass"));
                    } else {
                        profileName.setText(Language.get(60));
                        profileIcon.setImage(kernel.getProfileIcon("Crafting_Table"));
                    }
                    versionBlock.setVisible(false);
                    versionBlock.setManaged(false);
                    iconBlock.setVisible(false);
                    iconBlock.setManaged(false);
                    ramBlock.setVisible(false);
                    ramBlock.setVisible(false);
                } else {
                    if (p.hasIcon()) {
                        profileIcon.setImage(kernel.getProfileIcon(p.getIcon()));
                        profileIcon.setId(p.getIcon());
                    } else {
                        profileIcon.setImage(kernel.getProfileIcon("Furnace"));
                    }
                    profileName.setEditable(true);
                    deleteButton.setVisible(true);
                    if (p.hasName()){
                        profileName.setText(p.getName());
                    } else {
                        profileName.setText("");
                    }
                    versionBlock.setVisible(true);
                    versionBlock.setManaged(true);
                    iconBlock.setVisible(true);
                    iconBlock.setManaged(true);
                    ramBlock.setVisible(true);
                    ramBlock.setVisible(true);
                    if (p.hasVersion()) {
                        if (p.isLatestRelease()) {
                            versionList.getSelectionModel().select(0);
                        } else if (p.isLatestSnapshot() && settings.getEnableSnapshots()) {
                            versionList.getSelectionModel().select(1);
                        } else if (versionList.getItems().contains(p.getVersionID())) {
                            versionList.getSelectionModel().select(p.getVersionID());
                        } else {
                            versionList.getSelectionModel().select(0);
                        }
                    } else {
                        versionList.getSelectionModel().select(0);
                    }
                }

                if (p.hasResolution()) {
                    toggleEditorOption(resolutionLabel, true);
                    resH.setText(String.valueOf(p.getResolutionHeight()));
                    resW.setText(String.valueOf(p.getResolutionWidth()));
                } else {
                    toggleEditorOption(resolutionLabel, false);
                    resW.setText(String.valueOf(854));
                    resH.setText(String.valueOf(480));
                }
                if (p.hasGameDir()) {
                    toggleEditorOption(gameDirLabel, true);
                    gameDir.setText(p.getGameDir().getAbsolutePath());
                } else {
                    toggleEditorOption(gameDirLabel, false);
                    gameDir.setText(Utils.getWorkingDirectory().getAbsolutePath());
                }
                if (settings.getEnableAdvanced()) {
                    javaExecBlock.setVisible(true);
                    javaExecBlock.setManaged(true);
                    javaArgsBlock.setVisible(true);
                    javaArgsBlock.setManaged(true);;
                    ramBlock.setVisible(false);
                    ramBlock.setVisible(false);
                    if (p.hasJavaDir()){
                        toggleEditorOption(javaExecLabel, true);
                        javaExec.setText(p.getJavaDir().getAbsolutePath());
                    } else {
                        toggleEditorOption(javaExecLabel, false);
                        javaExec.setText(Utils.getJavaDir());
                    }
                    if (p.hasJavaArgs()) {
                        toggleEditorOption(javaArgsLabel, true);
                        javaArgs.setText(p.getJavaArgs());
                    } else {
                        toggleEditorOption(javaArgsLabel, false);
                        StringBuilder jA = new StringBuilder(15);
                        if (Utils.getOSArch() == OSArch.OLD) {
                            jA.append("-Xmx1G");
                        } else {
                            jA.append("-Xmx2G");
                        }
                        jA.append(" -Xmn128M");
                        javaArgs.setText(jA.toString());
                    }
                } else {
                    javaExecBlock.setVisible(false);
                    javaExecBlock.setManaged(false);
                    javaArgsBlock.setVisible(false);
                    javaArgsBlock.setManaged(false);;
                    ramBlock.setVisible(true);
                    ramBlock.setVisible(true);

                    try {
                        String[] value = p.getJavaArgs().replace("-Xmx", "").split("M");
                        if (value[0].toCharArray().length > 4) {
                            value = p.getJavaArgs().replace("-Xmx", "").split("G");
                            Double result = Double.valueOf(value[0] + "000") + Double.valueOf(value[0]) * 24;
                            profileRam.setValue(result);
                            profileRamAssignateLabel.setText(String.valueOf(result).replace(".0", "") + " MB");
                        } else {
                            profileRam.setValue(Double.valueOf(value[0]));
                            profileRamAssignateLabel.setText(value[0] + " MB");
                        }
                    } catch (Exception ex){
                        console.print("No hay RAM Asignada. Asignacion Automatica...");
                    }
                }
            }
        }
        switchTab(profileEditorTab);
    }

    /**
     * Loads the list of version for the profile editor
     */
    private void loadVersionList() {
        console.print("Cargando Lista de Versiones...");
        ObservableList<VersionMeta> vers = FXCollections.observableArrayList();
        VersionMeta latestVersion = new VersionMeta(Language.get(59), null, null);
        vers.add(latestVersion);
        if (settings.getEnableSnapshots()) {
            VersionMeta latestSnapshot = new VersionMeta(Language.get(60), null, null);
            vers.add(latestSnapshot);
        }
        for (VersionMeta v : kernel.getVersions().getVersions()) {
            if (v.getType() == VersionType.RELEASE) {
                vers.add(v);
            } else if (v.getType() == VersionType.SNAPSHOT && settings.getEnableSnapshots()) {
                vers.add(v);
            } else if ((v.getType() == VersionType.OLD_BETA || v.getType() == VersionType.OLD_ALPHA) && settings.getEnableHistorical()) {
                vers.add(v);
            }
        }

        versionList.setItems(vers);
        if (!versionListLoaded) {
            versionList.getSelectionModel().select(0);
            versionListLoaded = true;
        }
        console.print("Version list loaded.");
    }

    /**
     * Saves the profile data from the profile editor
     */
    @FXML public final void saveProfile() {
        Profile target;
        if (profileList.getSelectionModel().getSelectedIndex() == 0) {
            target = new Profile(ProfileType.CUSTOM);
            kernel.getProfiles().addProfile(target);
        } else {
            Label selectedElement = profileList.getSelectionModel().getSelectedItem();
            target = kernel.getProfiles().getProfile(selectedElement.getId());
        }
        if (target.getType() == ProfileType.CUSTOM) {
            if (!profileName.getText().isEmpty()) {
                target.setName(profileName.getText());
            } else {
                target.setName(null);
            }
            if (versionList.getSelectionModel().getSelectedIndex() == 0) {
                target.setVersionID(kernel.getVersions().getLatestRelease());
                target.setLatestRelease(true);
                target.setLatestSnapshot(false);
            } else if (versionList.getSelectionModel().getSelectedIndex() == 1 && settings.getEnableSnapshots()) {
                target.setVersionID(kernel.getVersions().getLatestSnapshot());
                target.setLatestRelease(false);
                target.setLatestSnapshot(true);
            } else {
                target.setVersionID(versionList.getSelectionModel().getSelectedItem());
                target.setLatestRelease(false);
                target.setLatestSnapshot(false);
            }
            try {
                target.setIcon(profileIcon.getId());
            } catch (IllegalArgumentException ex) {
                target.setIcon(null);
            }

            if (!settings.getEnableAdvanced()) {
                int value = (int) profileRam.getValue();
                target.setJavaArgs("-Xmx"+ value +"M -Xmn128M");
            }
        }
        if (!resW.isDisabled()) {
            try {
                int h = Integer.parseInt(resH.getText());
                int w = Integer.parseInt(resW.getText());
                target.setResolution(w, h);
            } catch (NumberFormatException ex) {
                console.print("Invalid resolution given.");
            }
        } else {
            target.setResolution(-1, -1);
        }
        if (!gameDir.isDisabled() && !gameDir.getText().isEmpty()) {
            target.setGameDir(new File(gameDir.getText()));
        } else {
            target.setGameDir(null);
        }
        if (settings.getEnableAdvanced()) {
            if (!javaExec.isDisabled() && !javaExec.getText().isEmpty()) {
                target.setJavaDir(new File(javaExec.getText()));
            } else {
                target.setJavaDir(null);
            }
            if (!javaArgs.isDisabled() && !javaArgs.getText().isEmpty()) {
                target.setJavaArgs(javaArgs.getText());
            } else {
                target.setJavaArgs(null);
            }
        }
        kernel.saveProfiles();
        if (kernel.getProfiles().getSelectedProfile() == target) {
            updateGameVersion();
        }
        kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(57));
        profileListLoaded = false;
        profileListPopupLoaded = false;
        switchTab(launchOptionsLabel);
    }

    /**
     * Discards the changes of the profile editor
     */
    @FXML public final void cancelProfile() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(55));
        if (result == 1) {
            switchTab(launchOptionsLabel);
        }
    }

    /**
     * Deletes the profile loaded by the profile editor
     */
    @FXML public final void deleteProfile() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(61));
        if (result == 1) {
            Label selectedElement = profileList.getSelectionModel().getSelectedItem();
            Profile p = kernel.getProfiles().getProfile(selectedElement.getId());
            if (kernel.getProfiles().deleteProfile(p)) {
                kernel.saveProfiles();
                updateGameVersion();
                kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(56));
            } else {
                kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(58));
            }
            loadProfileList();
            switchTab(launchOptionsLabel);
        }
    }

    /**
     * Toggles the editor options on and off
     * @param src The object that has been clicked
     * @param newState The new state
     */
    private void toggleEditorOption(Object src, boolean newState) {
        if (src instanceof Label) {
            Label l = (Label)src;
            toggleLabel(l, newState);
        }
        if (src == resolutionLabel) {
            resW.setDisable(!newState);
            resH.setDisable(!newState);
        } else if (src == gameDirLabel) {
            gameDir.setDisable(!newState);
        } else if (src == javaExecLabel) {
            javaExec.setDisable(!newState);
        } else if (src == javaArgsLabel) {
            javaArgs.setDisable(!newState);
        }
    }

    /**
     * Update editor when clicking labels. This method fetches the adjacent sibling to determine if is disabled
     * @param e The event trigger
     */
    @FXML public final void updateEditor(MouseEvent e) {
        Label l = (Label)e.getSource();
        toggleEditorOption(l, l.getParent().getChildrenUnmodifiable().get(1).isDisable());
    }

    /**
     * Updates the existing users list
     */
    private void updateExistingUsers() {
        Authentication a = kernel.getAuthentication();
        if (!a.getUsers().isEmpty() && a.getSelectedUser() == null) {
            existingPanel.setVisible(true);
            existingPanel.setManaged(true);
            ObservableList<User> users = FXCollections.observableArrayList();
            Set<User> us = a.getUsers();
            users.addAll(us);
            existingUsers.setItems(users);
            existingUsers.getSelectionModel().select(0);
        } else {
            existingPanel.setVisible(false);
            existingPanel.setManaged(false);
        }
    }

    /**
     * Shows or hides the login prompt
     * @param showLoginPrompt The new state
     */
    private void showLoginPrompt(boolean showLoginPrompt) {
        if (showLoginPrompt) {
            contentPane.getSelectionModel().select(loginTab);
            tabMenu.setVisible(false);
            tabMenu.setManaged(false);
            accountButton.setVisible(false);
            playPane.setVisible(false);
            updateExistingUsers();
        } else {
            switchTab(newsLabel);
            tabMenu.setVisible(true);
            tabMenu.setManaged(true);
            accountButton.setVisible(true);
            //Show play button
            if (!kernel.getDownloader().isDownloading()) {
                playPane.setVisible(true);
            }
            //Set account name for current user
            accountButton.setText(kernel.getAuthentication().getSelectedUser().getDisplayName());
        }
    }

    /**
     * Performs an authenticate with the data typed in the login form
     */
    public final void authenticate() {
        if (username.getText().isEmpty()) {
            kernel.showAlert(Alert.AlertType.WARNING, null, Language.get(17));
        } else if (password.getText().isEmpty() && authLeverage.isSelected()) {
            kernel.showAlert(Alert.AlertType.WARNING, null, Language.get(23));
        } else {
            try {
                Authentication auth = kernel.getAuthentication();
                String user;
                if (authLeverage.isSelected()) {
                    user = "leverage://" + username.getText();
                    auth.authenticate(user, password.getText());
                } else {
                    user = username.getText();
                    auth.authenticate(user, null);
                }
                kernel.saveProfiles();
                username.setText("");
                password.setText("");
                showLoginPrompt(false);
                fetchAds();
                texturesLoaded = false;
            } catch (AuthenticationException ex) {
                kernel.showAlert(Alert.AlertType.ERROR, Language.get(22), ex.getMessage());
                password.setText("");
            }/* catch (IOException e) {
                console.print("Error de Conexion al Servidor con RCON");
                kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(139));
                e.printStackTrace();
            }*/
        }
    }

    /**
     * Refreshes latest session
     */
    private void refreshSession() {
        console.print("Refrescando Session...");
        Authentication a = kernel.getAuthentication();
        User u = a.getSelectedUser();
        try {
            if (u != null) {
                a.refresh();
                texturesLoaded = false;
                kernel.saveProfiles();
                console.print("Session refrescada.");
            } else {
                console.print("No hay un usuario seleccionado.");
            }
        } catch (AuthenticationException ex) {
            if (u.getType() == UserType.LEVERAGE) {
                authLeverage.setSelected(true);
                username.setText(u.getUsername().replace("leverage://", ""));
            } else {
                authOffline.setSelected(true);
                username.setText(u.getUsername());
            }
            console.print("No se pudo refrescar la Session.");
        }/* catch (IOException e) {
            console.print("Error de Conexion al Servidor con RCON");
            kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(139));
            e.printStackTrace();
        }*/ finally {
            if (a.isAuthenticated()) {
                showLoginPrompt(false);
                fetchAds();
            } else {
                showLoginPrompt(true);
            }
        }
    }

    /**
     * Refreshes user selected from the existing user list
     */
    public final void refresh() {
        User selected = existingUsers.getSelectionModel().getSelectedItem();
        Authentication auth = kernel.getAuthentication();
        try {
            auth.setSelectedUser(selected);
            auth.refresh();
            kernel.saveProfiles();
            texturesLoaded = false;
            showLoginPrompt(false);
            fetchAds();
        } catch (AuthenticationException ex) {
            kernel.showAlert(Alert.AlertType.ERROR, Language.get(62), ex.getMessage());
            updateExistingUsers();
        }/* catch (IOException e) {
            console.print("Error de Conexion al Servidor con RCON");
            kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(139));
            e.printStackTrace();
        }*/
        onlineList();
    }

    /**
     * Logs out the selected user from the existing user list
     */
    public final void logout() {
        User selected = existingUsers.getSelectionModel().getSelectedItem();
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(8));
        if (result == 1) {
            Authentication auth = kernel.getAuthentication();
            auth.removeUser(selected);
            kernel.saveProfiles();
            kernel.closeWeb();
            updateExistingUsers();
        }
        onlineList();
    }

    /**
     * Opens the register page
     */
    @FXML public final void register() {
        if (authLeverage.isSelected()) {
            kernel.getHostServices().showDocument(Urls.register);
        }
    }

    /**
     * Opens the help page
     */
    @FXML public final void openHelp() {
        kernel.getHostServices().showDocument(Urls.help);
    }

    /**
     * Opens the news page
     */
    @FXML public final void openNews() {
        kernel.getHostServices().showDocument(Urls.leverage);
    }

    /**
     * Opens the list Server page
     */
    @FXML public final void openListServer() {
        kernel.getHostServices().showDocument(Urls.listOnline);
    }

    /**
     * Performs an authenticate if the Enter key is pressed in the Username or Password field
     * @param e The trigger event
     */
    @FXML public final void triggerAuthenticate(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            authenticate();
        }
    }

    /**
     * Updates the settings according to the label clicked
     * @param e The trigger event
     */
    @FXML public final void updateSettings(MouseEvent e) {
        Label source = (Label)e.getSource();
        if (source == keepLauncherOpen) {
            settings.setKeepLauncherOpen(!settings.getKeepLauncherOpen());
            toggleLabel(source, settings.getKeepLauncherOpen());
        } else if (source == outputLog) {
            settings.setShowGameLog(!settings.getShowGameLog());
            toggleLabel(source, settings.getShowGameLog());
        } else if (source == enableSnapshots) {
            if (!settings.getEnableSnapshots()) {
                kernel.showAlert(Alert.AlertType.WARNING, null, Language.get(71) + System.lineSeparator() + Language.get(72));
            }
            settings.setEnableSnapshots(!settings.getEnableSnapshots());
            toggleLabel(source, settings.getEnableSnapshots());
            validateSelectedProfile();
            loadProfileList();
            versionListLoaded = false;
        } else if (source == enableReopen) {
            settings.setEnableReopen(!settings.getEnableReopen());
            toggleLabel(source, settings.getEnableReopen());
        } else if (source == historicalVersions) {
            if (!settings.getEnableHistorical()) {
                kernel.showAlert(Alert.AlertType.WARNING, null, Language.get(73) + System.lineSeparator()
                        + Language.get(74) + System.lineSeparator()
                        + Language.get(75));

            }
            settings.setEnableHistorical(!settings.getEnableHistorical());
            toggleLabel(source, settings.getEnableHistorical());
            validateSelectedProfile();
            loadProfileList();
            versionListLoaded = false;
        } else if (source == advancedSettings) {
            if (!settings.getEnableAdvanced()) {
                kernel.showAlert(Alert.AlertType.WARNING, null, Language.get(76) + System.lineSeparator() + Language.get(77));
            }
            settings.setEnableAdvanced(!settings.getEnableAdvanced());
            toggleLabel(source, settings.getEnableAdvanced());
        }
        kernel.saveProfiles();
    }

    /**
     * Changes any label icon
     * @param label The target label
     * @param state The new state
     */
    private void toggleLabel(Styleable label, boolean state) {
        Object[] classes = label.getStyleClass().toArray();
        for (Object ckl : classes) {
            if (ckl.toString().startsWith("toggle")) {
                label.getStyleClass().remove(ckl.toString());
            }
        }
        if (state) {
            label.getStyleClass().add("toggle-enabled");
        } else {
            label.getStyleClass().add("toggle-disabled");
        }
    }

    /**
     * Exports the logs to a ZIP file
     */
    @FXML private void exportLogs() {
        File selected = selectFile("ZIP", "*.zip", "save");
        if (selected != null) {
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(selected))) {
                File[] files = Kernel.APPLICATION_LOGS.listFiles();
                if (files != null) {
                    byte[] bytes;
                    for (File file : files) {
                        ZipEntry entry = new ZipEntry(file.getName());
                        out.putNextEntry(entry);
                        bytes = Files.readAllBytes(file.toPath());
                        out.write(bytes);
                        out.closeEntry();
                    }
                }
                kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(35) + System.lineSeparator() + selected.getAbsolutePath());
            } catch (IOException ex) {
                kernel.showAlert(Alert.AlertType.ERROR, null, Language.get(35) + '\n' + selected.getAbsolutePath());
            }
        }
    }

    /**
     * Hide/Show Password Field in Login
     */
    @FXML private void hideShowPasswordField() {
        if(authLeverage.isSelected()) {
            passwordLabel.setVisible(true);
            password.setVisible(true);
        } else {
            passwordLabel.setVisible(false);
            password.setVisible(false);
        }
    }

    /**
     * Change Name Ram Assignate Slider
     */
    @FXML private void sliderChanged() {
        int value = (int) profileRam.getValue();
        profileRamAssignateLabel.setText(String.valueOf(value) + " MB");
    }

    /**
     * Switch List in ControlTab
     */
    @FXML private void switchControlTab() {
        //Action Bottom Control
        System.out.println("TOKE");
    }

    /**
     * Removes the launcher cache
     */
    @FXML private void deleteCache() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(10));
        if (result == 1) {
            File[] files = Kernel.APPLICATION_CACHE.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(11));
        }
    }

    /**
     * Removes the launcher cache
     */
    @FXML private void deleteLogs() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(10));
        if (result == 1) {
            String home = Kernel.APPLICATION_WORKING_DIR.getPath();
            Utils.deleteDirectory(new File(home+"/logs"));
            kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(114));
        }
    }

    /**
     * Removes the launcher cache
     */
    @FXML private void deleteCrash() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(10));
        if (result == 1) {
            String home = Kernel.APPLICATION_WORKING_DIR.getPath();
            Utils.deleteDirectory(new File(home+"/crash-reports"));
            kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(115));
        }
    }

    /**
     * Removes the launcher cache
     */
    @FXML private void deleteSave() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(10));
        if (result == 1) {
            String home = Kernel.APPLICATION_WORKING_DIR.getPath();
            Utils.deleteDirectory(new File(home+"/saves"));
            kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(119));
        }
    }

    /**
     * Removes the launcher cache
     */
    @FXML private void deleteConfig() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(10));
        if (result == 1) {
            String home = Kernel.APPLICATION_WORKING_DIR.getPath();
            Utils.deleteDirectory(new File(home+"/config"));
            Utils.deleteDirectory(new File(home+"/journeymap"));
            Utils.deleteDirectory(new File(home+"/customnpcs"));
            Utils.deleteDirectory(new File(home+"/astralsorcery"));
            File delete = new File(home+"/xaerominimap.txt");
            delete.delete();
            kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(116));
        }
    }

    /**
     * Removes the launcher cache
     */
    @FXML private void deleteShader() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(10));
        if (result == 1) {
            String home = Kernel.APPLICATION_WORKING_DIR.getPath();
            System.out.println(home);
            Utils.deleteDirectory(new File(home+"/shaderpacks"));
            kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(120));
        }
    }

    /**
     * Removes the launcher cache
     */
    @FXML private void deleteResources() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(10));
        if (result == 1) {
            String home = Kernel.APPLICATION_WORKING_DIR.getPath();
            Utils.deleteDirectory(new File(home+"/resourcepacks"));
            kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(118));
        }
    }

    /**
     * Removes the launcher cache
     */
    @FXML private void deleteExtra() {
        int result = kernel.showAlert(Alert.AlertType.CONFIRMATION, null, Language.get(10));
        if (result == 1) {
            String home = Kernel.APPLICATION_WORKING_DIR.getPath();
            Utils.deleteDirectory(new File(home+"/backups"));
            Utils.deleteDirectory(new File(home+"/server-resource-packs"));
            Utils.deleteDirectory(new File(home+"/playerskinsWhitelist"));
            Utils.deleteDirectory(new File(home+"/CustomDISkins"));
            File delete;
            String[] files = {"usernamecache.json", "usercache.json", "InnerStateServer-1.1.json"};
            for(int i=0; i<files.length; i++) {
                delete = new File(home+"/"+files[i]);
                delete.delete();
            }
            kernel.showAlert(Alert.AlertType.INFORMATION, null, Language.get(117));
        }
    }

    /**
     * Selects a game directory for the profile editor
     */
    @FXML private void selectGameDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        if (gameDir.getText().isEmpty()) {
            chooser.setInitialDirectory(Kernel.APPLICATION_WORKING_DIR);
        } else {
            File gd = new File(gameDir.getText());
            if (gd.isDirectory()) {
                chooser.setInitialDirectory(gd);
            } else {
                chooser.setInitialDirectory(Kernel.APPLICATION_WORKING_DIR);
            }
        }
        File selectedFolder = chooser.showDialog(null);
        if (selectedFolder != null) {
            gameDir.setText(selectedFolder.getAbsolutePath());
        }
    }

    /**
     * Selects the java executable for the profile editor
     */
    @FXML private void selectJavaExecutable() {
        File selected = selectFile(null, null, "open");
        if (selected != null && selected.isFile()) {
            javaExec.setText(selected.getAbsolutePath());
        }
    }

    /**
     * Update auth server label on existing users
     */
    @FXML public void updateAuthServer() {
        User user = existingUsers.getValue();
        if (user != null) {
            if (user.getType() == UserType.LEVERAGE) {
                authServer.setText("(LEVERAGE)");
            } else {
                authServer.setText("(OFFLINE)");
            }
        }
    }

    /**
     * Selects a file
     * @param extensionName The extension name
     * @param extension The extension
     * @param method Method to select the file
     * @return The selected file
     */
    private File selectFile(String extensionName, String extension, String method) {
        FileChooser chooser = new FileChooser();
        if (extension != null) {
            ExtensionFilter filter = new ExtensionFilter(extensionName, extension);
            chooser.getExtensionFilters().add(filter);
        }
        if (method.equalsIgnoreCase("open")) {
            chooser.setTitle(Language.get(95));
            return chooser.showOpenDialog(stage);
        } else if (method.equalsIgnoreCase("save")) {
            chooser.setTitle(Language.get(96));
            return chooser.showSaveDialog(stage);
        }
        return null;
    }

    /**
     * Opens the password recovery webpage
     */
    @FXML private void forgotPassword() {
        if (authLeverage.isSelected()) {
            kernel.getHostServices().showDocument(Urls.forgotPassword);
        }
        return;
    }
}
