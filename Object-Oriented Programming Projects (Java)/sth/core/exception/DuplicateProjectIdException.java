package sth.core.exception;

/**
 * Exception thrown when the project to create already exists.
 * 
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
public class DuplicateProjectIdException extends Exception {
    
    /** Serial number for serialization. */
    private static final long serialVersionUID = 201811151343L;
    
    /** Dicipline name */
    private String _discipline;
    
    /** Project name */
    private String _project;
    
    /**
     * 
     * @param discipline
     * @param project
     */
    public DuplicateProjectIdException(String discipline, String project) {
        _discipline = discipline;
        _project = project;
    }
    
    /**
     * 
     * @return Discipline name
     */
    public String getDiscipline() {
        return _discipline;
    }
    
    /**
     * 
     * @return Project name
     */
    public String getProject() {
        return _project;
    }
}
