package sth.app;

import static pt.tecnico.po.ui.Dialog.IO;

import pt.tecnico.po.ui.DialogException;
import pt.tecnico.po.ui.Menu;

import sth.app.main.MainMenu;
import sth.app.person.DoLogin;

import sth.core.exception.ImportFileException;

import sth.core.SchoolManager;

/**
 * Main driver for the travel management application.
 */
public class App {
	
	public static void main(String[] args) {
		SchoolManager school = new SchoolManager();
		
		String datafile = System.getProperty("import");
		if (datafile != null) {
			try {
				school.importFile(datafile);
			} catch (ImportFileException bde) {
				System.err.println("Error in parsing: " + bde.getMessage());
				bde.printStackTrace();
			}
		}
		
		try {
			DoLogin loginCmd = new DoLogin(school);
			loginCmd.execute();
			Menu menu = new MainMenu(school);
			menu.open();
		} catch (DialogException de) {
			de.printStackTrace();
		} finally {
			IO.close();
		}
	}
}
