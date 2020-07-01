package sth.app.teaching;

import sth.core.exception.NoAssociatedSurveyException;
import sth.core.exception.NoSuchProjectIdException;
import sth.core.exception.NoSuchDisciplineIdException;

import sth.core.SchoolManager;

/**
 * 4.4.5. Show survey results.
 */
public class DoShowSurveyResults extends sth.app.common.ProjectCommand {

	/**
	 * 
	 * @param receiver
	 */
	public DoShowSurveyResults(SchoolManager receiver) {
		super(Label.SHOW_SURVEY_RESULTS, receiver);
	}
	
	/** @see sth.app.common.ProjectCommand#myExecute() */
	@Override
	public final void myExecute() throws NoSuchDisciplineIdException, NoSuchProjectIdException, NoAssociatedSurveyException {
		_display.addLine(_receiver.showSurveyResults(_discipline.value(), _project.value()));
		_display.display();
	}
}
