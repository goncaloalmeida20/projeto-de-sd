package URLQueue;

import java.io.Serializable;

public class URLItem implements Serializable {
    public String url;
    public int recursion_count;

    public final int MAX_RECURSION = 2;

    public URLItem(String url){
        this.url = url;
        recursion_count = MAX_RECURSION;
    }

    public URLItem(String url, int recursion_count){
        this.url = url;
        this.recursion_count = recursion_count;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof URLItem)) return false;
        return url.equals(((URLItem)obj).url);
    }

    @Override
    public String toString() {
        return url;
    }
}
