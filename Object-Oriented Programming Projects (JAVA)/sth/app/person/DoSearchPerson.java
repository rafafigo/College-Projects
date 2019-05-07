package sth.app.person;

import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.Input;

import sth.core.SchoolManager;

/**
 * 4.2.4. Search person.
 */
public class DoSearchPerson extends Command<SchoolManager> {

	private Input<String> _input;
	  
	/**
	 * 
	 * @param receiver
	 */
	public DoSearchPerson(SchoolManager receiver) {
		super(Label.SEARCH_PERSON, receiver);
		_input = _form.addStringInput(Message.requestPersonName());
	}
	
	/** @see pt.tecnico.po.ui.Command#execute() */
	@Override
	public final void execute() {
		_form.parse();
		_display.addLine(_receiver.searchPerson(_input.value()));
		_display.display();  
	}
}