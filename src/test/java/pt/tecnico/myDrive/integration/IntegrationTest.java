package pt.tecnico.myDrive.integration;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import pt.ist.fenixframework.Atomic;
import pt.tecnico.myDrive.domain.MyDrive;
import pt.tecnico.myDrive.domain.PlainFile;
import pt.tecnico.myDrive.domain.SuperUser;
import pt.tecnico.myDrive.domain.User;
import pt.tecnico.myDrive.exception.FileDoesNotExistException;
import pt.tecnico.myDrive.service.*;
import pt.tecnico.myDrive.service.dto.FileDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class IntegrationTest extends AbstractServiceTest {

    private MyDrive md;
    private SuperUser su;

    private static final String IMPORT_XML_FILENAME = "users.xml";
    private static final int INITIAL_NUMBER_FILES = 2;
    private static final String DIR_TYPE = "Dir";
    private static final String PLAIN_TYPE = "Plain";
    private static final String LINK_TYPE = "Link";
    private static final String APP_TYPE = "App";

    private static final List<UserInfoTest> users = new ArrayList<>();

    private class UserInfoTest {
        public String username, password;
        public Long token;
        public String homeDir;
        public String currentDir;

        public int numberFilesHomeDir;
        public int numberDirsToCreate, numberPlainsToCreate, numberLinksToCreate, numberAppsToCreate;

        public Map<String,String> envVars;
    }

    private int indexOfByUsername(String username) {
        int idx = 0;
        for (UserInfoTest ui : users) {
            if (ui.username.equals(username))
                return idx;
            idx++;
        }
        return -1;
    }

    void specificUserInitialization() {
        for (UserInfoTest ui : users)
            System.out.println(ui.username);
        UserInfoTest jtb = users.get(indexOfByUsername("jtb"));
        jtb.numberFilesHomeDir += 4;
        for (UserInfoTest ui : users) {
            ui.numberPlainsToCreate = 10;
            ui.numberLinksToCreate = 10;
            ui.numberAppsToCreate = 10;
            ui.numberDirsToCreate = 10;
            ui.envVars.put("$"+ui.username+"_HOME",ui.homeDir);
        }
    }

    @Atomic
    private void getUserInfo() {

            for (User u : md.getUserSet()) {
                UserInfoTest ui = new UserInfoTest();
                ui.username = u.getUsername();
                ui.password = u.getPasswordForTesting();
                ui.homeDir = u.getHomeDir().getPath();
                ui.currentDir = ui.homeDir;
                ui.numberFilesHomeDir = 0;
                ui.envVars = new HashMap<>();
                users.add(ui);
            }
            specificUserInitialization();
    }

    protected void populate() {
        md = MyDrive.getInstance();
        su = md.getSuperUser();
    }

    private void loginUser(UserInfoTest uit) {
        log.debug("[System Integration Test] Login Service of user " + uit.username + " - uses LoginUserService");
        LoginUserService us = new LoginUserService(uit.username, uit.password);
        us.execute();

        assertNotNull(us.result());
        uit.token = us.result();
    }

    private void logoutUser(UserInfoTest uit){
        log.debug("[System Integration Test] Logout Service of user " + uit.username + " - uses LogoutUserService");
        LogoutUserService us = new LogoutUserService(uit.token);
        us.execute();
    }

    private void listDirectoryUser(UserInfoTest uif, int expectedNumberFiles) {
        log.debug("[System Integration Test] List current Dir Files of User: " + uif.username
                + ", Current Dir : " + uif.currentDir + " - uses ListDirectoryService");
        ListDirectoryService lds = new ListDirectoryService(uif.token,uif.currentDir);
        lds.execute();

        for (FileDto dto : lds.result())
            log.debug("\t" + dto.getType() + " -> " + dto.getFilename());

        assertEquals("[System Integration Test] ListDirectoryService. User " + uif.username + " should have the correct "
                + "number of files in " + uif.currentDir + " : ", expectedNumberFiles + INITIAL_NUMBER_FILES, lds.result().size());
    }

    private void changeDirUser(UserInfoTest uif, String pathNewDir) {
        log.debug("[System Integration Test] ChangeDirectoryService . User " + uif.username + "changes current dir from "
                + uif.currentDir + " to " + pathNewDir + " - uses ChangeDirectoryService");
        ChangeDirectoryService cds = new ChangeDirectoryService(uif.token, pathNewDir);
        cds.execute();

        assertEquals("Changed to a wrong pathname", pathNewDir, cds.result());
    }

    private void writeFileUser(UserInfoTest uit, String fileName, String content) {
        log.debug("[System Integration Test] WriteFileService. User: " + uit.username + ", write the content:" + content
                + " to the file : " + fileName + " - uses WriteFileService");
        WriteFileService wft = new WriteFileService(uit.token, fileName, content);
        wft.execute();

        String assertWriteServiceMsg = "[System Integration Test] WriteFileService. The  file "
                + uit.currentDir + "/" + fileName + " of user " + uit.username + " should exist";
        PlainFile pf = (PlainFile) su.lookup(uit.currentDir + "/" + fileName);

        assertNotNull(assertWriteServiceMsg, pf);
        assertEquals("Content Written don't match.", uit.username, pf.getContent());
    }

    private void readFileUser(UserInfoTest uit, String fileName) {
        log.debug("[System Integration Test] ReadFileService. User: " + uit.username + ", reads the content of file: " +
                fileName + " -  uses ReadFileService");
        ReadFileService rft = new ReadFileService(uit.token, fileName);
        rft.execute();

        assertNotNull("[System Integration Test] ReadFileService. The file " + fileName + " should exists", rft.result());
        assertEquals("Content Read from don't match.", uit.username, rft.result());
    }

    private void createFileBatchUser(UserInfoTest uit, String filename, String fileType, String content, int maxNumber) {
        for (int idFile = 0; idFile < maxNumber; idFile++) {
            createFileUser(uit, filename + idFile, fileType, content);
        }
    }

    private void createFileUser(UserInfoTest uit, String filename, String fileType, String content) {
        log.debug("[System Integration Test] CreateFileService. User: " + uit.username + ", will create the file "
                + filename + ", of type : " + fileType + ", in the directory: " + uit.currentDir + " - uses CreateFileService");
        if (fileType.equals(DIR_TYPE)) {
            new CreateFileService(uit.token, filename, fileType).execute();
        } else {
            new CreateFileService(uit.token, filename, fileType, content).execute();
        }

        assertNotNull(su.lookup(uit.currentDir + "/" + filename));
    }

    private void deleteFileUser(UserInfoTest uit, String fileName) {
        log.debug("[System Integration Test] DeleteFileService. User: " + uit.username + ", delete the file "
                + fileName + " - uses DeleteFileService");
        DeleteFileService dft = new DeleteFileService(uit.token, fileName);
        try{
            dft.execute();
            su.lookup(uit.currentDir + "/" + fileName);
        }catch (FileDoesNotExistException e){
            return;
        }

        fail("The File " + fileName + ", of user " + uit.username + ", in dir " + uit.currentDir + " was found, but was " +
                "expected to be delected");
    }

    private void addEnvVariableBatchUser(UserInfoTest uit){
        log.debug("[System Integration Test] AddEnvVariableService. Adding Env. Vars of the User: " + uit.username + "" +
                " - uses AddEnvVariableService");

        for(Map.Entry<String, String> entry : uit.envVars.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                AddEnvVariableService aev = new AddEnvVariableService(uit.token,name,value);
                aev.execute();
                assertTrue(aev.result().stream().anyMatch(var -> var.getName().equals(name)));
        }
    }

    @Test
    public void success() throws Exception {
        try {
            log.debug("[System Integration Test] - ImportXMLService");
            new ImportXMLService(IMPORT_XML_FILENAME).execute();
            getUserInfo();

            for (UserInfoTest ui : users) {
                if (ui.username.equals("root")  || ui.username.equals("nobody"))
                    continue;
                loginUser(ui);
                addEnvVariableBatchUser(ui);
                listDirectoryUser(ui, ui.numberFilesHomeDir);

                String filename = "plainExample";
                String plainContent = "This\nIs\nA\nPlain File\nContent!";
                createFileUser(ui, filename, PLAIN_TYPE, plainContent);
                listDirectoryUser(ui,++ui.numberFilesHomeDir);

                writeFileUser(ui, filename, ui.username);
                readFileUser(ui, filename);
                listDirectoryUser(ui, ui.numberFilesHomeDir);

                filename = "dir" + ui.username;
                createFileUser(ui, filename, DIR_TYPE, "");
                ui.numberFilesHomeDir++;

                String pathNewDir = "/home/" + ui.username + "/" + filename;
                changeDirUser(ui, pathNewDir);
                ui.currentDir = pathNewDir;

                createFileBatchUser(ui, DIR_TYPE, DIR_TYPE, "", ui.numberDirsToCreate);
                listDirectoryUser(ui, ui.numberDirsToCreate);

                createFileBatchUser(ui, PLAIN_TYPE, PLAIN_TYPE, plainContent, ui.numberPlainsToCreate);
                listDirectoryUser(ui, ui.numberPlainsToCreate + ui.numberDirsToCreate);

                String linkContent = "/home/" + ui.username;
                createFileBatchUser(ui, LINK_TYPE, LINK_TYPE, linkContent, ui.numberLinksToCreate);
                listDirectoryUser(ui, ui.numberLinksToCreate + ui.numberPlainsToCreate + ui.numberDirsToCreate);

                String appContent = "pt.tecnico.myDrive.presentation.Hello.sum";
                createFileBatchUser(ui, APP_TYPE, APP_TYPE, appContent, ui.numberAppsToCreate);

                int expectedNumberFiles = ui.numberLinksToCreate + ui.numberPlainsToCreate
                        + ui.numberDirsToCreate + ui.numberAppsToCreate;
                listDirectoryUser(ui, expectedNumberFiles);

                filename = "Plain.pdf";
                createFileUser(ui, filename, PLAIN_TYPE, "");

                deleteFileUser(ui,"Plain0");
                listDirectoryUser(ui,expectedNumberFiles);

                new MockUp<ExecuteFileAssociationService>(){
                    @Mock
                    public final void dispatch(){
                        su.lookup(ui.currentDir+"/"+"App0").execute(md.getUserByUsername(ui.username),new String[0]);
                    }
                };

                ExecuteFileAssociationService efas = new ExecuteFileAssociationService(ui.token, filename);
                efas.execute();

                pathNewDir = "/home/" + ui.username;
                changeDirUser(ui, pathNewDir);
                ui.currentDir = pathNewDir;

                listDirectoryUser(ui, ui.numberFilesHomeDir);
                deleteFileUser(ui, pathNewDir+"/dir"+ui.username);

                logoutUser(ui);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

}

