package Downloaders;

import classes.Page;

public class DownloaderMulticast implements Runnable{
    public Thread t;
    public int id;
    public DownloaderMulticast(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }
    public void run(){
        try{
            while(true){
                synchronized (DownloaderManager.pageList){
                    while(DownloaderManager.pageList.size() == 0){
                        DownloaderManager.pageList.wait();
                    }
                    Page currentPage = DownloaderManager.pageList.remove(0);
                    System.out.println("DownloaderMulticast " + id + " sending page " + currentPage.url);
                    DownloaderManager.pageList.notify();
                }
            }
        }
        catch(Exception e){
            System.out.println("DownloaderMulticast " + id + " exception: " + e.getMessage());
        }
    }
}
