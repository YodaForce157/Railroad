package io.github.railroad.project.ui.project.newProject.details;

import io.github.railroad.minecraft.FabricAPIVersion;
import io.github.railroad.minecraft.FabricLoaderVersion;
import io.github.railroad.minecraft.MinecraftVersion;
import io.github.railroad.minecraft.RecommendableVersion;
import io.github.railroad.minecraft.mapping.MappingChannel;
import io.github.railroad.minecraft.mapping.MappingHelper;
import io.github.railroad.minecraft.mapping.MappingVersion;
import io.github.railroad.project.License;
import io.github.railroad.project.ProjectType;
import io.github.railroad.project.data.FabricProjectData;
import io.github.railroad.project.ui.BrowseButton;
import io.github.railroad.project.ui.project.newProject.StarableListCell;
import io.github.railroad.ui.defaults.RRHBox;
import io.github.railroad.ui.defaults.RRVBox;
import io.github.railroad.utility.ClassNameValidator;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class FabricProjectDetailsPane extends RRVBox {
    private final TextField projectNameField = new TextField();
    private final TextField projectPathField = new TextField();
    private final CheckBox createGitCheckBox = new CheckBox();
    private final ComboBox<License> licenseComboBox = new ComboBox<>();
    private final TextField licenseCustomField = new TextField();

    private final ComboBox<MinecraftVersion> minecraftVersionComboBox = new ComboBox<>();
    private final ComboBox<FabricLoaderVersion> fabricLoaderVersionComboBox = new ComboBox<>();
    private final CheckBox includeFapiCheckBox = new CheckBox();
    private final ComboBox<FabricAPIVersion> fapiVersionComboBox = new ComboBox<>();
    private final TextField modIdField = new TextField();
    private final TextField modNameField = new TextField();
    private final TextField mainClassField = new TextField();
    private final CheckBox useAccessWidenerCheckBox = new CheckBox();
    private final CheckBox splitSourcesCheckBox = new CheckBox();

    private final ComboBox<MappingChannel> mappingChannelComboBox = new ComboBox<>();
    private final ComboBox<MappingVersion> mappingVersionComboBox = new ComboBox<>();

    private final TextField authorField = new TextField(System.getProperty("user.name")); // optional
    private final TextArea descriptionArea = new TextArea(); // optional
    private final TextField issuesField = new TextField(); // optional

    private final TextField groupIdField = new TextField();
    private final TextField artifactIdField = new TextField();
    private final TextField versionField = new TextField();

    private final AtomicBoolean hasOneDriveWarning = new AtomicBoolean(false);
    private final AtomicBoolean hasModidWarning = new AtomicBoolean(false);
    private final AtomicBoolean hasTypedInModid = new AtomicBoolean(false);
    private final AtomicBoolean hasTypedInModName = new AtomicBoolean(false);
    private final AtomicBoolean hasTypedInMainClass = new AtomicBoolean(false);
    private final AtomicBoolean hasTypedInArtifactId = new AtomicBoolean(false);
    
    public FabricProjectDetailsPane() {
        // Project Section
        var projectSection = new RRVBox(10);

        var projectNameBox = new RRHBox(10);
        projectNameBox.setAlignment(Pos.CENTER_LEFT);
        var projectNameLabel = new Label("Name:");
        projectNameLabel.setLabelFor(projectNameField);
        projectNameBox.getChildren().addAll(projectNameLabel, projectNameField);

        var projectPathVBox = new RRVBox(10);
        projectPathVBox.setAlignment(Pos.CENTER_LEFT);

        var createdAtLabel = new Label("This will be created at: " + System.getProperty("user.home"));
        createdAtLabel.setGraphic(new FontIcon(FontAwesomeSolid.INFO_CIRCLE));
        createdAtLabel.setTooltip(new Tooltip("The project will be created in this directory."));
        createdAtLabel.setTextFill(Color.SLATEGRAY);

        var projectPathBox = new RRHBox(10);
        projectPathBox.setAlignment(Pos.CENTER_LEFT);
        var projectPathLabel = new Label("Location:");
        projectPathLabel.setLabelFor(projectPathField);
        projectPathField.setPrefWidth(300);
        projectPathField.setText(System.getProperty("user.home"));
        projectPathField.setEditable(false);
        Border projectPathFieldBorder = projectPathField.getBorder();
        projectPathField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Validate the project path
            Path path = Path.of(newValue);
            if (Files.notExists(path) || !Files.isDirectory(path))
                projectPathField.setStyle("-fx-border-color: red;");
            else
                projectPathField.setBorder(projectPathFieldBorder);

            // Update the created at label
            String fullPath = fixPath(projectPathField.getText().trim() + "/" + projectNameField.getText().trim());
            createdAtLabel.setText("This will be created at: " + fullPath);

            // If the project is in OneDrive, warn the user
            if (fullPath.contains("OneDrive") && hasOneDriveWarning.compareAndSet(false, true)) {
                projectPathField.setStyle("-fx-border-color: orange;");

                var tooltip = new Tooltip("It is not recommended to create projects in OneDrive as it has a tendency to cause problems.");
                Tooltip.install(projectPathField, tooltip);

                var warningIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
                warningIcon.setIconSize(16);
                warningIcon.setIconColor(Color.ORANGE);
                projectPathBox.getChildren().add(warningIcon);
            } else if (!fullPath.contains("OneDrive") && hasOneDriveWarning.compareAndSet(true, false)) {
                projectPathField.setBorder(projectPathFieldBorder);

                Tooltip.uninstall(projectPathField, projectPathField.getTooltip());

                projectPathBox.getChildren().removeLast();
            } else if (fullPath.contains("OneDrive")) {
                projectPathField.setStyle("-fx-border-color: orange;");
            } else {
                projectPathField.setBorder(projectPathFieldBorder);
            }
        });

        var browseButtonIcon = new FontIcon(FontAwesomeSolid.FOLDER_OPEN);
        browseButtonIcon.setIconSize(16);
        browseButtonIcon.setIconColor(Color.CADETBLUE);
        var browseButton = new BrowseButton();
        browseButton.parentWindowProperty().bind(sceneProperty().map(Scene::getWindow));
        browseButton.textFieldProperty().set(projectPathField);
        browseButton.browseTypeProperty().set(BrowseButton.BrowseType.DIRECTORY);
        browseButton.setGraphic(browseButtonIcon);
        browseButton.setTooltip(new Tooltip("Browse"));
        projectPathBox.getChildren().addAll(projectPathLabel, projectPathField, browseButton);

        projectPathVBox.getChildren().addAll(projectPathBox, createdAtLabel);

        var gitBox = new RRHBox(10);
        gitBox.setAlignment(Pos.CENTER_LEFT);
        var createGitLabel = new Label("Create Git Repository:");
        createGitLabel.setLabelFor(createGitCheckBox);
        gitBox.getChildren().addAll(createGitLabel, createGitCheckBox);

        var licenseVBox = new RRVBox(10);
        licenseVBox.setAlignment(Pos.CENTER_LEFT);
        var licenseBox = new RRHBox(10);
        licenseBox.setAlignment(Pos.CENTER_LEFT);
        var licenseLabel = new Label("License:");
        licenseLabel.setLabelFor(licenseComboBox);
        licenseComboBox.getItems().addAll(License.values());
        licenseComboBox.setValue(License.MIT);
        licenseComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(License object) {
                return object.getName();
            }

            @Override
            public License fromString(String string) {
                return License.fromName(string);
            }
        });
        licenseComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == License.CUSTOM) {
                licenseVBox.getChildren().add(licenseCustomField);
            } else {
                licenseVBox.getChildren().remove(licenseCustomField);
            }
        });
        licenseBox.getChildren().addAll(licenseLabel, licenseComboBox);
        licenseVBox.getChildren().add(licenseBox);

        projectSection.getChildren().addAll(projectNameBox, projectPathVBox, gitBox, licenseVBox);

        // Minecraft Section
        var minecraftSection = new RRVBox(10);
        minecraftSection.setAlignment(Pos.CENTER_LEFT);

        var minecraftVersionBox = new RRHBox(10);
        minecraftVersionBox.setAlignment(Pos.CENTER_LEFT);
        var minecraftVersionLabel = new Label("Minecraft Version:");
        minecraftVersionLabel.setLabelFor(minecraftVersionComboBox);
        minecraftVersionComboBox.getItems().addAll(MinecraftVersion.getSupportedVersions(ProjectType.FORGE));
        minecraftVersionComboBox.setValue(MinecraftVersion.getLatestStableVersion());
        minecraftVersionComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MinecraftVersion object) {
                return object.id();
            }

            @Override
            public MinecraftVersion fromString(String string) {
                return MinecraftVersion.fromId(string).orElse(null);
            }
        });
        minecraftVersionBox.getChildren().addAll(minecraftVersionLabel, minecraftVersionComboBox);

        var fabricLoaderVersionBox = new RRHBox(10);
        fabricLoaderVersionBox.setAlignment(Pos.CENTER_LEFT);
        var fabricLoaderVersionLabel = new Label("Loader Version:");
        fabricLoaderVersionLabel.setLabelFor(fabricLoaderVersionComboBox);
        fabricLoaderVersionComboBox.setCellFactory(param -> new StarableListCell<>(
                version -> Objects.equals(version.loaderVersion().version(), FabricLoaderVersion.getLatestVersion(minecraftVersionComboBox.getValue()).loaderVersion().version()),
                version -> false,
                fabricLoaderVersion -> fabricLoaderVersion.loaderVersion().version()));
        fabricLoaderVersionComboBox.setButtonCell(new StarableListCell<>(
                version -> Objects.equals(version.loaderVersion().version(), FabricLoaderVersion.getLatestVersion(minecraftVersionComboBox.getValue()).loaderVersion().version()),
                version -> false,
                fabricLoaderVersion -> fabricLoaderVersion.loaderVersion().version()));
        fabricLoaderVersionComboBox.getItems().addAll(FabricLoaderVersion.getVersions(MinecraftVersion.getLatestStableVersion()));
        fabricLoaderVersionComboBox.setValue(FabricLoaderVersion.getLatestVersion(MinecraftVersion.getLatestStableVersion()));
        fabricLoaderVersionBox.getChildren().addAll(fabricLoaderVersionLabel, fabricLoaderVersionComboBox);

        var fapiBox = new RRVBox(10);
        fapiBox.setAlignment(Pos.CENTER_LEFT);
        var includeFapiBox = new RRHBox(10);
        includeFapiBox.setAlignment(Pos.CENTER_LEFT);
        var includeFapiLabel = new Label("Include Fabric API:");
        includeFapiLabel.setLabelFor(includeFapiCheckBox);
        includeFapiCheckBox.setSelected(true);
        var fapiVersionBox = new RRHBox(10);
        fapiVersionBox.setAlignment(Pos.CENTER_LEFT);
        var fapiVersionLabel = new Label("Fabric API Version:");
        fapiVersionLabel.setLabelFor(fapiVersionComboBox);
        fapiVersionComboBox.setCellFactory(param -> new StarableListCell<>(
                version -> Objects.equals(version, FabricAPIVersion.getLatest()),
                version -> false,
                FabricAPIVersion::version));
        fapiVersionComboBox.setButtonCell(new StarableListCell<>(
                version -> Objects.equals(version, FabricAPIVersion.getLatest()),
                version -> false,
                FabricAPIVersion::version));
        fapiVersionComboBox.getItems().addAll(FabricAPIVersion.getVersions(MinecraftVersion.getLatestStableVersion()));
        fapiVersionComboBox.setValue(FabricAPIVersion.getLatest());
        includeFapiBox.getChildren().addAll(includeFapiLabel, includeFapiCheckBox);
        fapiVersionBox.getChildren().addAll(fapiVersionLabel, fapiVersionComboBox);
        fapiBox.getChildren().addAll(includeFapiBox, fapiVersionBox);

        includeFapiCheckBox.setOnAction(event -> {
            if(fapiBox.getChildren().contains(fapiVersionBox)) {
                fapiBox.getChildren().remove(fapiVersionBox);
            } else {
                fapiBox.getChildren().add(fapiVersionBox);
            }
        });

        minecraftVersionComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            fabricLoaderVersionComboBox.getItems().setAll(FabricLoaderVersion.getVersions(newValue));
            fabricLoaderVersionComboBox.setValue(FabricLoaderVersion.getLatestVersion(newValue));

            MappingHelper.loadMappings(mappingChannelComboBox.getItems(), newValue);
            mappingChannelComboBox.setValue(mappingChannelComboBox.getItems().getFirst());

            fapiVersionComboBox.getItems().setAll(FabricAPIVersion.getVersions(newValue));
            fapiVersionComboBox.setValue(FabricAPIVersion.getLatest());
        });

        var modIdBox = new RRHBox(10);
        modIdBox.setAlignment(Pos.CENTER_LEFT);
        var modIdLabel = new Label("Mod ID:");
        modIdLabel.setLabelFor(modIdField);
        Border modidFieldBorder = modIdField.getBorder();
        modIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.equals(newValue))
                return;

            if (!newValue.matches("[a-z][a-z0-9_]")) {
                modIdField.setText(newValue = newValue.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", ""));
            }

            // Only allow a maximum of 64 characters
            if (newValue.length() > 64) {
                modIdField.setText(newValue = newValue.substring(0, 64));
            }

            // Validate the mod ID
            if (newValue.length() < 3 || !newValue.matches("^[a-z][a-z0-9_]{1,63}$"))
                modIdField.setStyle("-fx-border-color: red;");
            else
                modIdField.setBorder(modidFieldBorder);

            // If the mod ID is not valid, show a warning
            if ((newValue.length() < 5 && newValue.length() > 2) && hasModidWarning.compareAndSet(false, true)) {
                modIdField.setStyle("-fx-border-color: orange;");

                var tooltip = new Tooltip("Short mod IDs are discouraged as they may conflict with other mods.");
                Tooltip.install(modIdField, tooltip);

                var warningIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
                warningIcon.setIconSize(16);
                warningIcon.setIconColor(Color.ORANGE);
                modIdBox.getChildren().add(warningIcon);
            } else if (newValue.length() >= 5 && hasModidWarning.compareAndSet(true, false)) {
                modIdField.setBorder(modidFieldBorder);

                Tooltip.uninstall(modIdField, modIdField.getTooltip());
                modIdBox.getChildren().removeLast();
            } else if (newValue.length() < 3 && hasModidWarning.compareAndSet(true, false)) {
                Tooltip.uninstall(modIdField, modIdField.getTooltip());
                modIdBox.getChildren().removeLast();
            }

            // update the artifact ID field if it is empty
            if (!hasTypedInArtifactId.get() || artifactIdField.getText().isBlank())
                artifactIdField.setText(newValue.replaceAll("[^a-z0-9-]", ""));
        });
        modIdField.setOnKeyTyped(event -> {
            // If the user has typed in the mod ID and it is not empty, set the hasTypedInModid flag to true
            if (!hasTypedInModid.get() && !modIdField.getText().isBlank())
                hasTypedInModid.set(true);
            else if (hasTypedInModid.get() && modIdField.getText().isBlank())
                hasTypedInModid.set(false);
        });
        modIdBox.getChildren().addAll(modIdLabel, modIdField);

        var modNameBox = new RRHBox(10);
        modNameBox.setAlignment(Pos.CENTER_LEFT);
        var modNameLabel = new Label("Mod Name:");
        modNameLabel.setLabelFor(modNameField);
        modNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 256) {
                modNameField.setText(newValue.substring(0, 256));
            }
        });
        modNameField.setOnKeyTyped(event -> {
            // If the user has typed in the mod name and it is not empty, set the hasTypedInModName flag to true
            if (!hasTypedInModName.get() && !modNameField.getText().isBlank())
                hasTypedInModName.set(true);
            else if (hasTypedInModName.get() && modNameField.getText().isBlank())
                hasTypedInModName.set(false);
        });
        modNameBox.getChildren().addAll(modNameLabel, modNameField);

        var mainClassBox = new RRHBox(10);
        mainClassBox.setAlignment(Pos.CENTER_LEFT);
        var mainClassLabel = new Label("Main Class:");
        mainClassLabel.setLabelFor(mainClassField);
        mainClassField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!ClassNameValidator.isValid(newValue)) {
                mainClassField.setText(oldValue);
            }
        });
        mainClassField.setOnKeyTyped(event -> {
            // If the user has typed in the main class and it is not empty, set the hasTypedInMainClass flag to true
            if (!hasTypedInMainClass.get() && !mainClassField.getText().isBlank())
                hasTypedInMainClass.set(true);
            else if (hasTypedInMainClass.get() && mainClassField.getText().isBlank())
                hasTypedInMainClass.set(false);
        });
        mainClassBox.getChildren().addAll(mainClassLabel, mainClassField);

        var useAccessWidenerBox = new RRHBox(10);
        useAccessWidenerBox.setAlignment(Pos.CENTER_LEFT);
        var useAccessWidenerLabel = new Label("Use Access Widener:");
        useAccessWidenerLabel.setLabelFor(useAccessWidenerCheckBox);
        useAccessWidenerBox.getChildren().addAll(useAccessWidenerLabel, useAccessWidenerCheckBox);

        var splitSourcesBox = new RRHBox(10);
        splitSourcesBox.setAlignment(Pos.CENTER_LEFT);
        var splitSourcesLabel = new Label("Split Sources:");
        splitSourcesLabel.setLabelFor(splitSourcesCheckBox);
        splitSourcesCheckBox.setSelected(true);
        splitSourcesBox.getChildren().addAll(splitSourcesLabel, splitSourcesCheckBox);

        minecraftSection.getChildren().addAll(minecraftVersionBox, fabricLoaderVersionBox, fapiBox,
                modIdBox, modNameBox, mainClassBox, useAccessWidenerBox, splitSourcesBox);

        // Mapping Section
        var mappingSection = new RRVBox(10);
        mappingSection.setAlignment(Pos.CENTER_LEFT);

        var mappingChannelBox = new RRHBox(10);
        mappingChannelBox.setAlignment(Pos.CENTER_LEFT);
        var mappingChannelLabel = new Label("Mapping Channel:");
        mappingChannelLabel.setLabelFor(mappingChannelComboBox);
        MappingHelper.loadMappings(mappingChannelComboBox.getItems(), minecraftVersionComboBox.getValue());
        mappingChannelComboBox.setValue(MappingChannel.MOJMAP);
        mappingChannelComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MappingChannel object) {
                return object == null ? "" : object.getName();
            }

            @Override
            public MappingChannel fromString(String string) {
                return MappingChannel.valueOf(string.toUpperCase(Locale.ROOT));
            }
        });
        mappingChannelComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                return;

            MappingHelper.loadMappingsVersions(mappingVersionComboBox.getItems(), minecraftVersionComboBox.getValue(), newValue);
            mappingVersionComboBox.setValue(mappingVersionComboBox.getItems().getFirst());
        });
        mappingChannelBox.getChildren().addAll(mappingChannelLabel, mappingChannelComboBox);

        var mappingVersionBox = new RRHBox(10);
        mappingVersionBox.setAlignment(Pos.CENTER_LEFT);
        var mappingVersionLabel = new Label("Mapping Version:");
        mappingVersionLabel.setLabelFor(mappingVersionComboBox);
        mappingVersionComboBox.setCellFactory(param -> new StarableListCell<>(
                version -> version instanceof RecommendableVersion recommendableVersion && recommendableVersion.isRecommended(),
                MappingVersion::isLatest,
                MappingVersion::getId));
        mappingVersionComboBox.setButtonCell(new StarableListCell<>(
                version -> version instanceof RecommendableVersion recommendableVersion && recommendableVersion.isRecommended(),
                MappingVersion::isLatest,
                MappingVersion::getId));
        MappingHelper.loadMappingsVersions(mappingVersionComboBox.getItems(), minecraftVersionComboBox.getValue(), mappingChannelComboBox.getValue());
        mappingVersionComboBox.setValue(mappingVersionComboBox.getItems().getFirst());
        mappingVersionBox.getChildren().addAll(mappingVersionLabel, mappingVersionComboBox);

        mappingSection.getChildren().addAll(mappingChannelBox, mappingVersionBox);

        // Optional Section
        var optionalSection = new RRVBox(10);
        optionalSection.setAlignment(Pos.CENTER_LEFT);

        var authorBox = new RRHBox(10);
        authorBox.setAlignment(Pos.CENTER_LEFT);
        var authorLabel = new Label("Author:");
        authorLabel.setLabelFor(authorField);
        authorField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 256) {
                authorField.setText(newValue.substring(0, 256));
            }
        });
        authorBox.getChildren().addAll(authorLabel, authorField);

        var descriptionBox = new RRHBox(10);
        descriptionBox.setAlignment(Pos.CENTER_LEFT);
        var descriptionLabel = new Label("Description:");
        descriptionLabel.setLabelFor(descriptionArea);
        descriptionArea.setPrefHeight(100);
        descriptionArea.setWrapText(true);
        descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1028) {
                descriptionArea.setText(newValue.substring(0, 1028));
            }
        });
        descriptionBox.getChildren().addAll(descriptionLabel, descriptionArea);

        var issuesBox = new RRHBox(10);
        issuesBox.setAlignment(Pos.CENTER_LEFT);
        var issuesLabel = new Label("Issues:");
        issuesLabel.setLabelFor(issuesField);
        issuesBox.getChildren().addAll(issuesLabel, issuesField);

        optionalSection.getChildren().addAll(authorBox, descriptionBox, issuesBox);

        // Maven Section
        var mavenSection = new RRVBox(10);
        mavenSection.setAlignment(Pos.CENTER_LEFT);

        var groupIdBox = new RRHBox(10);
        groupIdBox.setAlignment(Pos.CENTER_LEFT);
        var groupIdLabel = new Label("Group ID:");
        groupIdLabel.setLabelFor(groupIdField);
        groupIdBox.getChildren().addAll(groupIdLabel, groupIdField);

        var artifactIdBox = new RRHBox(10);
        artifactIdBox.setAlignment(Pos.CENTER_LEFT);
        var artifactIdLabel = new Label("Artifact ID:");
        artifactIdLabel.setLabelFor(artifactIdField);
        artifactIdBox.getChildren().addAll(artifactIdLabel, artifactIdField);

        var versionBox = new RRHBox(10);
        versionBox.setAlignment(Pos.CENTER_LEFT);
        var versionLabel = new Label("Version:");
        versionLabel.setLabelFor(versionField);
        versionBox.getChildren().addAll(versionLabel, versionField);

        mavenSection.getChildren().addAll(groupIdBox, artifactIdBox, versionBox);

        getChildren().addAll(projectSection,
                new Separator(), minecraftSection,
                new Separator(), mappingSection,
                new Separator(), optionalSection,
                new Separator(), mavenSection);
        setSpacing(20);
        setPadding(new Insets(20));

        Border projectNameFieldBorder = projectNameField.getBorder();
        projectNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Remove any .<>:"/\|?* characters from the project name
            newValue = newValue.replaceAll("[.<>:\"/\\\\|?*]", "");

            // Check that the name does not exceed 256 characters
            if (newValue.length() > 256)
                projectNameField.setText(newValue.substring(0, 256));

            // Validate the project name
            if (newValue.isBlank())
                projectNameField.setStyle("-fx-border-color: red;");
            else
                projectNameField.setBorder(projectNameFieldBorder);

            // Update the created at label
            String path = fixPath(projectPathField.getText().trim() + "/" + projectNameField.getText().trim());
            createdAtLabel.setText("This will be created at: " + path);

            // Update the mod ID field if it is empty
            if (!hasTypedInModid.get() || modIdField.getText().isBlank())
                modIdField.setText(newValue.toLowerCase(Locale.ROOT).replace(" ", "_").replaceAll("[^a-z0-9_-]", ""));

            // Update the mod name field if it is empty
            if (!hasTypedInModName.get() || modNameField.getText().isBlank())
                modNameField.setText(newValue);

            // Update the main class field if it is empty
            if (!hasTypedInMainClass.get() || mainClassField.getText().isBlank()) {
                // convert to pascal case
                String[] words = newValue.split("[ _-]+");
                var pascalCase = new StringBuilder();
                for (String word : words) {
                    if(word.isBlank())
                        continue;

                    pascalCase.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
                }

                mainClassField.setText(pascalCase.toString().replaceAll("[^a-zA-Z0-9]", ""));
            }

            // Update the artifact ID field if it is empty
            if (!hasTypedInArtifactId.get() || artifactIdField.getText().isBlank())
                artifactIdField.setText(newValue.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", ""));
        });

        var createButton = new Button("Create");
        createButton.setAlignment(Pos.CENTER_RIGHT);
        getChildren().add(createButton);

//        createButton.disableProperty().bind(
//                isInvalid(projectNameField)
//                        .or(isInvalid(projectPathField))
//                        .or(isInvalid(modIdField))
//                        .or(isInvalid(modNameField))
//                        .or(isInvalid(mainClassField))
//                        .or(isInvalid(groupIdField))
//                        .or(isInvalid(artifactIdField))
//                        .or(isInvalid(versionField))
//                        .or(isInvalid(minecraftVersionComboBox))
//                        .or(isInvalid(fabricVersionComboBox))
//                        .or(isInvalid(mappingChannelComboBox))
//                        .or(isInvalid(mappingVersionComboBox))
//                        .or(isInvalid(licenseComboBox)
//                                .or(licenseComboBox.valueProperty().isEqualTo(License.CUSTOM)
//                                        .and(licenseCustomField.textProperty().isEmpty()))));

        createButton.setOnAction(event -> {
            if (validate()) {
                Scene scene = getScene();

                FabricProjectData data = createData();
                scene.setRoot(new FabricProjectCreationPane(data));
                event.consume();
            }
        });
    }

    private static BooleanBinding isInvalid(TextField textField) {
        return textField.textProperty().isEmpty().or(textField.styleProperty().isEqualTo("-fx-border-color: red;"));
    }

    private static BooleanBinding isInvalid(ComboBox<?> comboBox) {
        return comboBox.valueProperty().isNull().or(comboBox.styleProperty().isEqualTo("-fx-border-color: red;"));
    }

    private static void createAlert(String header, String content) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static String fixPath(String path) {
        while (path.endsWith(" "))
            path = path.substring(0, path.length() - 1);

        path = path.replace("/", "\\");

        // Remove trailing backslashes
        while (path.endsWith("\\"))
            path = path.substring(0, path.length() - 1);

        // remove any whitespace before a backslash
        path = path.replaceAll("\\s+\\\\", "\\");

        // remove any whitespace after a backslash
        path = path.replaceAll("\\\\\\\\s+", "\\\\");

        // remove any double backslashes
        path = path.replaceAll("\\\\\\\\", "\\\\");

        // remove any trailing whitespace
        path = path.trim();

        return path;
    }

    protected boolean validate() {
        // Validate the project name
        if (projectNameField.getText().isBlank()) {
            projectNameField.setStyle("-fx-border-color: red;");
            projectNameField.requestFocus();

            createAlert("Project Name is Required", "Please enter a name for your project.");
            return false;
        }

        // Validate the project path
        if (projectPathField.getText().isBlank()) {
            projectPathField.setStyle("-fx-border-color: red;");
            projectPathField.requestFocus();

            createAlert("Project Path is Required", "Please enter a path for your project.");
            return false;
        }

        Path path = Path.of(projectPathField.getText());
        if (Files.notExists(path)) {
            projectPathField.setStyle("-fx-border-color: red;");
            projectPathField.requestFocus();

            createAlert("Invalid Project Path", "The specified path does not exist.");
            return false;
        }

        if (!Files.isDirectory(path)) {
            projectPathField.setStyle("-fx-border-color: red;");
            projectPathField.requestFocus();

            createAlert("Invalid Project Path", "The specified path is not a directory.");
            return false;
        }

        // Validate the mod ID
        if (modIdField.getText().isBlank()) {
            modIdField.setStyle("-fx-border-color: red;");
            modIdField.requestFocus();

            createAlert("Mod ID is Required", "Please enter a mod ID for your project.");
            return false;
        }

        if (modIdField.getText().length() < 2) {
            modIdField.setStyle("-fx-border-color: red;");
            modIdField.requestFocus();

            createAlert("Invalid Mod ID", "The mod ID must be at least 2 characters long.");
            return false;
        }

        if (!modIdField.getText().matches("^[a-z][a-z0-9_]{1,63}$")) {
            modIdField.setStyle("-fx-border-color: red;");
            modIdField.requestFocus();

            createAlert("Invalid Mod ID", "The mod ID must start with a lowercase letter and contain only lowercase letters, numbers, and underscores.");
            return false;
        }

        // Validate the mod name
        if (modNameField.getText().isBlank()) {
            modNameField.setStyle("-fx-border-color: red;");
            modNameField.requestFocus();

            createAlert("Mod Name is Required", "Please enter a mod name for your project.");
            return false;
        }

        if (modNameField.getText().length() < 2) {
            modNameField.setStyle("-fx-border-color: red;");
            modNameField.requestFocus();

            createAlert("Invalid Mod Name", "The mod name must be at least 2 characters long.");
            return false;
        }

        if (modNameField.getText().length() > 256) {
            modNameField.setStyle("-fx-border-color: red;");
            modNameField.requestFocus();

            createAlert("Invalid Mod Name", "The mod name must be at most 256 characters long.");
            return false;
        }

        // Validate the main class
        if (mainClassField.getText().isBlank()) {
            mainClassField.setStyle("-fx-border-color: red;");
            mainClassField.requestFocus();

            createAlert("Main Class is Required", "Please enter a main class for your project.");
            return false;
        }

        if (!ClassNameValidator.isValid(mainClassField.getText())) {
            mainClassField.setStyle("-fx-border-color: red;");
            mainClassField.requestFocus();

            createAlert("Invalid Main Class", "The main class must be a valid Java class name.");
            return false;
        }

        // Validate the group ID
        if (groupIdField.getText().isBlank()) {
            groupIdField.setStyle("-fx-border-color: red;");
            groupIdField.requestFocus();

            createAlert("Group ID is Required", "Please enter a group ID for your project.");
            return false;
        }

        if (!groupIdField.getText().matches("[a-zA-Z0-9.]+")) {
            groupIdField.setStyle("-fx-border-color: red;");
            groupIdField.requestFocus();

            createAlert("Invalid Group ID", "The group ID must contain only letters, numbers, and periods.");
            return false;
        }

        // Validate the artifact ID
        if (artifactIdField.getText().isBlank()) {
            artifactIdField.setStyle("-fx-border-color: red;");
            artifactIdField.requestFocus();

            createAlert("Artifact ID is Required", "Please enter an artifact ID for your project.");
            return false;
        }

        if (!artifactIdField.getText().matches("[a-z0-9-]+")) {
            artifactIdField.setStyle("-fx-border-color: red;");
            artifactIdField.requestFocus();

            createAlert("Invalid Artifact ID", "The artifact ID must contain only lowercase letters, numbers, and hyphens.");
            return false;
        }

        // Validate the version
        if (versionField.getText().isBlank()) {
            versionField.setStyle("-fx-border-color: red;");
            versionField.requestFocus();

            createAlert("Version is Required", "Please enter a version for your project.");
            return false;
        }

        if (!versionField.getText().matches("[0-9]+(\\.[0-9]+){0,2}(-[a-zA-Z0-9]+)?")) {
            versionField.setStyle("-fx-border-color: red;");
            versionField.requestFocus();

            createAlert("Invalid Version", "The version must be in the format of x.y.z or x.y.z-tag.");
            return false;
        }

        // Validate the license
        if (licenseComboBox.getValue() == License.CUSTOM && licenseCustomField.getText().isBlank()) {
            licenseCustomField.setStyle("-fx-border-color: red;");
            licenseCustomField.requestFocus();

            createAlert("Custom License is Required", "Please enter a custom license for your project.");
            return false;
        }

        return true;
    }

    protected FabricProjectData createData() {
        String projectName = projectNameField.getText().trim();
        var projectPath = Path.of(projectPathField.getText().trim());
        boolean createGit = createGitCheckBox.isSelected();
        License license = licenseComboBox.getValue();
        String licenseCustom = license == License.CUSTOM ? licenseCustomField.getText().trim() : null;
        MinecraftVersion minecraftVersion = minecraftVersionComboBox.getValue();
        FabricLoaderVersion fabricLoaderVersion = fabricLoaderVersionComboBox.getValue();
        Optional<FabricAPIVersion> fapiVersion = Optional.ofNullable(includeFapiCheckBox.isSelected() ? fapiVersionComboBox.getValue() : null);
        String modId = modIdField.getText().trim();
        String modName = modNameField.getText().trim();
        String mainClass = mainClassField.getText().trim();
        boolean useAccessWidener = useAccessWidenerCheckBox.isSelected();
        boolean splitSources = splitSourcesCheckBox.isSelected();
        MappingChannel mappingChannel = mappingChannelComboBox.getValue();
        MappingVersion mappingVersion = mappingVersionComboBox.getValue();
        Optional<String> author = Optional.of(authorField.getText().trim()).filter(s -> !s.isBlank());
        Optional<String> description = Optional.of(descriptionArea.getText().trim()).filter(s -> !s.isBlank());
        Optional<String> issues = Optional.of(issuesField.getText().trim()).filter(s -> !s.isBlank());
        String groupId = groupIdField.getText().trim();
        String artifactId = artifactIdField.getText().trim();
        String version = versionField.getText().trim();

        return new FabricProjectData(projectName, projectPath, createGit, license, licenseCustom,
                minecraftVersion, fabricLoaderVersion, fapiVersion,
                modId, modName, mainClass, useAccessWidener, splitSources,
                mappingChannel, mappingVersion,
                author, description, issues,
                groupId, artifactId, version);
    }
}