package sth.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sth.core.exception.ClosedProjectException;
import sth.core.exception.DuplicateAssociatedSurveyException;
import sth.core.exception.InvalidSurveyOperationException;
import sth.core.exception.NoAssociatedSurveyException;
import sth.core.exception.NoSubmissionsMadeException;

/**
 * 
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
class Project implements Comparable<Project>, java.io.Serializable {

	/** Serial number for serialization */
    private static final long serialVersionUID = 201811152210L;

	/** Project name */
	private String _name;
	
	/** Project description */
	private String _description;

	/** Project discipline */
	private Discipline _discipline;
	
	/** true if the project is opened, false otherwise */
	private boolean _opened;
	
	/** Collection of Student submissions */
	private Map<Integer, Submission> _submissions;
	
	/** Survey associated with the project */
	private Survey _survey;

	/**
	 * Creates a new project.
	 * 
	 * @param name - Project name
	 */
	Project(String name, Discipline discipline) {
		this(name, "Descrição", discipline);
	}

	/**
	 * Creates a new project with a specified description.
	 * 
	 * @param name		  - Project name
	 * @param description - Project description
	 */
	Project(String name, String description, Discipline discipline) {
		_name = name;
		_description = description;
		_discipline = discipline;
		_opened = true;
		_submissions = new HashMap<>();
	}

	/**
	 * Adds a new submission to the project.
	 *
	 * @param student	 - Student submiting the project
	 * @param submission - Project submission
	 *
	 * @throws ClosedProjectException When a submission was made to a closed project
	 */
	void submit(Student student, String submission) throws ClosedProjectException {
		if (!isOpened())
			throw new ClosedProjectException(_name);
		_submissions.put(student.getId(), new Submission(submission));
	}

	/**
	 * Closes the project, disabling submissions.
	 *
	 * @param notifier - Entity that will notify observers if a associated survey is opened
	 */
	void close(Notifier notifier) {
		_opened = false;

		if (hasSurvey()) {
			try {
				_survey.open(notifier);
			} catch (InvalidSurveyOperationException e) {
				System.out.println("Unexpected core error: Survey state leak");
			}
		}
	}

	/**
	 * Shows all the submissions made to the project by appending a String with all
	 * the information of each submission.
	 * 
	 * @return Submissions information
	 */
	String showSubmissions() {
		String submissions = "";
		
		List<Integer> ids = new ArrayList<>(_submissions.keySet());
		Collections.sort(ids);
		
		for (Integer id : ids)
			submissions += "* " + id + " - " + _submissions.get(id) + "\n";
			
		return submissions;
	}

	/**
	 * Adds a survey to this project.
	 * 
	 * @throws ClosedProjectException When the chosen project to add a survey to has already closed
	 * @throws DuplicateAssociatedSurveyException When a survey already exists
	 */
	void addSurvey() throws ClosedProjectException, DuplicateAssociatedSurveyException {
		if (!isOpened())
			throw new ClosedProjectException(_name);
		if (hasSurvey())
			throw new DuplicateAssociatedSurveyException(_name);
		_survey = new Survey(this);
	}

	/**
	 * Removes the associated survey of this project.
	 */
	void removeSurvey() {
		_survey = null;
	}

	/**
	 * Submites an answer to the survey of this project.
	 * 
	 * @param id	  - ID of the student answering the survey
	 * @param time    - Time that took to answer the survey
	 * @param comment - Comment of the survey made to the project
	 *
	 * @throws NoAssociatedSurveyException When this project has no survey associated with it
	 * @throws NoSubmissionsMadeException When the student trying to answer the survey did not submit this project
	 * @throws InvalidSurveyOperationException When the survey of this project is not opened for answers or the student has already answered
	 */
	void answerSurvey(int id, int time, String comment) throws NoAssociatedSurveyException, NoSubmissionsMadeException, InvalidSurveyOperationException {
		if (!hasSurvey())
			throw new NoAssociatedSurveyException(_name);
		if (!hasSubmited(id))
			throw new NoSubmissionsMadeException(id, _name);
		_survey.answer(id, time, comment);
	}

	/**
	 * Gets the survey associated with the project.
	 * 
	 * @return The associated survey
	 *
	 * @throws NoAssociatedSurveyException When the project does not have a survey associated with it
	 */
	Survey getSurvey() throws NoAssociatedSurveyException {
		if (!hasSurvey())
			throw new NoAssociatedSurveyException(_name);
		return _survey;
	}

	/**
	 * Gets the name of the project.
	 * 
	 * @return Project name
	 */
	String getName() {
		return _name;
	}

	/**
	 * Gets the description of the project.
	 * 
	 * @return Project description
	 */
	String getDescription() {
		return _description;
	}

	/**
	 * Gets the discipline of the project.
	 * 
	 * @return Project description
	 */
	Discipline getDiscipline() {
		return _discipline;
	}

	/**
	 * Gets the number of submissions made to the project.
	 * 
	 * @return Number of submissions
	 */
	int getNumSubmissions() {
		return _submissions.size();
	}

	/**
	 * Tells if a given student has submited a project.
	 * 
	 * @param id - ID of a student 
	 * @return true if the student whose id is passed has submitted a project, false otherwise
	 */
	boolean hasSubmited(int id) {
		return _submissions.containsKey(id);
	}

	/**
	 * Tells if a given student has submited a project.
	 * 
	 * @param id - ID of a student 
	 * @return true if the student whose id is passed has submitted a project, false otherwise
	 */
	boolean hasSurvey() {
		return _survey != null;
	}

	/**
	 * Tells if the project is opened for submissions.
	 * 
	 * @return true if the project is opened, false otherwise
	 */
	boolean isOpened() {
		return _opened;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && 
			   obj instanceof Project &&
			   ((Project) obj)._name.equals(_name);
	}

	@Override
	public int compareTo(Project p) {
		return _name.compareTo(p._name);
	}

	/**
	 * Represents a submission to the project.
	 * A submission is represented by a <code>String</code>.
	 * 
	 * @author Miguel Levezinho,  No 90756
	 * @author Rafael Figueiredo, No 90770
	 * @version 2.0
 	 */
	private class Submission implements java.io.Serializable {

		/** Serial number for serialization */
    	private static final long serialVersionUID = 201812030258L;

		/** Submission content */
		private String _submission;

		/**
		 * Creates a project submission.
		 * 
		 * @param submission - Submission content
		 */
		Submission(String submission) {
			_submission = submission;
		}

		@Override
		public String toString() {
			return _submission;
		}
	}
}
