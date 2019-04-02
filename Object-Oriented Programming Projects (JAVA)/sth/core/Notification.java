package sth.core;

/**
 * 
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
class Notification implements java.io.Serializable {

	/** Message to send as a notification */
    private String _message;
	
	/**
	 * Creates a new notification.
	 */
	Notification(String message) {
		_message = message;
	}

	/**
	 * Gets the message to send as a notification.
	 *
	 * @return Message of the notification
	 */
	String getMessage() {
		return _message;
	}
}
