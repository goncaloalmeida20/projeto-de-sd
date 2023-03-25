package classes;

import java.util.ArrayList;
import java.util.List;

public class Page {
    public String url, title, citation;
    public List<String> words;
    public final List<String> links;

    public Page(){
        url = null;
        title = null;
        citation = null;
        words = new ArrayList<>();
        links = new ArrayList<>();
    }

    public Page(String url, String title, String citation, List<String> words, List<String> links) {
        this.url = url;
        this.title = title;
        this.citation = citation;
        this.words = new ArrayList<>(words);
        this.links = new ArrayList<>(links);
    }

    public void addWord(String word){
        if(citation == null) citation = word;
        else citation += " " + word;
        words.add(word);
    }

    public void addLink(String link){
        links.add(link);
    }

    public int n_links(){
        return links.size();
    }

    public String multicastString(){
        StringBuilder sb = new StringBuilder();
        sb.append("type|url;url_item|").append(url).append(";");
        sb.append("type|title;title_item|").append(title).append(";");
        sb.append("type|citation;citation_item|").append(citation).append(";");
        sb.append("type|word_list;item_count|").append(words.size()).append(";");
        for(int i = 0; i < words.size(); i++){
            sb.append("item_").append(i).append("|").append(words.get(i)).append(";");
        }
        /*sb.append("type|url_list;item_count|").append(links.size()).append(";");
        for(int i = 0; i < links.size(); i++){
            sb.append("item_").append(i).append("|").append(links.get(i)).append(";");
        }*/
        return sb.toString();
    }
}
