package movie;

import java.util.Arrays;
import java.util.Objects;

public class Media {

	private String iMDBID;
	private String type;
	private String title;
	private String year;
	private String[] genres;
	private String uRL;
	private String hTML;
	private boolean isSaved;

	public String getSaveString() {
		return iMDBID + '\t' + type + '\t' + title + '\t' + year + '\t' + isSaved + '\t' + genres[0];
	}

	public Media(String iMDBID, String type, String title, String year, String... genres) {
		this.iMDBID = iMDBID;
		this.type = type;
		this.title = title;
		this.year = year;
		this.genres = genres;
		this.uRL = "https://www.imdb.com/title/" + iMDBID + "/";
		this.isSaved = false;
	}

	public Media(String iMDBID, String type, String title, String year, boolean isSaved, String... genres) {
		this(iMDBID, type, title, year, genres);
		this.isSaved = isSaved;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(genres);
		result = prime * result + Objects.hash(hTML, iMDBID, title, type, uRL, year);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Media other = (Media) obj;
		return Arrays.equals(genres, other.genres) && Objects.equals(hTML, other.hTML)
				&& Objects.equals(iMDBID, other.iMDBID) && Objects.equals(title, other.title)
				&& Objects.equals(type, other.type) && Objects.equals(uRL, other.uRL)
				&& Objects.equals(year, other.year);
	}

	@Override
	public String toString() {
		return title + " " + year;
	}

	public String getTitle() {
		return title;
	}

	public String getURLString() {
		return uRL;
	}

	public boolean getIsSaved() {
		return isSaved;
	}

	public void setIsSaved(boolean isSaved) {
		this.isSaved = isSaved;
	}
}
