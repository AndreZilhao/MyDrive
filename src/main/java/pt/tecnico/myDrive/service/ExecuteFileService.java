package pt.tecnico.myDrive.service;

import pt.tecnico.myDrive.domain.*;
import pt.tecnico.myDrive.exception.DirCanNotHaveContentException;
import pt.tecnico.myDrive.exception.InvalidFileTypeException;
import pt.tecnico.myDrive.exception.MyDriveException;

public class ExecuteFileService extends MyDriveService {
	private long token;
	private String filename;
	private String fileType;
	private String content;

	public ExecuteFileService() {
		this.token = token;
		this.filename = filename;
		this.fileType = fileType;
		this.content = content;
	}


	@Override
	protected void dispatch() throws MyDriveException {
		MyDrive md = getMyDrive();
		Login login = md.getLoginFromId(token);
		login.refreshToken();
		User user = login.getUser();
		Dir currentDir = login.getCurrentDir();
		}
	}
