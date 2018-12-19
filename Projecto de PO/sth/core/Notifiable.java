package sth.core;

/**
 * TODO
 *
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
interface Notifiable {
	
	/**
	 * Notifies this observer of a new change in a survey state.
	 *
	 * @param message - Notification being sent to all observers
	 */
	public void notify(Notification message);
}
