package MODEL;

public class Client {

    private final int id;
    private boolean serviceStarted;
    private final int arrivalTime;
    private int serviceTime;
    private int waitingTime;
    private int initialServiceTime;

    public Client(int id, int arrivalTime, int serviceTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
        this.initialServiceTime=serviceTime;
        this.waitingTime=0;
        this.serviceStarted=false;
    }
   public void setWaitingTime(int waitingTime)
   {
       this.waitingTime=waitingTime;
   }
   public int getInitialServiceTime()
   {
       return initialServiceTime;
   }
    public void setServiceStarted(boolean started) {
        this.serviceStarted = started;
    }
    public boolean isServiceStarted() {
        return this.serviceStarted;
    }
    public int getId() {
        return id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getServiceTime() {
        return serviceTime;
    }
    public int getWaitingTime() {
        return waitingTime;
    }
    public void decrementServiceTime() {
        if (serviceTime > 0) {
            serviceTime--;
        }
    }
}
