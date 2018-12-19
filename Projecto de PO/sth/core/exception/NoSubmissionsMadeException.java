package sth.core.exception;

/** 
 * Exception thrown when a student tries to answer a survey associated with a project he did not submit to.
 * 
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
public class NoSubmissionsMadeException extends Exception {

    /** Serial number for serialization. */
    private static final long serialVersionUID = 201809021324L;

    /** Student id */
    private int _studentId;

    /** Project id. */
    private String _projectName;

    /**
    * @param id
    */
    public NoSubmissionsMadeException(int sId, String pId) {
        _studentId = sId;
        _projectName = pId;
    }

    /** @return sId */
    public int getStudentId() {
        return _studentId;
    }

    /** @return pId */
    public String getProjectId() {
        return _projectName;
    }
}
