/*
 * Copier.java
 * Name - Dan Wilensky
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A copier thread. Reads files to copy from a queue and copies them to the given destination.
 */
public class Copier implements Runnable {
    public static final int COPY_BUFFER_SIZE = 4096; //Size of buffer used for a single file copy process
    private SynchronizedQueue<File> resultsQueue;
    private File destDir;
    private final int id;
    private final boolean isMilestones;
    public SynchronizedQueue<String> milestonesQueue;

    /**
     * Constructor. Initializes the worker with a destination directory and a queue of files to copy.
     * @param id - the id of the thread running the specific instance,
     * @param destination - Destination directory
     * @param resultsQueue - Queue of files found, to be copied milestonesqueue a synchronizedQueue to write milestones to
     * @param milestonesQueue - the global string queue of every important action.
     * @param isMilestones - indicating whether or not the running thread should write to the milestonesQueue
     */
    public Copier(int id, java.io.File destination,
                  SynchronizedQueue<java.io.File> resultsQueue,
                  SynchronizedQueue<String> milestonesQueue, boolean isMilestones){
        this.id = id;
        this.isMilestones = isMilestones;
        this.milestonesQueue = milestonesQueue;
        this.destDir = destination;
        if (!this.destDir.isDirectory()) {
            System.err.println("There is no destination directory to copy to." +
                    " exiting.");
            return;
        }
        this.resultsQueue = resultsQueue;
    }

    /**
     * Runs the copier thread. Thread will fetch files from queue and copy them, one after each other,
     * to the destination directory. When the queue has no more files, the thread finishes.
     * If the isMilestones was set in the constructor
     * (and therefore the milstonesQueue was sent to it as well, it should write every
     * "important" action to this queue.
     */
    @Override

    public void run() {
        byte[] buffer = new byte[COPY_BUFFER_SIZE]; // the copy buffer
        File fileToCopy; // the file to copy
        int length; // the length of what we will read
        FileInputStream input;
        FileOutputStream output;

        // loop read from fileToCopy and write in copiedFile
        fileToCopy = this.resultsQueue.dequeue();

        while((fileToCopy) != null) {
            try {
                input = new FileInputStream(fileToCopy);
                File copiedFile = new File(this.destDir, fileToCopy.getName());
                output = new FileOutputStream(copiedFile);
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0,length);
                }
                // put the important action in the milestonesQueue
                if (isMilestones) { //
                    synchronized (this) {
                        this.milestonesQueue.enqueue("Copier on thread id " + this.id + " : file named " + fileToCopy.getName() + " was copied\n");
                    }
                }
                input.close();
                output.close();
                fileToCopy = this.resultsQueue.dequeue();

            } catch (IOException e) {
                System.err.println("An exception has occurred while copying " +
                        "files.");
                return;
            }
        }
    }
}
