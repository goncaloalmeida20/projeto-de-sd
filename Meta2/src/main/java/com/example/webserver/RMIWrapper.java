package com.example.webserver;

import RMISearchModule.SearchModuleC_S_I;
import classes.AdminInfo;
import classes.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
    private static String REGISTRY_ADDRESS_FILE = "registry_address.txt";
    private SearchModuleC_S_I searchC;

    private Semaphore RMISem;
    public RMIWrapper(Semaphore RMISem) throws RemoteException {
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        this.RMISem = RMISem;
        while(System.currentTimeMillis() < timeout_time){
            try{
                String regAddr;
                try{
                    File regAddrF = new File(REGISTRY_ADDRESS_FILE);
                    FileReader fr = new FileReader(regAddrF);
                    BufferedReader br = new BufferedReader(fr);
                    regAddr = br.readLine();
                    fr.close();
                }
                catch(Exception e){
                    logger.info("Error reading " + REGISTRY_ADDRESS_FILE + " file.");
                    return;
                }

                RMISem.acquire();
                Registry registry = LocateRegistry.getRegistry(regAddr, 7004);
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

    public int register(String username, String password, String sessionId) throws RemoteException {
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
                //Thread.sleep(10000);
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

    public int maven_register(String username, String password, String s_id) throws RemoteException {
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.maven_register(username, password, s_id);
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

    public int maven_login(String username, String password, String s_id) throws RemoteException{
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.maven_login(username, password, s_id);
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

    public int maven_logout(String s_id) throws RemoteException{
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.maven_logout(s_id);
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

    public ArrayList<Page> maven_search(int termCount, String[] terms) throws RemoteException{
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.maven_search(termCount, terms);
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

    public List<Page> maven_searchPages(String url) throws RemoteException {
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.searchPages(url,1, 1, true);
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

    public AdminInfo maven_admin() throws RemoteException {
        long timeout_time = System.currentTimeMillis() + TIMEOUT;
        while(System.currentTimeMillis() < timeout_time){
            try{
                RMISem.acquire();
                return searchC.maven_admin();
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
