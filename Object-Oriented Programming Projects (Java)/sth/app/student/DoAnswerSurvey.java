package sth.app.student;

import pt.tecnico.po.ui.DialogException;
import pt.tecnico.po.ui.Input;

import sth.app.exception.NoSuchProjectException;
import sth.app.exception.NoSurveyException;

import sth.core.exception.NoSuchProjectIdException;
import sth.core.exception.NoSuchDisciplineIdException;
import sth.core.exception.NoAssociatedSurveyException;
import sth.core.exception.NoSubmissionsMadeException;
import sth.core.exception.InvalidSurveyOperationException;

import sth.core.SchoolManager;

/**
 * 4.5.2. Answer survey.
 */
public class DoAnswerSurvey extends sth.app.common.ProjectCommand {

	private Input<Integer> _time;
    private Input<String> _comment;
	
	public DoAnswerSurvey(SchoolManager receiver) {
		super(Label.ANSWER_SURVEY, receiver);
		_time = _form.addIntegerInput(Message.requestProjectHours());
		_comment = _form.addStringInput(Message.requestComment());
	}
	
	/** @see sth.app.common.ProjectCommand#myExecute() */
	@Override
	public final void myExecute() throws NoSuchProjectIdException, NoSuchDisciplineIdException, DialogException {
		try {
			_receiver.answerSurvey(_discipline.value(), _project.value(), _time.value(), _comment.value());
		} catch (NoSubmissionsMadeException nsme) {
			throw new NoSuchProjectException(_discipline.value(), _project.value());
		} catch (NoAssociatedSurveyException | InvalidSurveyOperationException nase) {
			throw new NoSurveyException(_discipline.value(), _project.value());
		}
	}
}
