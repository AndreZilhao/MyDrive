package pt.tecnico.myDrive.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.joda.time.DateTime;

import java.math.BigInteger;

import java.util.Random;

import pt.tecnico.myDrive.domain.User;

public class Login extends Login_Base {
    
	static final Logger log = LogManager.getRootLogger();
	
    public Login(User user) {
        super();
        
        Long tokenId = new BigInteger(64, new Random()).longValue();
        
        this.setUser(user);
        this.setLoginDate(new DateTime());
     	this.setIdentifier(tokenId);
     	this.setCurrentDir(user.getHomeDir());
    }
    
    public Login(User user, long oldToken) {
        super();
        
        Long tokenId = new BigInteger(64, new Random()).longValue();
        
        this.setUser(user);
        this.setLoginDate(new DateTime());
        if (user.getUsername().equals("root")){
     	   this.setIdentifier(tokenId);
        }
        else{
        	while(true){
        		tokenId = new BigInteger(64, new Random()).longValue();
        		if (tokenId != oldToken){
        			this.setIdentifier(tokenId);
        			break;
        		}
        	}
        }
     }
    
    public void refreshToken(){
    		Long tokenId = new BigInteger(64, new Random()).longValue();
    		
	    	if (this.getIdentifier() == tokenId){
	    		this.refreshToken();
	    	}
	    	else{
	    		this.setIdentifier(tokenId);
	        	this.setLoginDate(new DateTime());
	    	}
    	}	
    
    public boolean isDateValid(DateTime data){
    	double hours = (data.getMillis() - this.getLoginDate().getMillis()) /1000/60/60;
		if(hours>2){
			return false;
		}
		return true;
    }
    
    public void delete(){
    	this.setUser(null);
    	this.setCurrentDir(null);
    	
    	for(EnvVariables env : super.getEnvVarSet()){
	    	super.removeEnvVar(env);
	    	//env.delete();
	    }
    	
    	deleteDomainObject();
    }
}


