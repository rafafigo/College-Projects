package sth.core.exception;

/**
 * Exception thrown when an ivalid operation is made on a survey.
 *
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
public class InvalidSurveyOperationException extends Exception {

    /** Serial number for serialization. */
    private static final long serialVersionUID = 201809021324L;

    /** Project id. */
    private String _projectName;

    /**
     * @param id
     */
    public InvalidSurveyOperationException(String id) {
        _projectName = id;
    }

    /** @return id */
    public String getId() {
        return _projectName;
    }
}