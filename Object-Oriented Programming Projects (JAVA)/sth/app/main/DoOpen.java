package sth.app.main;

import java.io.IOException;

import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.DialogException;
import pt.tecnico.po.ui.Input;

import sth.app.exception.NoSuchPersonException;

import sth.core.exception.NoSuchPersonIdException;

import sth.core.SchoolManager;

/**
 * 4.1.1. Open existing document.
 */
public class DoOpen extends Command<SchoolManager> {

	private Input<String> _fileName;

	/**
	 * 
	 * @param receiver
	 */
	public DoOpen(SchoolManager receiver) {
		super(Label.OPEN, receiver);
		_fileName = _form.addStringInput(Message.openFile());
	}

	/** @see pt.tecnico.po.ui.Command#execute() */
	@Override
	public final void execute() throws DialogException {
		_form.parse();
		
		try {
			_display.add(_receiver.openState(_fileName.value()));
			_display.display();
		} catch (NoSuchPersonIdException nsp) {
			throw new NoSuchPersonException(nsp.getId());
		} catch (IOException e) {
			_display.popup(Message.fileNotFound());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}