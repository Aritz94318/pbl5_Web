package edu.mondragon.we2.pinkalert.model;

public class GlobalUpdateRequest {
    private int numPatients;
    private int numDoctors;
    private int numMachines;

    public int getNumPatients() {
        return numPatients;
    }

    public void setNumPatients(int numPatients) {
        this.numPatients = numPatients;
    }

    public int getNumDoctors() {
        return numDoctors;
    }

    public void setNumDoctors(int numDoctors) {
        this.numDoctors = numDoctors;
    }

    public int getNumMachines() {
        return numMachines;
    }

    public void setNumMachines(int numMachines) {
        this.numMachines = numMachines;
    }

public GlobalUpdateRequest (int numPatients,int numDoctors, int numMachines){

    this.numPatients=numPatients;
    this.numDoctors=numDoctors;
    this.numMachines=numMachines;
}


}
