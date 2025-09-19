package diningphilosophers;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Philosopher extends Thread {
    private final static int delai = 1000;
    private final ChopStick myLeftStick;
    private final ChopStick myRightStick;
    private boolean running = true;
    private final Random rand = new Random();

    public Philosopher(String name, ChopStick left, ChopStick right) {
        super(name);
        myLeftStick = left;
        myRightStick = right;
    }

    private void think() throws InterruptedException {
        System.out.println("M."+this.getName()+" pense... ");
        sleep(delai+new Random().nextInt(delai+1));
        System.out.println("M."+this.getName()+" arrête de penser");
    }

    private void eat() throws InterruptedException {
        System.out.println("M."+this.getName() + " mange...");
        sleep(delai+new Random().nextInt(delai+1));
        //System.out.println("M."+this.getName()+" arrête de manger");
    }

    @Override
    public void run() {
        while (running) {
            try {
                think();

                boolean ate = false;
                while (!ate && running) {
                    // Phase 1 : tentative
                    if (myLeftStick.tryTake()) {
                        if (myRightStick.tryTake()) {
                            // Deux baguettes obtenues
                            eat();
                            myRightStick.release();
                            myLeftStick.release();
                            ate = true;
                        } else {
                            // Pas réussi à avoir la deuxième
                            myLeftStick.release();
                            // Phase 2 : attendre un peu avant de réessayer
                            sleep(100 + rand.nextInt(200));
                        }
                    } else {
                        // attendre un peu avant de réessayer
                        sleep(100 + rand.nextInt(200));
                    }
                }

            } catch (InterruptedException ex) {
                Logger.getLogger("Table").log(Level.SEVERE, "{0} Interrupted", this.getName());
            }
        }
    }


    // Permet d'interrompre le philosophe "proprement" :
    // Il relachera ses baguettes avant de s'arrêter
    public void leaveTable() {
        running = false;
    }

}
