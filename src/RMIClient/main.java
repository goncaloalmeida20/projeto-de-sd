/*import java.util.*;

class main{
    public static void main(String[]args){

    }
}

class UrlList implements Serializable{
    private void writeObject(ObjectOutputStream out) throws IOException { out.defaultWriteObject(); }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException { in.defaultReadObject(); }

    private int item_count;
    private String[] item_names;

    public UrlList(int item_count, String[] item_names){
        this.item_count = item_count;
        this.item_names = new String[item_count];

        System.arraycopy(item_names, item_count, this.item_names, 0, item_count);
    }

    public int getItem_count() {
        return item_count;
    }

    public String[] getItem_names() {
        return item_names;
    }
}

class SearchResponse implements Serializable{
    private void writeObject(ObjectOutputStream out) throws IOException { out.defaultWriteObject(); }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException { in.defaultReadObject(); }

    private String[] urls;

    public URL_List(String[] urls){
        int len_urls = urls.length;
        int[] copiedArray = Arrays.copyOf(urls, len_urls);
    }
    public String[] getUrls() {
        return urls;
    }
}*/