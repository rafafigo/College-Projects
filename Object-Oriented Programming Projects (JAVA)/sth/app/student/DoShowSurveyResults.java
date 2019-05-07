package sth.app.student;

import sth.core.exception.NoAssociatedSurveyException;
import sth.core.exception.NoSuchDisciplineIdException;
import sth.core.exception.NoSuchProjectIdException;

import sth.core.SchoolManager;

/**
 * 4.5.3. Show survey results.
 */
public class DoShowSurveyResults extends sth.app.common.ProjectCommand {

	/**
	 * 
	 * @param receiver
	 */
	public DoShowSurveyResults(SchoolManager receiver) {
		super(Label.SHOW_SURVEY_RESULTS, receiver);
	}
	
	/** @see pt.tecnico.po.ui.Command#execute() */
	@Override
	public final void myExecute() throws NoSuchProjectIdException, NoSuchDisciplineIdException, NoAssociatedSurveyException {
		_display.addLine(_receiver.showSurveyResults(_discipline.value(), _project.value()));
		_display.display();
	}
}
