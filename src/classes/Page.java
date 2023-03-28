package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Page implements Serializable {
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

    public Page(String multicastString){
        url = null;
        title = null;
        citation = null;
        words = new ArrayList<>();
        links = new ArrayList<>();
        decodeMulticastString(multicastString);
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
        words.add(word.toLowerCase());
    }

    public void addLink(String link){
        links.add(link);
    }

    public int n_links(){
        return links.size();
    }

    public String multicastString(){
        StringBuilder sb = new StringBuilder();
        sb.append("url;").append(url.replace("|", "||").replace(";","|0")).append(";");
        sb.append("title;").append(title.replace("|", "||").replace(";","|0")).append(";");
        sb.append("citation;").append(citation.replace("|", "||").replace(";","|0")).append(";");
        sb.append("word_list;").append(words.size()).append(";");

        for(int i = 0; i < words.size(); i++){
            sb.append(words.get(i).replace("|", "||").replace(";","|0")).append(";");
        }

        sb.append("url_list;").append(links.size()).append(";");

        for(int i = 0, j = 0; i < links.size(); i++){
            sb.append(links.get(i).replace("|", "||").replace(";","|0")).append(";");
        }
        return sb.toString();
    }

    public void decodeMulticastString(String multicastString){
        String[] splitString = multicastString.split(";");
        String type;
        for (int i = 0; i < splitString.length; i++) {
            type = splitString[i];
            i++;
            int count;
            switch (type){
                case "url":
                    this.url = splitString[i].replace("||","|").replace("|0",";");
                    break;
                case "title":
                    this.title = splitString[i].replace("||","|").replace("|0",";");
                    break;
                case "citation":
                    this.citation = splitString[i].replace("||","|").replace("|0",";");
                    break;
                case "word_list":
                    count = Integer.parseInt(splitString[i]);
                    for(int w = 0; w < count; w++){
                        this.words.add(splitString[i+w].replace("||","|").replace("|0",";"));
                    }
                    i += count;
                    break;
                case "url_list":
                    count = Integer.parseInt(splitString[i]);
                    for(int l = 0; l < count; l++){
                        this.links.add(splitString[i+l].replace("||","|").replace("|0",";"));
                    }
                    i += count;
                    break;
                default:
                    System.out.println("Multicast String parse error");
                    break;
            }
        }
    }
}
