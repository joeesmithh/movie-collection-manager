package movie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import panes.MediaPane;

@SuppressWarnings("unchecked")
public final class Database {
	
	private static final int minChar = '!', maxChar = '~';
	private static final String pathSave = "res" + File.separator + "saves" + File.separator + "sv_media.txt"; 
	
	private static ArrayList<Media>[] media = (ArrayList<Media>[]) new ArrayList[maxChar - minChar + 1];
	private static ArrayList<Media> savedMedia = new ArrayList<Media>();
	private static ArrayList<MediaPane> mediaPanes = new ArrayList<MediaPane>();
	private static ObjectProperty<ArrayList<Media>> savedMediaProperty = new SimpleObjectProperty<ArrayList<Media>>(new ArrayList<Media>());
	
	/**
	 *  Load Media objects from text file.
	 */
	public static void loadSave() {
		
		try(Scanner file = new Scanner(new File(pathSave))){
			
			while(file.hasNextLine()) {
				
				try(Scanner line = new Scanner(file.nextLine()).useDelimiter("\t")){
					
					String iMDBID = line.next();
					String type = line.next();
					String title = line.next();
					String year = line.next();
					String isSaved = line.next();
					String genres = line.next();
					
					ArrayList<Media> m = new ArrayList<Media>(savedMediaProperty.get());
					m.add(new Media(iMDBID, type, title, year, Boolean.parseBoolean(isSaved), genres));
					
					getSavedMediaProperty().setValue(m);;
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 	Save properties of all Media objects in list to file.
	 */
	public static void save() {
		
		try(PrintWriter pw = new PrintWriter(new File(pathSave))) {
			
			StringBuilder sb = new StringBuilder();
			
			for (Media media : savedMediaProperty.getValue()) {
				sb.append(media.getSaveString() + '\n');
			}
			
			if(sb.length() > 1)
				sb.deleteCharAt(sb.length() - 1);
			
			pw.print(sb.toString());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** Loads the large media database into {@code media} list in a threaded fashion. 
	 *	Returning the Task allows callers to add listeners to the value property
	 *	of the running Task. 
	 */
	public static Task<Integer> taskLoadDatabase() {
		
		Task<Integer> task = new Task<Integer>() {

			@Override
			protected Integer call() throws Exception {
				
				// Initialize all Media ArrayLists
				for (int i = 0; i < media.length; i++)
					media[i] = new ArrayList<Media>();

				// Try initialize File object for database.txt file path
				int count = 0;
				try {
					
					// Initialize Scanner object to hold file stream of database.txt
					try (BufferedReader databaseFileStream = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/database/db_movies.txt")))) {
						
						// While the database.txt file has data to read
						String line;
						while ((line = databaseFileStream.readLine()) != null) {
							
							// Initialize Scanner object to hold file stream of current line
							try (Scanner lineStream = new Scanner(line)) {

								// Set file stream to be tab delimited
								lineStream.useDelimiter("\t");

								String iMDBID = lineStream.next(), title = lineStream.next(), year = lineStream.next(),
										genres = lineStream.next();

								// Singular-character entries
								// -----------------------------------------------------------------------------------------------------------------------

								// If the title cannot be alphabetized, don't create the Media object
								if (title.charAt(0) < minChar || title.charAt(0) > maxChar)
									continue;

								// Initialize new Media object and add it to the Movie ArrayList at the
								// correct alphabetic index in the Array of ArrayLists
								media[title.toLowerCase().charAt(0) - minChar]
										.add(new Media(iMDBID, "movie", title, year, genres));

								// Increment total media count
								updateValue(++count);
								updateProgress(count, 500118);
							}
						}
					}
				} catch (NumberFormatException nfe) {
					System.out.println(nfe.toString());
				} catch (NullPointerException npe) {
					System.out.println(npe.toString());
				} catch (FileNotFoundException fnfe) {
					System.out.println(fnfe.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return count;
			}
		};
			
		// Execute and return threaded Task
		Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();
		return task;
	}

	/**
	 * Populate an ArrayList with Media object matches.
	 * 
	 * @param search Search key.
	 * @param max    Maximum amount of matches to return.
	 * @return ArrayList of Media objects with titles beginning with {@code search}
	 *         key.
	 */
	public static ArrayList<Media> search(String search, int max) {

		// Initialize empty Media list
		ArrayList<Media> matches = new ArrayList<Media>();

		// Return empty list if search is empty or contains invalid characters
		if (search.length() < 1 || search.charAt(0) < minChar || search.charAt(0) > maxChar)
			return matches;

		// Get the Media list at search's first character index
		ArrayList<Media> charMediaList = media[search.toLowerCase().charAt(0) - minChar];
		
		// When search contains one character, don't search any further - return truncated list
		if (search.length() == 1)
			return new ArrayList<Media>(charMediaList.subList(0, max));

		// Loop through all Media in list searching for matches
		for (int i = 0; i < charMediaList.size(); i++) {
			Media m = charMediaList.get(i);

			if (m.getTitle().toLowerCase().startsWith(search.toLowerCase()))
				matches.add(m);

			// Return all matches if desired max matches reached
			if (matches.size() >= max)
				return matches;
		}

		return matches;
	}

	/**
	 * Stores ArrayList of Media for each character from {@code minChar} to
	 * {@code maxChar}
	 */
	public static ArrayList<Media>[] getMedia() {
		return media;
	}

	/** Stores ArrayList of MediaPanes for each loaded MediaPane */
	public static ArrayList<MediaPane> getMediaPanes() {
		return mediaPanes;
	}

	public static ArrayList<Media> getSavedMedia() {
		return savedMedia;
	}

	public static ObjectProperty<ArrayList<Media>> getSavedMediaProperty() {
		return savedMediaProperty;
	}

}
