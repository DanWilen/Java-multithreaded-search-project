/*
 * Name - Dan Wilensky
 */

import java.io.File;

/**
 * A searcher thread. Searches for files that end with
 * a specific extension in all directories listed in a directory queue.
 */
public class Searcher implements Runnable {

    private final int id;
    private final boolean isMilestones;
    public SynchronizedQueue<String> milestonesQueue;
    private String extension;
    SynchronizedQueue<File> directoryQueue;
    SynchronizedQueue<File> resultsQueue;

    /**
     * Constructor.
     * Initializes the searcher thread.
     * @param id  - of the thread running the instance
     * 		  extension - wanted extension
     * 		  directoryQueue - A queue with directories to search in (as listed by the scouter)
     * 		  resultsQueue - A queue for files found (to be copied by a copier)
     * 		  milestonesQueue a synchronizedQueue to write milestones to
     * 		  isMilestones - indicating whether or not the running thread should write to the milestonesQueue
     */


    public Searcher(int id, java.lang.String extension,
                    SynchronizedQueue<java.io.File> directoryQueue,
                    SynchronizedQueue<java.io.File> resultsQueue,
				SynchronizedQueue<String> milestonesQueue, boolean isMilestones){

        this.id = id;
        this.isMilestones = isMilestones;
        this.milestonesQueue = milestonesQueue;
        this.extension = extension;
        this.directoryQueue = directoryQueue;
        this.resultsQueue = resultsQueue;
    }

    /**
     * Runs the searcher thread. Thread will fetch a directory to search in from the directory queue,
     * then search all files inside it (but will not recursively search subdirectories!).
     * Files that a contain the and have the wanted extension are enqueued to the results queue.
     * This method begins by registering to the results queue as a producer and when finishes,
     * it unregisters from it. If the isMilestones was set in the constructor
     * (and therefore the milstonesQueue was sent to it as well,
     * it should write every "important" action to this queue.
     * */

    @Override
    public void run() {
        File dir;
        this.resultsQueue.registerProducer();
        // search for all files inside a directory
        while((dir = this.directoryQueue.dequeue()) != null) {
            File[] listOfFiles = dir.listFiles();
            try {
                // loop for all files in a directory
                for (File file : listOfFiles) {
                    // if its a file check its extension
                    if (file.isFile()) {
                        // if so put in queue
                        if (isWithFileExtension(file, this.extension)) {
                            this.resultsQueue.enqueue(file);

                            // put the important action in the milestonesQueue
                            if (isMilestones) { //
                                synchronized (this) {
                                    this.milestonesQueue.enqueue("Searcher on thread id " + this.id +
                                            " : file named " + file.getName() + " was found\n");
                                }
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                System.err.println("An exception has occurred while trying to " +
                        "search pattern.");
                return;
            }
        }
        this.resultsQueue.unregisterProducer();
    }

    /**
     * This method checks if a filename ends with a given extension
     * @param file The file we want to check
     * @param extension the extension of the file
     * @return true if the filename ends with a given extension. Otherwise, false.
     */

    private boolean isWithFileExtension(File file, String extension) {
        return file.getName().endsWith(extension);
    }
}
