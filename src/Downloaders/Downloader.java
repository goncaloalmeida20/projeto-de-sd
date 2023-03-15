package Downloaders;

import java.rmi.registry.LocateRegistry;

import URLQueue.URLQueueStarter;
import URLQueue.URLQueue_I;
import classes.Page;

public class Downloader implements Runnable{
    public Thread t;
    public int id;

    public Downloader(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    public void run(){
        System.out.println("Downloader " + id + " started!");
        try{
            URLQueue_I uq = (URLQueue_I) LocateRegistry
                    .getRegistry(URLQueueStarter.URLQUEUE_PORT)
                    .lookup(URLQueueStarter.URLQUEUE_NAME);
            while(true){
                //Wait for new URL to arrive at the URL Queue
                String url = uq.nextURL();

                System.out.println("Downloader " + id + " starting to analyze" + url);

                //Analyze the URL using Jsoup
                JsoupAnalyzer jsa = new JsoupAnalyzer();
                Page resultingPage = jsa.pageAnalyzer(url);

                System.out.println("Received text: " + resultingPage.citation);

                //Add links in the page to the Queue
                for (String link : resultingPage.links) {
                    System.out.println("Adding link " + link + " to the list");
                    //uq.addURL(link, false);
                }
            }
        }
        catch(Exception e){
            System.out.println("Downloader " + id + " exception: " + e.getMessage());
        }
    }
}
