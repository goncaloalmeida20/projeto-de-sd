package RMISearchModule;

import IndexStorageBarrels.BarrelModule_S_I;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerInfo implements Serializable {
    public final List<ClientInfo> cIList;
    public final List<BarrelModule_S_I> barrels;

    public int cAllCounter;
    public int bAllCounter;

    public ServerInfo() {
        this.cIList = Collections.synchronizedList(new ArrayList<>());
        this.barrels = Collections.synchronizedList(new ArrayList<>());
        this.cAllCounter = 0;
        this.bAllCounter = 0;
    }

    public ClientInfo clientInfoById(int id){
        System.out.println(id + " " + cIList.size());
        return cIList.get(id - 1);
    }
}
