package Downloaders;

import classes.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.StringTokenizer;

public class JsoupAnalyzer{
    public static final int WORD_LIMIT = 100;
    public JsoupAnalyzer(){

    }

    public Page pageAnalyzer(String url){
        try{
            //connect to the url
            Document doc = Jsoup.connect(url).get();

            //get the page
            Page p = new Page();
            p.url = url;
            p.title = doc.title();
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int wordCount = 0;
            //get at max WORD_LIMIT words
            while(tokens.hasMoreElements() && wordCount++ < WORD_LIMIT)
            {
                p.addWord(tokens.nextToken());
            }
            //add all the links in the page
            Elements links = doc.select("a[href]");
            for(Element link: links){
                p.addLink(link.attr("abs:href"));
            }
            return p;
        }
        catch(Exception e){
            System.out.println("JsoupAnalyzer exception: " + e.getMessage());
        }
        return null;
    }
}