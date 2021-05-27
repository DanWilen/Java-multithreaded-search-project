/*
 * Name - Dan Wilensky
 */

/**
 * A synchronized bounded-size queue for multithreaded producer-consumer applications.
 *
 * @param <T> Type of data items
 */
public class SynchronizedQueue<T> {

    private T[] buffer;
    private int queueCapacity;
    private int numOfElements;
    private int producers;
    private int front;
    private int rear;

    /**
     * Constructor.
     * Allocates a buffer (an array) with the given capacity and resets pointers and counters.
     * @param capacity - Buffer capacity
     */
    @SuppressWarnings("unchecked")
    public SynchronizedQueue(int capacity) {
        this.buffer = (T[])(new Object[capacity]);
        this.queueCapacity = capacity;
        this.numOfElements = 0;
        this.producers = 0;
        this.front = this.numOfElements;
        this.rear = this.queueCapacity - 1;
    }

    /**
     * Dequeues the first item from the queue and returns it. If the queue is empty
     * but producers are still registered to this queue, this method blocks until
     * some item is available. If the queue is empty and no more items are planned
     * to be added to this queue (because no producers are registered), this method returns null.
     *
     * @return The first item, or null if there are no more items
     */
    public T dequeue() {
        synchronized (this) {
            while (numOfElements == 0 && this.producers > 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    System.err.println("An exception has occurred while " +
                            "trying to wait for threads.");
                    System.exit(1);
                }
            }
            if (numOfElements == 0 && this.producers == 0) {
                return null;
            }
            T itemToReturn = this.buffer[this.front];
            this.buffer[this.front] = null;
            this.front = (this.front + 1) % this.queueCapacity;
            this.numOfElements--;
            this.notifyAll();
            return itemToReturn;
        }
    }

    /**
     * Enqueues an item to the end of this queue. If the queue is full,
     * this method blocks until some space becomes available.
     * @param item Item to enqueue
     */
    public void enqueue(T item) {
        synchronized (this) {
            while (numOfElements == getCapacity()) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    System.err.println("An exception has occurred while " +
                            "trying to wait for threads.");
                    System.exit(1);
                }
            }
            this.rear = (this.rear + 1) % this.queueCapacity;
            this.buffer[this.rear] = item;
            this.numOfElements++;
            this.notifyAll();
        }
    }

    /**
     * Returns the capacity of this queue
     * @return queue capacity
     */
    public int getCapacity() {
        return this.queueCapacity;

    }

    /**
     * Returns the current size of the queue (number of elements in it)
     * @return queue size
     */
    public int getSize() {
        synchronized (this) {
            return this.numOfElements;
        }
    }

    /**
     *  Registers a producer to this queue. This method actually increases the internal producers
     * counter of this queue by 1. This counter is used to determine whether the queue is still
     * active and to avoid blocking of consumer threads that try to dequeue elements from an empty
     *queue, when no producer is expected to add any more items. Every producer of this queue
     * must call this method before starting to enqueue items, and must also call unregisterProducer()
     * when finishes to enqueue all items.
     *
     */
    public void registerProducer() {
        synchronized (this) {
            this.producers++;
        }
    }

    /**
     * Unregisters a producer from this queue. See registerProducer().
     */
    public void unregisterProducer() {
        synchronized (this) {
            this.producers--;
        }
    }

}
