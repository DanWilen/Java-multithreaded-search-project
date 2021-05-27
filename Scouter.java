/*
 * Scouter.java
 * Name - Dan Wilensky
 */

import java.io.File;

/**
 * A scouter thread This thread lists all sub-directories from a given root path.
 * Each sub-directory is enqueued to be searched for files by Searcher threads.
 */

public class Scouter implements Runnable {

    private final int id;
    private final boolean isMilestones;
    public SynchronizedQueue<String> milestonesQueue;
    private SynchronizedQueue<File> directoryQueue;
    private File rootDir;

    /**
     * Construnctor.
     * Initializes the scouter with a queue for the directories
     * to be searched and a root directory to start from.
     *
     * @param id - the id of the thread running the instance
     * @param directoryQueue - A queue for directories to be searched
     * @param root - Root directory to start from
     * @param milestonesQueue - a synchronizedQueue to write milestones to
     * @param isMilestones - indicating whether or not the running thread should write to the milestonesQueue
     *
     * */
    public Scouter(int id, SynchronizedQueue<java.io.File> directoryQueue,
                   java.io.File root, SynchronizedQueue<String> milestonesQueue, boolean isMilestones)    {
        this.id = id;
        this.isMilestones = isMilestones;
        this.milestonesQueue = milestonesQueue;
        this.directoryQueue = directoryQueue;
        this.rootDir = root;
    }

    /**
     * Starts the scouter thread. Lists directories under root directory and adds them to queue,
     * then lists directories in the next level and enqueues them and so on. This method begins by
     * registering to the directory queue as a producer and when finishes,
     * it unregisters from it. If the isMilestones was set in the constructor
     * (and therefore the milstonesQueue was sent to it as well,
     * it should write every "important" action to this queue.
     */
    @Override
    public void run() {
        this.directoryQueue.registerProducer();
        this.directoryQueue.enqueue(this.rootDir);

        // if not a directory
        if (!this.rootDir.isDirectory()) {
            System.err.println("There is no source directory to search in " + this.rootDir.getName() + "exiting.");
            return;
        }
        // put the important action in the milestonesQueue
        if (isMilestones) { //
            synchronized (this) {
                this.milestonesQueue.enqueue("Scouter on thread id " + this.id +
                        " : directory named " + rootDir.getName() + " was scouted\n");
            }
        }
        // call to all directories and sub directories
        addDirsAndSubdirs(this.rootDir);
        this.directoryQueue.unregisterProducer();
    }

    /**
     * This method lists all directories and subdirectories of a given file/directory
     * @param path the file as a root
     */
    private void addDirsAndSubdirs(File path) {
        try {
            File[] files = path.listFiles();
            for (File subfile : files) {
                if (subfile.isDirectory()) {
                    this.directoryQueue.enqueue(subfile);

                    // put the important action in the milestonesQueue
                    if (isMilestones) { //
                        synchronized (this) {
                            this.milestonesQueue.enqueue("Scouter on thread id " + this.id +
                                    ": directory named " + subfile.getName() + " was scouted\n");
                        }
                    }
                    // call to all directories and sub directories of
                    // that's how we scout everything
                    addDirsAndSubdirs(subfile);
                }
            }
        } catch (NullPointerException e){
            System.err.println("Failed to retrieve all directories and " +
                    "subdirectories.");
        }
    }
}
