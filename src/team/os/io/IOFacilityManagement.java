package team.os.io;

public class IOFacilityManagement {
    public static void main(String[] args) {
        IOFacility test = new IOFacility("test");
        test.getFacility();
        System.out.println("Facility:" + test.facilityTotalNumberChart);
        test.initState();
        System.out.println("State:" + test.stateChart);
    }
}
