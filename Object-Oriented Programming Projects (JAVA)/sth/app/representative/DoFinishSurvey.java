package sth.app.representative;

import pt.tecnico.po.ui.DialogException;

import sth.app.exception.FinishingSurveyException;

import sth.core.exception.InvalidSurveyOperationException;
import sth.core.exception.NoAssociatedSurveyException;
import sth.core.exception.NoSuchDisciplineIdException;
import sth.core.exception.NoSuchProjectIdException;

import sth.core.SchoolManager;

/**
 * 4.6.5. Finish survey.
 */
public class DoFinishSurvey extends sth.app.common.ProjectCommand {

	/**
	 * 
	 * @param receiver
	 */
	public DoFinishSurvey(SchoolManager receiver) {
		super(Label.FINISH_SURVEY, receiver);
	}
	
	/** @see sth.app.common.ProjectCommand#myExecute() */ 
	@Override
	public final void myExecute() throws NoSuchDisciplineIdException, NoSuchProjectIdException, NoAssociatedSurveyException, DialogException {
		try {
			_receiver.finalizeSurvey(_discipline.value(), _project.value());
		} catch (InvalidSurveyOperationException ise) {
			throw new FinishingSurveyException(_discipline.value(), _project.value());
		}
	}
}
