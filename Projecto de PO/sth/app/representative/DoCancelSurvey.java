package sth.app.representative;

import pt.tecnico.po.ui.DialogException;

import sth.app.exception.NonEmptySurveyException;
import sth.app.exception.SurveyFinishedException;

import sth.core.exception.InvalidSurveyOperationException;
import sth.core.exception.NonEmptyAssociatedSurveyException;
import sth.core.exception.NoAssociatedSurveyException;
import sth.core.exception.NoSuchDisciplineIdException;
import sth.core.exception.NoSuchProjectIdException;

import sth.core.SchoolManager;

/**
 * 4.5.2. Cancel survey.
 */
public class DoCancelSurvey extends sth.app.common.ProjectCommand {

	/**
	 * 
	 * @param receiver
	 */
	public DoCancelSurvey(SchoolManager receiver) {
		super(Label.CANCEL_SURVEY, receiver);
	}
	
	/** @see sth.app.common.ProjectCommand#myExecute() */
	@Override
	public final void myExecute() throws NoSuchProjectIdException, NoSuchDisciplineIdException, NoAssociatedSurveyException, DialogException {
		try {
			_receiver.cancelSurvey(_discipline.value(), _project.value());
		} catch (NonEmptyAssociatedSurveyException ese) {
			throw new NonEmptySurveyException(_discipline.value(), _project.value());
		} catch (InvalidSurveyOperationException ise) {
			throw new SurveyFinishedException(_discipline.value(), _project.value());
		}
	}
}
