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


    /**
     * Adds a word to the citation, filters the word
     * @param word word to add to the list and to the citation
     */
    public void addWord(String word){
        if(citation == null) citation = word;
        else citation += " " + word;
        String lowerCase = word.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        if(lowerCase.length() > 0 && !words.contains(lowerCase))
            words.add(lowerCase);
    }

    /**
     * Adds a link to the link list
     * @param link link to add to the list
     */
    public void addLink(String link){
        if(!links.contains(link))
            links.add(link);
    }


    /**
     * Generates a formatted string with this page's info to be used in the multicast communication
     * @return the formatted string
     */
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


    /**
     * decodes the formatted string and adds the info to this page
     * @param multicastString the formatted string
     */
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
                        this.words.add(splitString[++i].replace("||","|").replace("|0",";"));
                    }
                    break;
                case "url_list":
                    count = Integer.parseInt(splitString[i]);
                    for(int l = 0; l < count; l++){
                        this.links.add(splitString[++i].replace("||","|").replace("|0",";"));
                    }
                    break;
                default:
                    System.out.println("Multicast String parse error");
                    break;
            }
        }
    }
}
