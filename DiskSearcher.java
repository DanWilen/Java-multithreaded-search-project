/*
 * DiskSearcher.java
 * Name - Dan Wilensky
 */

import java.io.File;
import java.io.IOError;
import java.util.concurrent.TimeUnit;

/**
 * Main application class. This application searches for all files under some given
 * path with the required extension (that is given as an argument to the program).
 * All files found are copied to some specific directory.
 */
public class DiskSearcher {
    public static final int DIRECTORY_QUEUE_CAPACITY = 50;//Capacity of the queue that holds the directories to be searched
    public static final int RESULTS_QUEUE_CAPACITY = 50;//Capacity of the queue that holds the files found
    public static SynchronizedQueue<String> milestonesQueue = new SynchronizedQueue<String>(DIRECTORY_QUEUE_CAPACITY);
    private static int idCounter = 0; // the id of a thread

    /**
     * Main method.
     * Reads arguments from command line and starts the search.
     * @param args Command line arguments, 6 parameters:
     * args[0] - milestoneQueueFlag - boolean - true/false
     * args[1] - extension - String - file extension to find
     * args[2] - rootDirName - String - absolute path of the root directory
     * args[3] - destDirName - String - absolute path of the destination directory
     * args[4] - numOfSearchers - int - number of searches
     * args[5] - numOfCopiers - int - number of copiers
   **/

    public static void main(String[] args) {

        // Sets the time of execution for calculating the duration of the program.
        long startTime = System.nanoTime();

        // Check if there are enough arguments
        if (args.length != 6) {
            System.err.println("You have entered " + args.length +" arguments it should be 6 arguments instead.\nexiting.");
            return;
        }
        //Parsing
        boolean milestoneQueueFlag = Boolean.parseBoolean(args[0]);
        String extension = args[1];
        String rootDirName = args[2];
        String destDirName = args[3];
        int numOfSearchers = Integer.parseInt(args[4]);
        int numOfCopiers = Integer.parseInt(args[5]);

        // put the important action in the milestonesQueue as the first action
        if (milestoneQueueFlag){
            milestonesQueue.enqueue("General, program has started the search\n");
        }else {
            milestonesQueue = null;
        }

        File rootDir;
        File destDir;

        try {
            rootDir = new File(rootDirName);
            destDir = new File(destDirName);
            if (!rootDir.exists()) {
                System.err.println("Root directory not found. exiting.");
                return;
            }
            // if destination directory doesnt exist, create one
            if (!destDir.exists()) {
                destDir.mkdir();
            }
        } catch (IOError e) {
            System.err.println("Not a valid directories " + args[1] + " " + args[2] + ".");
            return;
        }

        // Check if there are valid number of searchers and copiers
        if (numOfCopiers <= 0 || numOfSearchers <= 0) {
            System.err.println("Please enter valid number of searchers and " +
                    "copiers. exiting.");
            return;
        }

        SynchronizedQueue<File> directoryQueue = new SynchronizedQueue<>(DIRECTORY_QUEUE_CAPACITY);
        SynchronizedQueue<File> resultsQueue = new SynchronizedQueue<>(RESULTS_QUEUE_CAPACITY);

        Thread scouter = new Thread(new Scouter(idCounter,directoryQueue, rootDir,milestonesQueue,milestoneQueueFlag));
        ++idCounter; // increase after creating a new thread
        scouter.start();


        // handling all the searchers
        Searcher[] searchers = new Searcher[numOfSearchers];
        Thread[] searcherThreads = new Thread[numOfSearchers];
        for (int i = 0; i < numOfSearchers; i++) {
            searchers[i] = new Searcher(idCounter,extension, directoryQueue, resultsQueue,milestonesQueue,milestoneQueueFlag);
            ++idCounter;// increase after creating a new thread
            searcherThreads[i] = new Thread(searchers[i]);
            searcherThreads[i].start();
        }

        // handling all the copiers
        Copier[] copiers = new Copier[numOfCopiers];
        Thread[] copierThreads = new Thread[numOfCopiers];
        for (int i = 0; i < numOfCopiers; i++) {
            copiers[i] = new Copier(idCounter,destDir, resultsQueue,milestonesQueue,milestoneQueueFlag);
            ++idCounter;// increase after creating a new thread
            copierThreads[i] = new Thread(copiers[i]);
            copierThreads[i].start();
        }

        // join scouter threads
        // will make sure that scouter(=Thread) is terminated before the next instruction is executed by the program.
        try {
            scouter.join();
        } catch (InterruptedException e) {
            System.err.println("Failed to join() the threads.");
            return;
        }
        // join all searchers and copiers threads
        try {
            for (int i = 0; i < searchers.length; i++) {
                searcherThreads[i].join(1);
            }
            for (int j = 0; j < copiers.length; j++) {
                copierThreads[j].join(1);
            }
        } catch (InterruptedException e) {
            System.err.println("Failed to join() the threads.");
            return;
        }

        // handling the milestonesQueue strings and print it in the console
        StringBuilder str = new StringBuilder();
        String check;
        check = milestonesQueue.dequeue();
        while((check) != null) {
            str.append(check);
            check = milestonesQueue.dequeue();
        }

        if (milestoneQueueFlag){
            System.out.println(str);
        }

        // calculating the duration of the program and print in the console
        long endTime = System.nanoTime();
        long durationInNano = (endTime - startTime);  //Total execution time in nano seconds
        long durationInMillis = TimeUnit.NANOSECONDS.toMillis(durationInNano);  //Total execution time in nano seconds

        System.out.println("Program Ends after " + durationInMillis + " milliseconds");
        System.exit(1);
    }

}
