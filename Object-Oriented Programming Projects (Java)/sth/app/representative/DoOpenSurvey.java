package sth.app.representative;

import pt.tecnico.po.ui.DialogException;

import sth.app.exception.OpeningSurveyException;

import sth.core.exception.InvalidSurveyOperationException;
import sth.core.exception.NoAssociatedSurveyException;
import sth.core.exception.NoSuchDisciplineIdException;
import sth.core.exception.NoSuchProjectIdException;

import sth.core.SchoolManager;

/**
 * 4.6.3. Open survey.
 */
public class DoOpenSurvey extends sth.app.common.ProjectCommand {

	/**
	 * 
	 * @param receiver
	 */
	public DoOpenSurvey(SchoolManager receiver) {
		super(Label.OPEN_SURVEY, receiver);
	}

	/** @see sth.app.common.ProjectCommand#myExecute() */ 
	@Override
	public final void myExecute() throws NoSuchDisciplineIdException, NoSuchProjectIdException, NoAssociatedSurveyException, DialogException {
		try {
			_receiver.openSurvey(_discipline.value(), _project.value());
		} catch(InvalidSurveyOperationException ise) {
			throw new OpeningSurveyException(_discipline.value(), _project.value());
		}
	}
}
