package diningphilosophers;

public class ChopStick {
    private static int stickCount = 0;
    private boolean iAmFree = true;
    private final int myNumber;

    public ChopStick() {
        myNumber = ++stickCount;
    }

    synchronized public boolean tryTake() {
        if (!iAmFree) return false;
        iAmFree = false;
        System.out.println("baguette " + myNumber + " prise (try)");
        return true;
    }

    synchronized public void release() {
        // assert !iAmFree;
        iAmFree = true;
        System.out.println("baguette " + myNumber + " relâchée");
        notifyAll(); // prévient ceux qui attendent cette baguette
    }

    @Override
    public String toString() {
        return "baguette #" + myNumber;
    }
}
