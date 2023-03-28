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
            Document doc = Jsoup.connect(url).get();
            Page p = new Page();
            p.url = url;
            p.title = doc.title();
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int wordCount = 0;
            while(tokens.hasMoreElements() && wordCount++ < WORD_LIMIT)
            {
                p.addWord(tokens.nextToken());
            }
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