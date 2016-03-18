package pt.tecnico.myDrive.domain;

import pt.tecnico.myDrive.exception.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;

import java.util.Stack;

public class User extends User_Base {

    static final Logger log = LogManager.getRootLogger();

    public User() {
        super();
    }
	  
	  public User (String username, String name, String umask, String home, String password){
		  
	    setUsername(username);
	    setName(username);
	    setPassword(username);
	    setMask(umask);
	    setHome(home);
	  }
	  
	  public File createFile(String name, User user, Dir directory, String permissions){
		  File file= new File(name,user,directory,permissions);
		  return file;
	  }
	  
	  @Override
	  public void addFile(File fileToBeAdded) throws UserAlreadyExistsException{
		  if(hasFile(fileToBeAdded.getName()))
			  throw new FileAlreadyExistsException(fileToBeAdded.getName());
		  
		  super.addFile(fileToBeAdded);
	  }
	  
	  public File getFileByName(String name){
		  for (File file: getFileSet())
			  if (file.getName().equals(name))
				  return file;
		  return null;
	  }
	  
	  public boolean hasFile(String fileName){
		  return getFileByName(fileName)!= null;
	  }
	  
	  public boolean isAlphanumeric(String str) {
		  for (int i=0; i<str.length(); i++) {
			  char c = str.charAt(i);
			  if (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a)
		      return false;
		    }
		  return true;
		}
	  
	  @Override
	  public void setUsername(String username) throws InvalidUsernameException /*UserAlreadyExistsException*/ {
		  
	    if (username == null){
	      throw new InvalidUsernameException("Username cannot be empty");
	    }
	    /*if (username.equals("root")){
	      throw new UserAlreadyExistsException(username);
	    }*/
		  if(isAlphanumeric(username)){
	    	super.setUsername(username);
	    } else {
	    	throw new InvalidUsernameException("not valid");
	    }
	  }  
	 
	  public void setMask(String umask) throws InvalidUsernameException{
	    if (umask.equals("rwxd----")){
	      super.setUmask(umask);
	    } else {
	      throw new InvalidUsernameException("Mask not valid");
	    }
	  }
	 	
	  public void remove(){
	    for(File f: getFileSet()){
	      f.remove();
	    }
	    setMyDrive(null);
	    deleteDomainObject();
	  }

    public User(MyDrive md, String username, Element xml) {
        super();
        xmlImport(username, xml);
        setMyDrive(md);
    }

    @Override
    public void setMyDrive(MyDrive md) {
        if (md == null)
            super.setMyDrive(null);
        else
            md.addUser(this);
    }

	public boolean checkPermission(File file, Character c){
		if (file.isOwner(this)){
			switch (c){
				case 'r':
					return file.getPermissions().matches("r.......");
				case 'w':
					return file.getPermissions().matches(".w......");
				case 'x':
					return file.getPermissions().matches("..x.....");
				case 'd':
					return file.getPermissions().matches("...d....");
			}
		}
		switch (c){
			case 'r':
				return file.getPermissions().matches("....r...");
			case 'w':
				return file.getPermissions().matches(".....w..");
			case 'x':
				return file.getPermissions().matches("......x.");
			case 'd':
				return file.getPermissions().matches(".......d");
		}
		return false;
	}

	public boolean setPermissions (File file, String newPermissions){
		if (file.isOwner(this)){
			file.setPermissions(newPermissions);
			log.info("Set Permissions to "+ file.getName()+ ": Access Granted.");
			return true;
		} else {
			log.info("Set Permissions to "+ file.getName()+ ": Access Denied.");
			return false;
		}
	}

	private Stack<String> toStack (String pathname) {
		String[] params = pathname.split("/");
		Stack<String> st = new Stack<>();
		for (int i = params.length - 1; i > 0; i--) {
			log.debug(params[i]);
			st.push(params[i]);
		}
		return st;
	}

	public File lookup(String pathname) throws MyDriveException {

		File file = Dir.getRootDir();
		Stack<String> st = toStack(pathname);

		while (!st.empty()) {
			String filename = st.pop();
			file = file.getFileByName(filename);
			if (file.equals(null))
				throw new FileDoesNotExistException(filename);
			if (!(this.checkPermission(file, 'r'))) {
				throw new NoPermissionException(filename);
			}
			//TODO: Check for links.
		}
		return file;
	}

	public Dir makeDir(String pathname){

		File file = Dir.getRootDir();
		Stack<String> st = toStack(pathname);
		while (!st.empty()) {
				String temp = st.pop();
				Dir d = (Dir)file;
				file = file.getFileByName(temp);
				if (file == null) {
					file = new Dir(temp,this,d,this.getUmask());
				}

		}
		return (Dir)file;
	}

	public String read (File file) throws MyDriveException {
		if(this.checkPermission(file, 'r')){
			return file.read();
		}
		throw new NoPermissionException("read");
	}

	public void write (File file, String content) throws MyDriveException {
		if(this.checkPermission(file, 'w')){
			file.write(content);
			return;
		}
		throw new NoPermissionException("write");
	}

	public void execute (File file) throws MyDriveException {
		if(this.checkPermission(file, 'x')){
			file.execute();
			return;
		}
		throw new NoPermissionException("execute");
	}

	public void delete (File file) throws MyDriveException {
		if(this.checkPermission(file, 'd')){
			file.delete();
			return;
		}
		throw new NoPermissionException("delete");
	}

    public void xmlImport(String username, Element userElement) throws ImportDocumentException {

        String defaultHome = "/home/" + username;
        String defaultMask = "rwxd----";
        String defaultName = username;
        String defaultPassword = username;

        for (Element child : userElement.getChildren()) {
            if (child.getName().equals("home"))
                defaultHome = child.getText();
            if (child.getName().equals("mask"))
                defaultMask = child.getText();
            if (child.getName().equals("name"))
                defaultName = child.getText();
            if (child.getName().equals("password"))
                defaultPassword = child.getText();
            log.info("<" + child.getName() + ">" + child.getText() + " </" + child.getName() + ">");
        }

        setUsername(username);
        setHome(defaultHome);
        setUmask(defaultMask);
        setName(defaultName);
        setPassword(defaultPassword);
    }

	public Element xmlExport() {
		Element userNode = new Element("user");
		userNode.setAttribute("username", getUsername());

		Element nameElement = new Element("name");
		Element maskElement = new Element("mask");
		Element homeElement = new Element("home");
		Element passwordElement = new Element("password");

		nameElement.addContent(getName());
		maskElement.addContent(getUmask());
		homeElement.addContent(getHome());
		passwordElement.addContent(getPassword());

		userNode.addContent(nameElement);
		userNode.addContent(maskElement);
		userNode.addContent(homeElement);
		userNode.addContent(passwordElement);

		return userNode;
	}
}
