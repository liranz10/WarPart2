package Entities;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;


@Aspect
public class MyLogger {
	  private Logger log = Logger.getLogger("WarGame");
	
	  @After("execution(public void Destructor_info(..))")
	  void logDestructor_(JoinPoint p) {
	        log.info((String)p.getArgs()[0]);
	    }
	  
	  @After("execution(public void Destructorinfo(..))")
	  void logDestructor(JoinPoint p) {
	        log.info((String)p.getArgs()[0]);
	    }
	  @After("execution(public void Launcherinfo(..))")
	  void logLauncher(JoinPoint p) {
	        log.info((String)p.getArgs()[0]);
	    }
	  
	  
	  @After("execution(public void init())")
	  public void setupWarLoggerHandler(JoinPoint p) {
		  try {
		   FileHandler fh = new FileHandler("log.txt");
           fh.setFormatter(new FormattedLoggerMessage());
           log.addHandler(fh);
           log.setUseParentHandlers(false);
           log.setLevel(Level.ALL);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	  }
	  
	  @After("execution(public void setupDestructor_(..))")
	  public void setupLoggerHandlerDestructor_(JoinPoint p) {
	        try {
	            FileHandler fh = new FileHandler(p.getArgs()[0] + ".txt");
                fh.setFilter(new MissileLauncherDestructorFilter((Destructor_) p.getArgs()[0]));
	            fh.setFormatter(new FormattedLoggerMessage());
	            log.addHandler(fh);
	            
	            if(War.isConsoleGame()) {
	                ConsoleHandler ch = new ConsoleHandler();
	                ch.setFilter(new MissileLauncherDestructorFilter((Destructor_) p.getArgs()[0]));
	                ch.setFormatter(new FormattedLoggerMessage());
	                log.addHandler(ch);
	            }
	        }catch (Exception e) {
			}
	  }	
	  
	  @After("execution(public void setupDestructor(..))")
	  public void setupLoggerHandlerDestructor(JoinPoint p) {
	        try {
	            FileHandler fh = new FileHandler(p.getArgs()[0] + ".txt");
                fh.setFilter(new MissileDestructorFilter((Destructor) p.getArgs()[0]));
	            fh.setFormatter(new FormattedLoggerMessage());
	            log.addHandler(fh);
	            
	            if(War.isConsoleGame()) {
	                ConsoleHandler ch = new ConsoleHandler();
	                ch.setFilter(new MissileDestructorFilter((Destructor) p.getArgs()[0]));
	                ch.setFormatter(new FormattedLoggerMessage());
	                log.addHandler(ch);
	            }
	        }catch (Exception e) {
			}
	  }
	  
	  @After("execution(public void setupLauncher(..))")
	  public void setupLoggerHandlerLauncher(JoinPoint p) {
	        try {
	            FileHandler fh = new FileHandler(p.getArgs()[0] + ".txt");
                fh.setFilter(new LauncherFilter((Launcher) p.getArgs()[0]));
	            fh.setFormatter(new FormattedLoggerMessage());
	            log.addHandler(fh);
	            
	            if(War.isConsoleGame()) {
	                ConsoleHandler ch = new ConsoleHandler();
	                ch.setFilter(new LauncherFilter((Launcher) p.getArgs()[0]));
	                ch.setFormatter(new FormattedLoggerMessage());
	                log.addHandler(ch);
	            }
	        }catch (Exception e) {
			}
	  }	
	  
	  
	  
	    private class MissileLauncherDestructorFilter implements Filter {

	        private Destructor_ destructor;

	        public MissileLauncherDestructorFilter(Destructor_ destructor) {
	            this.destructor = destructor;
	        }

	        @Override
	        public boolean isLoggable(LogRecord rec) {
	            if (rec.getMessage().startsWith("Launcher destructor: "+destructor.getType()))
	                return true;
	            else
	                return false;
	        }

	    }
	    
	    private class MissileDestructorFilter implements Filter {

	        private Destructor destructor;

	        public MissileDestructorFilter(Destructor destructor) {
	            this.destructor = destructor;
	        }

	        @Override
	        public boolean isLoggable(LogRecord rec) {
	            if (rec.getMessage().startsWith("Missile destructor: "+ destructor.getId()))
	                return true;
	            else
	                return false;
	        }

	    }
	    
	    private class LauncherFilter implements Filter {

	        private Launcher launcher;

	        public LauncherFilter(Launcher launcher) {
	            this.launcher = launcher;
	        }

	        @Override
	        public boolean isLoggable(LogRecord rec) {
	        	if(rec.getMessage().startsWith("Launcher " + launcher.getId()))
	                return true;
	            else
	                return false;
	        }

	    }
	    
	    class FormattedLoggerMessage extends Formatter {

	        @Override
	        public String format(LogRecord rec) {
	            StringBuffer buf = new StringBuffer(1000);
	            LocalDateTime localDate = LocalDateTime.now();//For reference
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	            String formattedString = localDate.format(formatter);
	            buf.append(formattedString + ": \t");
	            buf.append(formatMessage(rec));
	            buf.append("\n");
	            return buf.toString();
	        }
	    }

}
