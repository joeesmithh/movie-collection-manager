package movie;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import javafx.concurrent.Task;

public final class IMDBHTML {
	private final static String locatorPropString = "</script><title>";
	private final static String locatorImage = "\"image\":\"";
	private final static String locatorDescription = "\",\"description\":\"";
	
	public static Task<String> loadHTML(String urlString) {
		
		Task<String> task = new Task<String>() {

			@Override
			protected String call() throws Exception {
				
				try {

					// Initialize URL object
					URL url = new URI(urlString).toURL();

					// Request access from host
					URLConnection cnct = url.openConnection();
					cnct.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

					// Write each line of file to Array elements
					try (Scanner s = new Scanner(cnct.getInputStream())) {
						while (s.hasNext()) {
							String line = s.nextLine();

							if (line.contains(locatorPropString)) {

								return line;
							}
						}
					}

				} catch (Exception e) {
				}

				return "";
			}
			
		};
				
		// Execute and return threaded Task
		Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();
		return task;
	}

	public static String getLocatorImage() {
		return locatorImage;
	}
	
	public static String getLocatorDescription() {
		return locatorDescription;
	}
}
