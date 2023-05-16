package RMISearchModule;

import Downloaders.AdminDownloader_S_I;
import IndexStorageBarrels.BarrelModule_S_I;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ServerInfo implements Serializable {
    public final List<ClientInfo> cIList;

    public final List<MavenClientInfo> mCIList;

    public final List<BarrelModule_S_I> barrels;

    public final List<AdminDownloader_S_I> adminDownloaders;

    public int cAllCounter;
    public int bAllCounter;

    public ServerInfo() {
        this.cIList = Collections.synchronizedList(new ArrayList<>());
        this.mCIList = Collections.synchronizedList(new ArrayList<>());
        this.barrels = Collections.synchronizedList(new ArrayList<>());
        this.adminDownloaders = Collections.synchronizedList(new ArrayList<>());
        this.cAllCounter = 0;
        this.bAllCounter = 0;
    }

    public ClientInfo clientInfoById(int id){
        return cIList.get(id - 1);
    }

    public MavenClientInfo clientInfoByS_id(String s_id){
        for(MavenClientInfo c: mCIList){
            if (c.s_id.equals(s_id)){
                return c;
            }
        }
        return null;
    }
}
