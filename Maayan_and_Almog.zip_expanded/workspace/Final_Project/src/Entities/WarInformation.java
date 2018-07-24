package Entities;

import java.util.Observable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

@SuppressWarnings("deprecation")
public class WarInformation extends Observable {
    private int missilesLaunched = 0;
    private int missilesDestructed = 0;
    private int missilesHit = 0;
    private int missilesMissed = 0;
    private int missilesLaunchersDestructed = 0;
    private int totalEconomicDamage = 0;
    private final Logger logger = Logger.getLogger("WarGame");
    public enum eCALLER_FUNCTION {
                                  INCREMENT_MISSILES_LAUNCHED,
                                  INCREMENT_MISSILES_MISSED,
                                  INCREMENT_MISSILES_DESTRUCTED,
                                  INCREMENT_MISSILES_HIT,
                                  INCREMENT_MISSILES_LAUNCHERS_DESTRUCTED,
                                  INCREMENT_TOTAL_ECONOMIC_DAMAGE
                                }

    public WarInformation() {
        try {
            FileHandler fh = new FileHandler("log.txt");
            fh.setFormatter(new FormattedLoggerMessage());
            addLoggerHandler(fh);
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void incrementMissilesLaunched() {
        missilesLaunched++;
        notifyAllObserver(eCALLER_FUNCTION.INCREMENT_MISSILES_LAUNCHED);
    }

    public synchronized void incrementMissilesMissed() {
        missilesMissed++;
        notifyAllObserver(eCALLER_FUNCTION.INCREMENT_MISSILES_MISSED);
    }

    public synchronized void incrementMissilesDestructed() {
        missilesDestructed++;
        notifyAllObserver(eCALLER_FUNCTION.INCREMENT_MISSILES_DESTRUCTED);
    }

    public synchronized void incrementMissilesHit() {
        missilesHit++;
        notifyAllObserver(eCALLER_FUNCTION.INCREMENT_MISSILES_HIT);
    }

    public synchronized void incrementMissilesLaunchersDestructed() {
        missilesLaunchersDestructed++;
        notifyAllObserver(eCALLER_FUNCTION.INCREMENT_MISSILES_LAUNCHERS_DESTRUCTED);
    }

    public synchronized void incrementTotalEconomicDamage(int damageCost) {
        if (damageCost >= 0) {
            totalEconomicDamage += damageCost;
        }

        notifyAllObserver(eCALLER_FUNCTION.INCREMENT_TOTAL_ECONOMIC_DAMAGE);
    }

    public void notifyAllObserver(eCALLER_FUNCTION callerFunction) {
        setChanged();
        notifyObservers(callerFunction);
    }

    @Override
    public String toString() {
        return  "\nMissiles Launched:              " + missilesLaunched            +
                "\nMissiles Destructed:            " + missilesDestructed          +
                "\nMissiles Hit:                   " + missilesHit                 +
                "\nMissile Missed:                 " + missilesMissed              +
                "\nMissiles Launchers Destructed:  " + missilesLaunchersDestructed +
                "\nTotal Economic Damage:          " + totalEconomicDamage         +
                "\n";
    }

    public int getMissilesLaunched() {
        return missilesLaunched;
    }

    public int getMissilesDestructed() {
        return missilesDestructed;
    }

    public int getMissilesHit() {
        return missilesHit;
    }

    public int getMissilesLaunchersDestructed() {
        return missilesLaunchersDestructed;
    }

    public int getTotalEconomicDamage() {
        return totalEconomicDamage;
    }

    public int getMissilesMissed() {
        return missilesMissed;
    }

    public Logger getLogger() {
        return logger;
    }

    public void addLoggerHandler(StreamHandler handler) {
        logger.addHandler(handler);
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
