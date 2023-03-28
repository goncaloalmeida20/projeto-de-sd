package RMISearchModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerInfo implements Serializable {
    public final List<ClientInfo> cIList;

    public int cAllCounter;

    public ServerInfo() {
        this.cIList = Collections.synchronizedList(new ArrayList<>());
        this.cAllCounter = 0;
    }

    public ClientInfo clientInfoById(int id){
        System.out.println(id + " " + cIList.size());
        return cIList.get(id - 1);
    }
}
