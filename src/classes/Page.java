package classes;

import java.util.ArrayList;
import java.util.List;

public class Page {
    public String url, title, citation;
    public List<String> words;
    public List<String> links;

    public Page(){
        url = null;
        title = null;
        citation = null;
        words = new ArrayList<String>();
        links = new ArrayList<String>();
    }

    public Page(String url, String title, String citation, List<String> words, List<String> links) {
        this.url = url;
        this.title = title;
        this.citation = citation;
        this.words = new ArrayList<String>(words);
        this.links = new ArrayList<String>(links);
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
}
