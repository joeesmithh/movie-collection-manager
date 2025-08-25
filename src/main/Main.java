package main;

import movie.*;
import panes.MediaPane;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {

	private final Insets padding = new Insets(5);
	private final int margin = 0;
	private final int maxListDisplayCount = 30;

	private BorderPane bdrPane = new BorderPane();
	private ArrayList<Media> mediaMatches = new ArrayList<>();

	public static void main(String[] args) {

		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		bdrPane.setStyle("-fx-background-color: #f5f5f5");
		
		// Left Pane
		// -----------------------------------------------------------------------------------

		// Assign instance of ListView of type Media to variable
		ListView<Media> lvMediaTitles = getListView();
		ListView<Media> lvSavedMedia = getListView();

		// Perform actions when new ListView item is selected
		lvMediaTitles.getSelectionModel().selectedItemProperty().addListener(new MediaSelectionListener(lvMediaTitles));
		lvSavedMedia.getSelectionModel().selectedItemProperty().addListener(new MediaSelectionListener(lvSavedMedia));

		// Re-populate saved media ListView if Database list has changed
		Database.getSavedMediaProperty().addListener(new ChangeListener<ArrayList<Media>>() {

			@Override
			public void changed(ObservableValue<? extends ArrayList<Media>> observable, ArrayList<Media> oldValue,
					ArrayList<Media> newValue) {
				lvSavedMedia.setItems(FXCollections.observableArrayList(newValue));
				Database.save();
			}
		});

		// Load saved media - triggers getSavedMediaProperty() listeners when loaded
		Database.loadSave();

		// TextField - search
		TextField tfSearch = new TextField("Loading database...");
		tfSearch.setEditable(false);
		tfSearch.setDisable(true);
		tfSearch.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				mediaMatches = Database.search(newValue, maxListDisplayCount);

				// Clear ListView selection when search is updated
				lvMediaTitles.getSelectionModel().clearSelection();

				// Populate ListView with matches
				lvMediaTitles.setItems(FXCollections.observableArrayList(mediaMatches));
			}

		});

		// Initialize VBox
		Label lblSearch = new Label("Search Database"), lblSavedMedia = new Label("Saved Media");
		lblSearch.setFont(Font.font("Candara", FontWeight.BOLD, 12));
		lblSavedMedia.setFont(Font.font("Candara", FontWeight.BOLD, 12));
		StackPane.setAlignment(lblSavedMedia, Pos.BOTTOM_RIGHT);
		StackPane.setAlignment(lblSearch, Pos.BOTTOM_RIGHT);
		
		VBox vbPaneLeft = getVBox(new StackPane(lblSearch), tfSearch, lvMediaTitles, new StackPane(lblSavedMedia),
				lvSavedMedia);
		VBox.setMargin(lvMediaTitles, new Insets(0, 0, 10, 0));

		bdrPane.setLeft(vbPaneLeft);

		// Load Database
		// ------------------------------------------------------------------------------------------------

		// Label - Total Media
		Label lblMediaCount = new Label();

		// StackPane - Label Wrapper
		ProgressBar pbLoadSave = new ProgressBar();
		StackPane.setAlignment(pbLoadSave, Pos.CENTER_LEFT);
		
		StackPane spLoadSave = new StackPane(pbLoadSave);
		HBox.setHgrow(spLoadSave, Priority.ALWAYS);
		
		pbLoadSave.maxWidthProperty().bind(spLoadSave.widthProperty());

		HBox hbLoadSave = new HBox(5, lblMediaCount, spLoadSave);
		hbLoadSave.setStyle("-fx-border-color: #708090; -fx-border-width: 1; -fx-border-insets: 2, 2, 2, 2");
		hbLoadSave.setPadding(padding);
		

		bdrPane.setBottom(hbLoadSave);

		// Load database
		// -------------------------------------------------------------------------------------------------------------

		Task<Integer> loadDBTask = Database.taskLoadDatabase();

		// Listen for database loading
		loadDBTask.progressProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				pbLoadSave.setProgress(newValue.doubleValue());
			}
		});

		// Listen for amount of Media objects loaded from database
		loadDBTask.valueProperty().addListener(new ChangeListener<Integer>() {

			@Override
			public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
				lblMediaCount.setText("Total Media: " + newValue);

			}
		});

		// Listen for state change in database
		loadDBTask.stateProperty().addListener(new ChangeListener<Worker.State>() {

			@Override
			public void changed(ObservableValue<? extends javafx.concurrent.Worker.State> observable,
					javafx.concurrent.Worker.State oldValue, javafx.concurrent.Worker.State newValue) {

				if (newValue == Worker.State.SUCCEEDED) {
					tfSearch.setText("Frankenstein");
					tfSearch.setEditable(true);
					tfSearch.setDisable(false);
				}
			}
		});

		// Center Pane
		// -----------------------------------------------------------------------------------
		Label lblIntro = new Label( "Hello and welcome to my movie collection manager! \nThis program " + 
				"utilizes the Internet Movie Database's (IMDB) open source .csv file to create a " + 
				"programmatic catalogue of every movie within their database. \n\nThe user can " + 
				"search for any movie within the database, view associated movie art and descriptions, " + 
				"and add them to their movie collection (Saved Media). This process involves " + 
				"downloading the media's associated HTML page and scraping the HTML file for the " + 
				"relevant information. \n\nThe program will save basic attributes of media added to the " + 
				"collection during runtime and reload them on program restart.");
		lblIntro.setWrapText(true);
		lblIntro.setFont(Font.font("Candara", FontWeight.BOLD, Label.USE_COMPUTED_SIZE));

		StackPane stckPaneCenter = new StackPane(lblIntro);
		stckPaneCenter.setPadding(new Insets(60));
		stckPaneCenter.setPrefSize(500, 500);
		stckPaneCenter.setStyle("-fx-border-color: #708090; -fx-border-width: 1; -fx-border-insets: 2, 2, 2, 2");
		bdrPane.setCenter(stckPaneCenter);
		
		// Scene and Stage
		// -----------------------------------------------------------------------------------
		Scene scene = new Scene(bdrPane, 500 * 3 / 2, 300 * 3 / 2);
		primaryStage.setTitle("Joe's Movie Collection Manager");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	class MediaSelectionListener implements InvalidationListener {

		private ListView<Media> lv;

		public MediaSelectionListener(ListView<Media> lv) {
			super();
			this.lv = lv;
		}

		@Override
		public void invalidated(javafx.beans.Observable observable) {

			// Get the Media object at ListView's current selection index
			Media crntMedia = lv.getSelectionModel().getSelectedItem();
			if (crntMedia != null) {

				MediaPane mediaPane = null;

				// If selected Media already has MediaPane association, grab it rather than
				// creating new one
				for (MediaPane mp : Database.getMediaPanes()) {
					if (mp.getMedia().equals(crntMedia)) {
						mediaPane = mp;
						break;
					}
				}

				// If MediaPane was not grabbed, create new and assign to ArrayList
				if (mediaPane == null) {
					mediaPane = new MediaPane(crntMedia);
					Database.getMediaPanes().add(mediaPane);
				}

				// Set main BorderPane's center to MediaPane
				bdrPane.setCenter(mediaPane);
			}

		}

	}

	private ListView<Media> getListView() {
		ListView<Media> lv = new ListView<Media>();
		// VBox.setVgrow(lv, Priority.ALWAYS);
		return lv;
	}

	private VBox getVBox(Node... nodes) {
		VBox vb = new VBox(margin, nodes);
		vb.setPadding(padding);
		//vb.setStyle("-fx-border-color: black");
		return vb;
	}
}
