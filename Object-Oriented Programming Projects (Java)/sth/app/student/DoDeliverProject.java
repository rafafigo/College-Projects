package sth.app.student;

import pt.tecnico.po.ui.DialogException;
import pt.tecnico.po.ui.Input;

import sth.app.exception.NoSuchProjectException;

import sth.core.exception.ClosedProjectException;
import sth.core.exception.NoSuchDisciplineIdException;
import sth.core.exception.NoSuchProjectIdException;

import sth.core.SchoolManager;

/**
 * 4.5.1. Deliver project.
 */
public class DoDeliverProject extends sth.app.common.ProjectCommand {

	private Input<String> _submission;
	
	/**
	 * 
	 * @param receiver
	 */
	public DoDeliverProject(SchoolManager receiver) {
		super(Label.DELIVER_PROJECT, receiver);
		_submission = _form.addStringInput(Message.requestDeliveryMessage());
	}
	
	/** @see pt.tecnico.po.ui.Command#execute() */
	@Override
	public final void myExecute() throws NoSuchProjectIdException, NoSuchDisciplineIdException, DialogException {
		try {
			_receiver.submitProject(_discipline.value(), _project.value(), _submission.value());
		} catch (ClosedProjectException cpe) {
			throw new NoSuchProjectException(_discipline.value(), _project.value());
		}
	}
}
