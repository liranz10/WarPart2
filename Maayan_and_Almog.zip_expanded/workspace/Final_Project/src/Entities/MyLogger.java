package Entities;

import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.Pointcut;


public aspect MyLogger {
	  private Logger log = Logger.getLogger("WarGame");

	   Pointcut trace(): execution(* Entities.Destructor_.launcherDestructorStart(..));
	    void log(JoinPoint point) {
	        System.out.println("stam");
	    }
	    
}
