package sth.core.exception;

/** 
 * Exception thrown when a survey with answers is canceled.
 *
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
public class NonEmptyAssociatedSurveyException extends Exception {

    /** Serial number for serialization. */
    private static final long serialVersionUID = 201809021324L;

    /** Project id. */
    private String _projectName;

    /**
     * @param id
     */
    public NonEmptyAssociatedSurveyException(String id) {
        _projectName = id;
    }

    /** @return id */
    public String getId() {
        return _projectName;
    }
}