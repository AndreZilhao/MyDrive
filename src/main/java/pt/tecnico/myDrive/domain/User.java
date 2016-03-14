package pt.tecnico.myDrive.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import pt.tecnico.myDrive.exception.ImportDocumentException;

import java.util.Stack;

public class User extends User_Base {

    static final Logger log = LogManager.getRootLogger();

    public User() {
        super();
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

    public File lookup(String pathname){
        String[] params = pathname.split("/");
        Stack<String> st = new Stack<>();
        for (int i = params.length-1; i > 0; i--) {
            log.debug(params[i]);
            st.push(params[i]);
        }

        File fd = SuperUser.getInstance().getFileByName("/");
        while (!st.empty()) {
            if (fd instanceof Dir)
                fd = ((Dir) fd).getFileByName(st.pop());
            //TODO: Check for links.
            //TODO: Check Permissions.
        }
        return fd;
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






}
