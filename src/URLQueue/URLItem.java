package URLQueue;

public class URLItem {
    public String url;
    public boolean update_if_exists;

    public URLItem(String url){
        this.url = url;
        update_if_exists = true;
    }

    public URLItem(String url, boolean update_if_exists){
        this.url = url;
        this.update_if_exists = update_if_exists;
    }

    @Override
    public boolean equals(Object obj) {
        return url.equals(((URLItem)obj).url);
    }
}
