package sth.core;

/**
 * 
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
interface Notifier {

	/**
     * Adds a new entity to the list of observers to notify when a survey state changes to opened or finished.
     *
     * @param observer - The observer entity that wants to be notified
     */
    public void addNotifiable(Notifiable observer);
    
    /**
     * Removes an entity from the list of observers to notify when a survey state changes to opened or finished.
     *
     * @param observer - The observer entity that wants to be notified
     */
    public void removeNotifiable(Notifiable observer);

    /**
     * Sends a notification to all observers.
     *
     * @param message - Notification being sent to all observers
     */
    public void notifyAll(String message);
}
