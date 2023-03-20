package Downloaders;

import java.rmi.registry.LocateRegistry;

import URLQueue.URLItem;
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
            JsoupAnalyzer jsa = new JsoupAnalyzer();
            while(true){
                //Wait for new URL to arrive at the URL Queue
                URLItem urlItem = uq.nextURL();
                String url = urlItem.url;
                int next_recursion_count = urlItem.recursion_count - 1;

                System.out.println("Downloader " + id + " starting to analyze " + url + " with recursion count " +
                        next_recursion_count);

                //Analyze the URL using Jsoup
                Page resultingPage = jsa.pageAnalyzer(url);
                if(resultingPage == null) continue;
                System.out.println("Received text: " + resultingPage.citation);

                if(next_recursion_count > 0){
                    //Add links in the page to the Queue
                    for (String link : resultingPage.links) {
                        System.out.println("Adding link " + link + " to the list");
                        uq.addURLRecursively(link, next_recursion_count);
                    }
                }
                synchronized(DownloaderManager.pageList){
                    while(DownloaderManager.pageList.size() >= DownloaderManager.MAX_PAGE_LIST_SIZE){
                        DownloaderManager.pageList.wait();
                    }
                    DownloaderManager.pageList.add(resultingPage);
                    DownloaderManager.pageList.notify();
                }

            }
        }
        catch(Exception e){
            System.out.println("Downloader " + id + " exception: " + e.getMessage());
        }
    }
}
