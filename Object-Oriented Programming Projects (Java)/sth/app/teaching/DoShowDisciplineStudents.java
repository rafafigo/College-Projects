package sth.app.teaching;

import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.DialogException;
import pt.tecnico.po.ui.Input;

import sth.app.exception.NoSuchDisciplineException;

import sth.core.exception.NoSuchDisciplineIdException;

import sth.core.SchoolManager;

/**
 * 4.4.4. Show course students.
 */
public class DoShowDisciplineStudents extends Command<SchoolManager> {

	private Input<String> _discipline;
	
	public DoShowDisciplineStudents(SchoolManager receiver) {
		super(Label.SHOW_COURSE_STUDENTS, receiver);
		_discipline = _form.addStringInput(Message.requestDisciplineName());
	}
	
	/** @see pt.tecnico.po.ui.Command#execute() */
	@Override
	public final void execute() throws DialogException {
		_form.parse();
	
		try {
			_display.addLine(_receiver.showStudents(_discipline.value()));
			_display.display();
		} catch (NoSuchDisciplineIdException nsd) {
			throw new NoSuchDisciplineException(_discipline.value());
		}
	}
}
