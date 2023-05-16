package com.example.webserver;

import RMISearchModule.SearchModuleC_S_I;
import classes.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class RMIWrapper {
    private static final Logger logger = LoggerFactory.getLogger(RMIWrapper.class);
    private static final long TIMEOUT = 10000, RETRY_INTERVAL = 1000;
    private SearchModuleC_S_I searchC;

    private Semaphore RMISem;
    public RMIWrapper(Semaphore RMISem) throws RemoteException {
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        this.RMISem = RMISem;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                Registry registry = LocateRegistry.getRegistry("localhost", 7004);
                searchC = (SearchModuleC_S_I) registry.lookup("127.0.0.1");
                return;
            }
            catch(Exception e){
                logger.info("Server is not responding, retrying...");

            }
            finally {
                RMISem.release();
            }

            try{
                Thread.sleep(RETRY_INTERVAL);
            }catch(Exception e){
                logger.info("Retries interrupted");
            }
        }
        throw new RemoteException("Server timed out");
    }

    public int register(String username, String password) throws RemoteException {
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.register(username, password);
            }
            catch(Exception e){
                logger.info("Server is not responding, retrying...");
            }
            finally{
                RMISem.release();
            }
            try{
                Thread.sleep(RETRY_INTERVAL);
            }catch(Exception e){
                logger.info("Retries interrupted");
            }
        }
        throw new RemoteException("Server timed out");
    }
    public int login(String username, String password, int id) throws RemoteException{
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.login(username, password, id);
            }
            catch(Exception e){
                logger.info("Server is not responding, retrying...");
            }
            finally{
                RMISem.release();
            }

            try{
                Thread.sleep(RETRY_INTERVAL);
            }catch(Exception e){
                logger.info("Retries interrupted");
            }
        }
        throw new RemoteException("Server timed out");
    }
    public void indexUrl(String url) throws RemoteException{
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                //logger.info("Test " + RMISem.availablePermits());
                //Thread.sleep(100000);
                searchC.indexUrl(url);
                return;
            }
            catch(Exception e){
                logger.info("Server is not responding, retrying...");
            }
            finally{
                RMISem.release();
            }

            try{
                Thread.sleep(RETRY_INTERVAL);
            }catch(Exception e){
                logger.info("Retries interrupted");
            }
        }
        throw new RemoteException("Server timed out");
    }
    public ArrayList<Page> search(int termCount, String[] terms, int n_page) throws RemoteException{
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.search(termCount, terms, n_page);
            }
            catch(Exception e){
                logger.info("Server is not responding, retrying...");
            }
            finally{
                RMISem.release();
            }

            try{
                Thread.sleep(RETRY_INTERVAL);
            }catch(Exception e){
                logger.info("Retries interrupted");
            }
        }
        throw new RemoteException("Server timed out");
    }
    public ArrayList<Page> searchPages(String url, int n_page, int id, boolean logged) throws RemoteException{
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.searchPages(url, n_page, id, logged);
            }
            catch(Exception e){
                logger.info("Server is not responding, retrying...");
            }
            finally{
                RMISem.release();
            }

            try{
                Thread.sleep(RETRY_INTERVAL);
            }catch(Exception e){
                logger.info("Retries interrupted");
            }
        }
        throw new RemoteException("Server timed out");
    }
    public Map<Integer, Integer> admin() throws RemoteException{
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.admin();
            }
            catch(Exception e){
                logger.info("Server is not responding, retrying...");
            }
            finally{
                RMISem.release();
            }

            try{
                Thread.sleep(RETRY_INTERVAL);
            }catch(Exception e){
                logger.info("Retries interrupted");
            }
        }
        throw new RemoteException("Server timed out");
    }
    public int logout(int id) throws RemoteException{
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.logout(id);
            }
            catch(Exception e){
                logger.info("Server is not responding, retrying...");
            }
            finally{
                RMISem.release();
            }

            try{
                Thread.sleep(RETRY_INTERVAL);
            }catch(Exception e){
                logger.info("Retries interrupted");
            }
        }
        throw new RemoteException("Server timed out");
    }

    public List<HashMap<Integer, String>> getTopTenSeaches() throws RemoteException{
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.getTopTenSeaches();
            }
            catch(Exception e){
                logger.info("Server is not responding, retrying...");
            }
            finally{
                RMISem.release();
            }

            try{
                Thread.sleep(RETRY_INTERVAL);
            }catch(Exception e){
                logger.info("Retries interrupted");
            }
        }
        throw new RemoteException("Server timed out");
    }
}
