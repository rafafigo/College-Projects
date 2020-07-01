package sth.core.exception;

/**
 * Exception thrown when the survey to create already exists in the project.
 * 
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
public class DuplicateAssociatedSurveyException extends Exception {

    /** Serial number for serialization. */
    private static final long serialVersionUID = 201811240044L;

    /** Project id. */
    private String _projectName;
    
    /**
     * @param id
     */
    public DuplicateAssociatedSurveyException(String id) {
        _projectName = id;
    }
    
    /**
     * 
     * @return Project name
     */
    public String getId() {
        return _projectName;
    }
}