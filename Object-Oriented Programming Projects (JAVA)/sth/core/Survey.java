package sth.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sth.core.exception.NoAssociatedSurveyException;
import sth.core.exception.NonEmptyAssociatedSurveyException;
import sth.core.exception.InvalidSurveyOperationException;

/**
 * Class that represents a survey of a specific project.
 * A survey can be in diferent states, represented by a <code>SurveyState</code> type.
 * The possible states are: Created, Opened, Closed and Finalized.
 * More states and behaviors can be added/changed easily whitout altering this class.
 * A survey also stores, anonymously, the answers of the students that submitted to the associated project.
 * @see Project
 * @see SurveyState
 * @see Answer
 *
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
class Survey implements java.io.Serializable {

	/** Serial number for serialization */
    private static final long serialVersionUID = 201811152211L;

    /** Project that has this survey */
    private Project _project;

    /** State of the survey that decides certain methods actions */
    private SurveyState _state;

    /** Set of student IDs that anonymously stores the students who already filled this survey */
    private Set<Integer> _ids;

    /** List of answers made to the survey */
    private List<Answer> _answers;

	/**
	 * Creates a new survey given the project it belongs to.
	 *
	 * @param project - Project that has this survey
	 */
    Survey(Project project) {
    	_project = project;
    	_state = new Created();
    	_ids = new HashSet<>();
    	_answers = new ArrayList<>();
    }

    /**
	 * Cancels the survey.
	 * The actions of this method depend on the surveys state.
	 *
	 * @param notifier - Entity that will notify observers if the survey is opened
	 *
	 * @throws NonEmptyAssociatedSurveyException When the survey to cancel is opened and has received answers
	 * @throws InvalidSurveyOperationException When the survey is in a finished state, canceling is impossible
	 */
	void cancel(Notifier notifier) throws NonEmptyAssociatedSurveyException, InvalidSurveyOperationException {
    	_state.cancel(notifier);
    }

    /**
	 * Opens the survey.
	 * The actions of this method depend on the surveys state.
	 *
	 * @param notifier - Entity that will notify observers if the survey is opened
	 *
	 * @throws InvalidSurveyOperationException When the survey is in a finished state, opening is impossible
	 */
	void open(Notifier notifier) throws InvalidSurveyOperationException {
		_state.open(notifier);
	}

	/**
	 * Closes the survey.
	 * The actions of this method depend on the surveys state.
	 *
	 * @throws InvalidSurveyOperationException When the survey is in a created or finished state, closing is impossible
	 */
	void close() throws InvalidSurveyOperationException {
		_state.close();
	}

	/**
	 * Finalizes the survey.
	 * The actions of this method depend on the surveys state.
	 *
	 * @param notifier - Entity that will notify observers if the survey is finalized
	 * 
	 * @throws InvalidSurveyOperationException When the survey is in a created or opened state, finalizing is impossible
	 */
	void finish(Notifier notifier) throws InvalidSurveyOperationException {
		_state.finish(notifier);
	}

	/**
	 * Adds an answer to the survey.
	 * The actions of this method depend on the surveys state.
	 * 
	 * @param id 	  - ID of the student answering the survey
	 * @param time 	  - Time the student took to finish the project
	 * @param comment - Comment on the project
	 *
	 * @throws InvalidSurveyOperationException When the survey is not in an opened state, answering is impossible
	 */
	void answer(int id, int time, String comment) throws NoAssociatedSurveyException, InvalidSurveyOperationException {
		_state.answer(id, time, comment);
	}

	/**
	 * Gets the results of the answers to the survey.
	 * The actions of this method depend on the surveys state.
	 * 
	 * @param presenter - The entity that will show the survey results in a specific format
	 * @return The results of the survey
	 */
	String showResults(SurveyShowable presenter) {
		return _project.getDiscipline().getName() + " - " + _project.getName() + " " + _state.showResults(presenter);
	}

	/**
	 * Tells if the survey has been answered.
	 *
	 * @return true if the survey as at least one answer, false otherwise
	 */
	boolean hasAnswers() {
		return getNumAnswers() > 0;
	}

	/**
	 * Gets the number of answers to the survey.
	 *
	 * @return Number of answers to the survey
	 */
	int getNumAnswers() {
		return _answers.size();
	}

	/**
	 * Gets the project that has this survey associated.
	 *
	 * @return The survey project
	 */
	Project getProject() {
    	return _project;
	}

	/**
	 * Gets the minimum time the project took to be finished.
	 *
	 * @return The minimum time the project was solved in 
	 */
	int getMinTime() {
		int min = hasAnswers() ? _answers.get(0).getTime() : 0;
		
		for (Answer a : _answers)
			if (a.getTime() < min)
				min = a.getTime();

		return min;
	}

	/**
	 * Gets the average time the project took to be finished.
	 *
	 * @return The average time the project was solved in
	 */
	int getAverageTime() {
		int sum = 0;
		
		for (Answer a : _answers)
			sum += a.getTime();
		
		return hasAnswers() ? sum / getNumAnswers() : 0;
	}

	/**
	 * Gets the maximum time the project took to be finished.
	 *
	 * @return The maximum time the project was solved in
	 */
	int getMaxTime() {
		int max = 0;
		
		for (Answer a : _answers)
			if (a.getTime() > max)
				max = a.getTime();

		return max;
	}

	/**
 	 * Class that represents an answer to the outer survey.
 	 * An answer is composed of a comment to the associated project
 	 * and the time that took to finish said project.
 	 *
 	 * @author Miguel Levezinho,  No 90756
 	 * @author Rafael Figueiredo, No 90770
 	 * @version 2.0
 	 */
	private class Answer implements java.io.Serializable {

		/** Serial number for serialization */
    	private static final long serialVersionUID = 201812030257L;

		/** Time that took to finish the project */
		private int _time;

		/** Comment to the submited project */
		private String _comment;

		/**
		 * Creates a new answer to the survey, given a time a comment on the project.
		 *
		 * @param time    - Time that took to finish the project
		 * @param comment - Comment to the submited project
		 */
		Answer(int time, String comment) {
			_comment = comment;
			_time = time;
		}

		/**
		 * Gets the time that took to finish the project
		 * 
		 * @return The time that took to finish the project
		 */
		int getTime() {
			return _time;
		}

		/**
		 * Gets the comment to the submited project
		 * 
		 * @return The comment to the submited project
		 */
		String getComment() {
			return _comment;
		}
	}

	/**
	 * Interface that represents an abstract state for a <code>Survey</code> object.
	 * Lists the different actions a survey can have.
	 * The passible survey states that already implement this interface are Created, Opened, Closed and Finalized.
	 * @see Survey
	 * @see Created
	 * @see Opened
	 * @see Closed
	 * @see Finalized
	 *
	 * @version 2.0
	 */
	private interface SurveyState {

		/**
		 * Cancels the survey.
		 * The actions of this method depend on the surveys state.
		 * 
		 * @param notifier - Entity that will notify observers if the survey is opened
		 *
		 * @throws NonEmptyAssociatedSurveyException When the survey to cancel is opened and has received answers
		 * @throws InvalidSurveyOperationException When the survey is in a finished state, canceling is impossible
		 */
		public void cancel(Notifier notifier) throws NonEmptyAssociatedSurveyException, InvalidSurveyOperationException;

		/**
		 * Opens the survey.
		 * The actions of this method depend on the surveys state.
		 * 
		 * @param notifier - Entity that will notify observers if the survey is opened
		 *
		 * @throws InvalidSurveyOperationException When the survey is in a finished state, opening is impossible
		 */
		public void open(Notifier notifier) throws InvalidSurveyOperationException;

		/**
		 * Closes the survey.
		 * The actions of this method depend on the surveys state.
		 *
		 * @throws InvalidSurveyOperationException When the survey is in a created or finished state, closing is impossible
		 */
		public void close() throws InvalidSurveyOperationException;

		/**
		 * Finalizes the survey.
		 * The actions of this method depend on the surveys state.
		 * 
		 * @param notifier - Entity that will notify observers if the survey is finalized
		 *
		 * @throws InvalidSurveyOperationException When the survey is in a created or opened state, finalizing is impossible
		 */
		public void finish(Notifier notifier) throws InvalidSurveyOperationException;

		/**
		 * Adds an answer to the survey.
		 * The actions of this method depend on the surveys state.
		 * 
		 * @param id 	  - ID of the student answering the survey
		 * @param time 	  - Time the student took to finish the project
		 * @param comment - Comment on the project
		 *
		 * @throws InvalidSurveyOperationException When the survey is not in an opened state, answering is impossible
		 */
		public void answer(int id, int time, String comment) throws InvalidSurveyOperationException;

		/**
		 * Gets the results of the survey.
		 * The actions of this method depend on the surveys state.
		 * 
		 * @param presenter - The entity that will show the survey results in a specific format
		 * @return The results of the survey
		 */
		public String showResults(SurveyShowable presenter);
	}

	/**
	 *
	 * @version 2.0
	 */
	private class Created implements SurveyState, java.io.Serializable {

		/** Serial number for serialization */
	    private static final long serialVersionUID = 201812022008L;

		@Override
		public void cancel(Notifier notifier) {
			_project.removeSurvey();
		}

		@Override
		public void open(Notifier notifier) throws InvalidSurveyOperationException {
			if (_project.isOpened())
				throw new InvalidSurveyOperationException(_project.getName());
			_state = new Opened();
			notifier.notifyAll("Pode preencher inquérito do projecto " + _project.getName() + " da disciplina " + _project.getDiscipline().getName());
		}

		@Override
		public void close() throws InvalidSurveyOperationException {
			throw new InvalidSurveyOperationException(_project.getName());
		}

		@Override
		public void finish(Notifier notifier) throws InvalidSurveyOperationException {
			throw new InvalidSurveyOperationException(_project.getName());
		}

		@Override
		public void answer(int id, int time, String comment) throws InvalidSurveyOperationException {
			throw new InvalidSurveyOperationException(_project.getName());
		}

		@Override
		public String showResults(SurveyShowable presenter) {
			return "(por abrir)";
		}
	}

	/**
	 *
	 * @version 2.0
	 */
	private class Opened implements SurveyState, java.io.Serializable {

		/** Serial number for serialization */
	    private static final long serialVersionUID = 201812022006L;

		@Override
		public void cancel(Notifier notifier) throws NonEmptyAssociatedSurveyException {
			if (hasAnswers())
				throw new NonEmptyAssociatedSurveyException(_project.getName());
			_project.removeSurvey();
		}

		@Override
		public void open(Notifier notifier) {}

		@Override
		public void close() {
			_state = new Closed();
		}

		@Override
		public void finish(Notifier notifier) throws InvalidSurveyOperationException {
			throw new InvalidSurveyOperationException(_project.getName());
		}

		@Override
		public void answer(int id, int time, String comment) {
			if (_ids.add(id))
    			_answers.add(new Answer(time, comment));
		}

		@Override
		public String showResults(SurveyShowable presenter) {
			return "(aberto)";
		}
	}

	/**
	 *
	 * @version 2.0
	 */
	private class Closed implements SurveyState, java.io.Serializable {

		/** Serial number for serialization */
	    private static final long serialVersionUID = 201812022009L;

		@Override
		public void cancel(Notifier notifier) {
			open(notifier);
		}

		@Override
		public void open(Notifier notifier) {
			_state = new Opened();
			notifier.notifyAll("Pode preencher inquérito do projecto " + _project.getName() + " da disciplina " + _project.getDiscipline().getName());
		}

		@Override
		public void close() {}

		@Override
		public void finish(Notifier notifier) {
			_state = new Finalized();
			notifier.notifyAll("Resultados do inquérito do projecto " + _project.getName() + " da disciplina " + _project.getDiscipline().getName());
		}

		@Override
		public void answer(int id, int time, String comment) throws InvalidSurveyOperationException {
			throw new InvalidSurveyOperationException(_project.getName());
		}

		@Override
		public String showResults(SurveyShowable presenter) {
			return "(fechado)";
		}
	}

	/**
	 *
	 * @version 2.0
	 */
	private class Finalized implements SurveyState, java.io.Serializable {

		/** Serial number for serialization */
	    private static final long serialVersionUID = 201812022007L;

		@Override
		public void cancel(Notifier notifier) throws InvalidSurveyOperationException {
			throw new InvalidSurveyOperationException(_project.getName());
		}

		@Override
		public void open(Notifier notifier) throws InvalidSurveyOperationException {
			throw new InvalidSurveyOperationException(_project.getName());
		}

		@Override
		public void close() throws InvalidSurveyOperationException {
			throw new InvalidSurveyOperationException(_project.getName());
		}

		@Override
		public void finish(Notifier notifier) {}

		@Override
		public void answer(int id, int time, String comment) throws InvalidSurveyOperationException {
			throw new InvalidSurveyOperationException(_project.getName());
		}

		@Override
		public String showResults(SurveyShowable presenter) {
			return presenter.showAnswers(Survey.this);
		}
	}
}
