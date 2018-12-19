package sth.app.representative;

import pt.tecnico.po.ui.DialogException;

import sth.app.exception.DuplicateSurveyException;
import sth.app.exception.NoSuchProjectException;

import sth.core.exception.ClosedProjectException;
import sth.core.exception.DuplicateAssociatedSurveyException;
import sth.core.exception.NoSuchDisciplineIdException;
import sth.core.exception.NoSuchProjectIdException;

import sth.core.SchoolManager;

/**
 * 4.5.1. Create survey.
 */
public class DoCreateSurvey extends sth.app.common.ProjectCommand {

	/**
	 * 
	 * @param receiver
	 */
	public DoCreateSurvey(SchoolManager receiver) {
		super(Label.CREATE_SURVEY, receiver);
	}
	
	/** @see sth.app.common.ProjectCommand#myExecute() */ 
	@Override
	public final void myExecute() throws NoSuchDisciplineIdException, NoSuchProjectIdException, DialogException {
		try {
			_receiver.createSurvey(_discipline.value(), _project.value());
		} catch (ClosedProjectException cpe) {
			throw new NoSuchProjectException(_discipline.value(), _project.value());
		} catch (DuplicateAssociatedSurveyException dse) {
			throw new DuplicateSurveyException(_discipline.value(), _project.value());
		}
	}
}
