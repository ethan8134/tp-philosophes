package diningphilosophers;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Philosopher extends Thread {
    private final static int delai = 1000;
    private final ChopStick myLeftStick;
    private final ChopStick myRightStick;
    private volatile boolean running = true;
    private final Random rand = new Random();

    public Philosopher(String name, ChopStick left, ChopStick right) {
        super(name);
        myLeftStick = left;
        myRightStick = right;
    }

    private void think() throws InterruptedException {
        System.out.println("M." + this.getName() + " pense... ");
        sleep(delai + rand.nextInt(delai + 1));
        System.out.println("M." + this.getName() + " arrête de penser");
    }

    private void eat() throws InterruptedException {
        System.out.println("M." + this.getName() + " mange...");
        sleep(delai + rand.nextInt(delai + 1));
    }

    /**
     * Tente d'acquérir first puis second. Si second indisponible, attend un court
     * timeout sur le monitor de 'second' pour voir s'il se libère ; sinon
     * relâche 'first' et renvoie false.
     */
    private boolean acquireWithShortWait(ChopStick first, ChopStick second) throws InterruptedException {
        // Tentative immédiate sur la première baguette
        if (!first.tryTake()) {
            return false;
        }

        // On tient maintenant 'first' : on va tenter la seconde.
        synchronized (second) {
            // tentative immédiate sur la seconde (synchronisée sur second)
            if (!second.tryTake()) {
                // pas disponible : on attend un court laps de temps sur second
                long timeout = 50 + rand.nextInt(151); // 50..200 ms (ajustable)
                try {
                    second.wait(timeout);
                } catch (InterruptedException ie) {
                    // si on est interrompu, on relâche la première avant de remonter l'exception
                    first.release();
                    throw ie;
                }
                // après attente, on retente
                if (!second.tryTake()) {
                    // toujours pas dispo -> on relâche la première et on abandonne
                    System.out.println(this.getName() + " n'a pas pu obtenir la 2e baguette, relâche " + first);
                    first.release();
                    return false;
                }
            }
        }

        // si on arrive ici, on a pris les deux baguettes (first + second)
        return true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                think();

                boolean ate = false;
                while (!ate && running) {
                    // Ordre aléatoire d'acquisition (garde du comportement original)
                    if (rand.nextInt(2) == 0) {
                        if (acquireWithShortWait(myLeftStick, myRightStick)) {
                            eat();
                            // libération dans l'ordre inverse ou quelconque
                            myRightStick.release();
                            myLeftStick.release();
                            ate = true;
                        }
                    } else {
                        if (acquireWithShortWait(myRightStick, myLeftStick)) {
                            eat();
                            myLeftStick.release();
                            myRightStick.release();
                            ate = true;
                        }
                    }

                    if (!ate) {
                        // Petite attente aléatoire avant de retenter (évite bouclage serré)
                        sleep(50 + rand.nextInt(200));
                    }
                }

            } catch (InterruptedException ex) {
                Logger.getLogger("Table").log(Level.SEVERE, "{0} Interrupted", this.getName());
                // sortir proprement
                running = false;
            }
        }
    }

    public void leaveTable() {
        running = false;
        this.interrupt(); // pour débloquer une attente éventuelle
    }
}
