package sth.app.representative;

import pt.tecnico.po.ui.DialogException;

import sth.app.exception.ClosingSurveyException;

import sth.core.exception.InvalidSurveyOperationException;
import sth.core.exception.NoAssociatedSurveyException;
import sth.core.exception.NoSuchDisciplineIdException;
import sth.core.exception.NoSuchProjectIdException;

import sth.core.SchoolManager;

/**
 * 4.5.4. Close survey.
 */
public class DoCloseSurvey extends sth.app.common.ProjectCommand {

	/**
	 * 
	 * @param receiver
	 */
	public DoCloseSurvey(SchoolManager receiver) {
		super(Label.CLOSE_SURVEY, receiver);
	}
	
	/** @see sth.app.common.ProjectCommand#myExecute() */
	@Override
	public final void myExecute() throws NoSuchProjectIdException, NoSuchDisciplineIdException, NoAssociatedSurveyException, DialogException {
		try {
			_receiver.closeSurvey(_discipline.value(), _project.value());
		} catch (InvalidSurveyOperationException ise) {
			throw new ClosingSurveyException(_discipline.value(), _project.value());
		}
	}
}
