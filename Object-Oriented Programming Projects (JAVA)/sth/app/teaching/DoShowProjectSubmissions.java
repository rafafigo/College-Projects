package sth.app.teaching;

import sth.core.exception.NoSuchDisciplineIdException;
import sth.core.exception.NoSuchProjectIdException;

import sth.core.SchoolManager;

/**
 * 4.4.3. Show project submissions.
 */
public class DoShowProjectSubmissions extends sth.app.common.ProjectCommand {

	/**
	 * 
	 * @param receiver
	 */
	public DoShowProjectSubmissions(SchoolManager receiver) {
		super(Label.SHOW_PROJECT_SUBMISSIONS, receiver);
	}
	
	/** @see sth.app.common.ProjectCommand#myExecute() */
	@Override
	public final void myExecute() throws NoSuchDisciplineIdException, NoSuchProjectIdException {
		_display.addLine(_receiver.showSubmissions(_discipline.value(), _project.value()));
		_display.display();
	}
}
