package orego.cluster;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SearchController extends Remote {
	
	public static final String SEARCH_CONTROLLER_NAME = "orego.cluster.SearchController";
	
	/** Add a new searcher to be used. */
	void addSearcher(TreeSearcher searcher) throws RemoteException;
	
	/** Accepts search results from a given Searcher. */
	void acceptResults(TreeSearcher searcher, int[] runs, int[] wins) throws RemoteException; 
}
