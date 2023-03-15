package IndexStorageBarrels;

import java.util.*;

public class BarrelManager {
    public static final int NUMBER_OF_BARRELS = 2;

    private static List<Barrel> barrelList;
    public static void main(String[] args) {
        barrelList = new ArrayList<Barrel>();
        for (int i = 0; i < NUMBER_OF_BARRELS; i++) {
            barrelList.add(new Barrel(i));
        }
    }
}
