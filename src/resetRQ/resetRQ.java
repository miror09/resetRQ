/**
 * 
 */
package resetRQ;

import java.util.*;
import java.lang.System;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.replication.ReplicationJournalEntry;
import com.filenet.api.collection.ReplicationJournalEntrySet;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.collection.RepositoryRowSet;
import com.filenet.api.query.SearchScope;
import com.filenet.api.constants.RefreshMode;
import java.text.SimpleDateFormat;

/**
 * @author developer
 *
 */
public class resetRQ {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String sqlString = "select this from ReplicationJournalEntry";	

		/* parsing main arguments */
		if (args.length != 0)
			sqlString=args[0];

		if(!(sqlString.toLowerCase()).startsWith("select this from replicationjournalentry"))
		{
			System.out.println("\n\tNot a valid argument: '" + sqlString + "'\n");
			System.out.println("\tOnly one argument is alowed and it must start with string 'Select This from ReplicationJournalEntry'. Exiting ...");
			System.exit(1);	
		}


		CEConnection ce = null;	
		ObjectStore os1 = null;

		long startTime = 0;
		long endTime = 0;
		RepositoryRowSet rowSet = null;
		//String sqlString = "select this from ReplicationJournalEntry";

		try
	        {
			startTime = System.currentTimeMillis();
			SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy");

			ce = new CEConnection();
			System.out.println("\n\tGetting connection to FileNet CE -> Produkcia ...");
			ce.establishConnection("GCDAdmin","sys","FileNetP8","t3://st-srv1:7001/FileNet/Engine/");
			os1 = ce.fetchOS("Produkcia");
			System.out.println("\n\tConnection to FileNet CE -> ObjectStore" + os1.get_DisplayName() + " successful!");			
			
			
			System.out.println("\n\tSearching ...\n");
			SearchSQL sqlObject = new SearchSQL(sqlString);
			SearchScope searchScope = new SearchScope(os1);

			// Get all items in the replication queue.
			ReplicationJournalEntrySet rjeSet = (ReplicationJournalEntrySet)searchScope.fetchObjects(sqlObject, new Integer(1), null, Boolean.TRUE);

			int rowCount = 0;
			int deadCount = 0;
			Iterator iter = rjeSet.iterator();
			ReplicationJournalEntry rje;
		
			// Iterate replication queue items and reset RetryCount property for ReplicationJournalEntry objects.
			while (iter.hasNext())
			{
				rje = (ReplicationJournalEntry)iter.next();
				rje.refresh();
				if (rje.get_ClassDescription().get_SymbolicName().equals(ClassNames.REPLICATION_JOURNAL_ENTRY))
				{
					if (rje.get_RetryCount().equals(new Integer(0)) )
						deadCount++;
					rowCount++;
				}
			}
			endTime = System.currentTimeMillis();
			System.out.println("\n\tSUMMARY:\tNumber of dead records in Replication journal: " + deadCount);
			System.out.println("\t\t\tTotal number of records in Replication journal: " + rowCount);
			System.out.println("\t\t\tSearching time: " + (endTime - startTime) + " ms");


			System.out.print("\n\n\tWould you like to update? (Y/N) ... ");

			String input = System.console().readLine();

			if(!"Y".equalsIgnoreCase(input))
			{
				System.out.println("\n\tExiting ...\n");
				System.exit(0);
			}
			
			System.out.println("\n\tUpdating ...\n");		
			startTime = System.currentTimeMillis();

			// Get all items in the replication queue.
			rjeSet = (ReplicationJournalEntrySet)searchScope.fetchObjects(sqlObject, new Integer(1), null, Boolean.TRUE);

			rowCount = 0;
			iter = rjeSet.iterator();
		
			// Iterate replication queue items and reset RetryCount property for ReplicationJournalEntry objects.
			while (iter.hasNext())
			{
				rje = (ReplicationJournalEntry)iter.next();
				rje.refresh();
				if (rje.get_ClassDescription().get_SymbolicName().equals(ClassNames.REPLICATION_JOURNAL_ENTRY))
				{
					//System.out.print("\t" + ++rowCount + ": Creator: " + rje.get_Creator() + "\tDate Created: " + sdf.format(rje.get_DateCreated()) + "\tStatus: " + rje.get_ReplicationStatus() + "\tRetry count: " + rje.get_RetryCount());
					if (rje.get_RetryCount().equals(new Integer(0)) )
					{
						rje.set_RetryCount(new Integer(4));
						rje.save(RefreshMode.REFRESH);
						System.out.print("\r\t ---> Records updated: " + ++rowCount*100/deadCount + "%");
//						try {
//							Thread.sleep(1000);
//						} catch(Exception e) {
//							System.out.println("Exception caught");
//						}
					}
				}
			}
			endTime = System.currentTimeMillis();
			System.out.println("\n\n\tSUMMARY:\tUpdated: " + rowCount + " record. Total processing time: " + (endTime - startTime) + " ms");

	        }
	        catch (EngineRuntimeException e)
	        {
	        	if(e.getExceptionCode() == ExceptionCode.E_NOT_AUTHENTICATED)
	        	{
	        		System.out.println("Invalid login credentials supplied - please try again " + e.getMessage());
	        	}
	        	else if(e.getExceptionCode() == ExceptionCode.API_UNABLE_TO_USE_CONNECTION)
	        	{
	        		System.out.println("Unable to connect to server.  Please check to see that URL is correct and server is running" + e.getMessage());
	        	}
	        	else
	        	{
	        		System.out.println("ERR: " + "user" + "pass" + e.getMessage());
	        	}
	            	e.printStackTrace();
	        }
		}
}
