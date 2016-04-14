package pt.tecnico.myDrive.service.dto;

import org.joda.time.DateTime;

public class FileDto implements Comparable<FileDto> {
	private int id;
	private String filename;
	private DateTime lastModification;
	private String perimissions;
	private String type;
	private String content;

	public FileDto(int id, String filename, DateTime lastModification, String perimissions, String type, String content) {
		this.id = id;
		this.filename = filename;
		this.lastModification = lastModification;
		this.perimissions = perimissions;
		this.type = type;
		this.content = content;
	}

	public FileDto(int id, String filename, DateTime lastModification, String perimissions, String type) {
		this.id = id;
		this.filename = filename;
		this.lastModification = lastModification;
		this.perimissions = perimissions;
		this.type = type;
		this.content = "";
	}

	public final int getId() {
		return id;
	}

	public final String getFilename() {
		return filename;
	}

	public final DateTime getLastModification() {
		return lastModification;
	}

	public final String getPerimissions() {
		return perimissions;
	}

	public final String getContent() {
		return content;
	}

	public final String getType() {
		return type;
	}

	@Override
	public int compareTo(FileDto o) {
		if (id == o.getId())
			return 0;
		else
			return 1;
	}
}
