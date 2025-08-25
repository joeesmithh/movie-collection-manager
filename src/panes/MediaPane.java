package panes;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import movie.Database;
import movie.IMDBHTML;
import movie.Media;

public class MediaPane extends BorderPane {

	// Commonly used
	private final Insets padding = new Insets(5);
	private final Insets paddingLeft = new Insets(0, 0, 5, 5);
	private final Font fontBold = Font.font("Helvetica", FontWeight.BOLD, USE_COMPUTED_SIZE);

	// Cached fields
	private Media media;
	private HBox hbMediaArt;
	private VBox vbMediaInfo;
	private String imageURL;
	private String description;

	public MediaPane(Media media) {
		super();

		// General property initialization
		setPadding(padding);
		setStyle(("-fx-border-color: #708090; -fx-border-width: 1; -fx-border-insets: 2, 2, 2, 2"));
		this.media = media;

		// getPropertyString() gets the line in an HTML which contains movie attributes
		Label lblLoading = new Label("Loading movie attributes...");
		setCenter(lblLoading);
		Task<String> taskLoadHTMLString = IMDBHTML.loadHTML(media.getURLString());

		// Load panes when HTML load Task is successful
		taskLoadHTMLString.stateProperty().addListener(new ChangeListener<Worker.State>() {

			@Override
			public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
				
				// When the HTML file has fully loaded (i.e., when the task has succeeded)
				if (newValue == Worker.State.SUCCEEDED) {

					// Button - Save Media
					// --------------------------------------------------------------------------------------------
					Button btnSaveMedia = new Button(media.getIsSaved() == false ? "Save Movie" : "Remove Movie");
					VBox.setMargin(btnSaveMedia, new Insets(0, 3, 0, 3));
					btnSaveMedia.setMaxWidth(Double.MAX_VALUE);
					btnSaveMedia.setOnAction(e -> {

						ArrayList<Media> ml = new ArrayList<Media>(Database.getSavedMediaProperty().getValue());

						if (media.getIsSaved() == true) {
							ml.remove(media);
							media.setIsSaved(false);
							btnSaveMedia.setText("Save Movie");
						} else {
							ml.add(media);
							media.setIsSaved(true);
							btnSaveMedia.setText("Remove Movie");
						}

						Database.getSavedMediaProperty().setValue(ml);

					});

					VBox vbLeftPane = new VBox(5, getHBoxMediaArt(taskLoadHTMLString.getValue()), btnSaveMedia);
					setLeft(vbLeftPane);
					
					setCenter(getVBoxMediaInfo(taskLoadHTMLString.getValue()));
				}
			}
		});
	}
	
	private VBox getVBoxMediaInfo(String hTMLString) {

		// vbWrapper - Return if already set
		if (vbMediaInfo != null)
			return vbMediaInfo;

		// lblHeader - Initialization
		Label lblHeader = new Label("Description:");
		lblHeader.setFont(fontBold);

		// This is a comment

		// lblDescription - Initialization
		String description = getDescription(hTMLString).length() < 1 ? "Description not found."
				: getDescription(hTMLString);
		Label lblDescription = new Label(description);
		lblDescription.setWrapText(true);

		// vbWrapper - Initialization
		VBox vbWrapper = new VBox(lblHeader, lblDescription);
		vbWrapper.setPadding(padding);
		BorderPane.setAlignment(vbWrapper, Pos.TOP_LEFT);
		BorderPane.setMargin(vbWrapper, paddingLeft);

		// vbWrapper - Property Initialization
		vbWrapper.setMaxHeight(USE_PREF_SIZE);
		vbWrapper.setStyle("-fx-border-color: #708090; -fx-border-width: 1; -fx-border-insets: 2, 2, 2, 2");

		// vbWrapper - Return
		vbMediaInfo = vbWrapper;
		return vbWrapper;

	}

	private HBox getHBoxMediaArt(String hTMLString) {

		// Return if already set
		if (hbMediaArt != null)
			return hbMediaArt;

		// ImageView - Media Art
		ImageView ivMediaArt = getImageViewMediaArt(hTMLString);

		// HBox - Media Art Wrapper
		HBox hb = new HBox(ivMediaArt);
		HBox.setMargin(ivMediaArt, padding);

		// HBox - Property Initialization
		hb.setStyle("-fx-border-color: #708090; -fx-border-width: 1; -fx-border-insets: 2, 2, 2, 2");
		hb.setMaxHeight(hb.getPrefHeight() + 10);
		hb.setMaxWidth(hb.getPrefWidth() + 10);

		// Return
		hbMediaArt = hb;
		return hb;
	}

	private ImageView getImageViewMediaArt(String hTMLString) {
		Image img = getImageMediaArt(hTMLString);

		ImageView iv = new ImageView(img);
		iv.setFitWidth(160);
		iv.setPreserveRatio(true);
		return iv;
	}

	public Image getImageMediaArt(String hTMLString) {

		// Try to create Image object with image URL; create with default image URL
		// invalid
		try {
			return new Image(getImageURL(hTMLString));
		} catch (Exception e) {

			try {
				return new Image(new FileInputStream(
						"res" + File.separator + "images" + File.separator + "img_media_art_not_found.png"));
			} catch (Exception e2) {
				System.out.println("No media art to display.");
				return null;
			}
		}
	}

	public String getDescription(String hTMLString) {

		if (description != null)
			return description;

		description = getProperty(hTMLString, IMDBHTML.getLocatorDescription()).replaceAll("&apos;", "'")
				.replaceAll("&quot;", "\"");
		return description;

	}

	public String getImageURL(String hTMLString) {

		// If the image URL has already been computed, return field instead
		if (imageURL != null)
			return imageURL;

		imageURL = getProperty(hTMLString, IMDBHTML.getLocatorImage());
		return imageURL;
	}

	private String getProperty(String hTMLString, String locator) {

		int indexStart = hTMLString.indexOf(locator);
		if (indexStart == -1)
			return "";

		String substringStart = hTMLString.substring(indexStart + locator.length());

		int indexEnd = substringStart.indexOf("\"");
		String substringFull = substringStart.substring(0, indexEnd);

		return substringFull;
	}

	public Media getMedia() {
		return media;
	}

}
