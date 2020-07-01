package sth.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import sth.core.exception.BadEntryException;
import sth.core.exception.ClosedProjectException;
import sth.core.exception.DuplicateAssociatedSurveyException;
import sth.core.exception.DuplicateProjectIdException;
import sth.core.exception.ImportFileException;
import sth.core.exception.InvalidSurveyOperationException;
import sth.core.exception.NonEmptyAssociatedSurveyException;
import sth.core.exception.NoAssociatedSurveyException;
import sth.core.exception.NoSubmissionsMadeException;
import sth.core.exception.NoSuchDisciplineIdException;
import sth.core.exception.NoSuchPersonIdException;
import sth.core.exception.NoSuchProjectIdException;

/**
 * TODO
 *
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
public class SchoolManager {

	/** The school whose data will be accessed through this class */
	private School _school;
	
	/** Name of the file that stores the data to populate the core dominion */
	private String _fileName;
	
	/** Current person logged in on the system */
	private Person _user;

	/**
	 * Creates a new manager for a school.
	 * A default name for the school ("School") is given.
	 */
	public SchoolManager() {
		_school = new School("School");
	}

	/**
	 * Creates a new manager for a school.
	 * 
	 * @param schoolName - Name of the school
	 */
	public SchoolManager(String schoolName) {
		_school = new School(schoolName);
	}

	/**
	 * Imports data from a file, following a certain format, that will be read and parsed by the program.
	 * 
	 * @param dataFile - Name of the file to parse
	 * @throws ImportFileException When an error related to reading/parsing the file occurs
	 */
	public void importFile(String dataFile) throws ImportFileException {
		try {
			_school.importFile(dataFile);
		} catch (IOException | BadEntryException e) {
			throw new ImportFileException(e);
		}
	}

	/**
	 * Sets the current logged in user.
	 * 
	 * @param id - ID number of the person to be logged in
	 * @throws NoSuchPersonIdException When the core does not find the passed id in its system
	 */
	public void login(int id) throws NoSuchPersonIdException {
		_user = _school.getPerson(id);
	}

	/**
	 * Tells if the logged in user is an <code>Employee</code>.
	 * 
	 * @return true if the user is an <code>Employee</code>, false otherwise
	 */
	public boolean isLoggedUserAdministrative() {
		return _user instanceof Employee;
	}

	/**
	 * Tells if the logged in user is a <code>Teacher</code>.
	 * 
	 * @return true if the user is a <code>Teacher</code>, false otherwise
	 */
	public boolean isLoggedUserProfessor() {
		return _user instanceof Teacher;
	}

	/**
	 * Tells if the logged in user is a <code>Student</code>.
	 * 
	 * @return true if the user is a <code>Student</code>, false otherwise
	 */
	public boolean isLoggedUserStudent() {
		return _user instanceof Student;
	}

	/**
	 * Tells if the logged in user is a <code>Student</code> representative.
	 * 
	 * @return true if the user is a <code>Student</code> representative, false otherwise
	 */
	public boolean isLoggedUserRepresentative() {
		return _user instanceof Student && ((Student) _user).isRepresentative();
	}

	/**
	 * Gets the name of the file with dominion data.
	 * 
	 * @return The name of the file
	 */
	public String getFileName() {
		return _fileName;
	}

	/**
	 * Sets the name of the file dominion data
	 * 
	 * @param fileName - The name of the file
	 */
	public void setFileName(String fileName) {
		_fileName = fileName;
	}

	/**
	 * TODO
	 *
	 * @param fileName - Name of the file where the state of the core is stored
	 * @return Pending notifications of opened and closed surveys
	 * 
	 * @throws NoSuchPersonIdException When the current user is not in the stored core state
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public String openState(String fileName) throws NoSuchPersonIdException, IOException, ClassNotFoundException {
		try (ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(fileName))) {

			School newSchool = (School) objIn.readObject();
			_user = newSchool.getPerson(_user.getId());
			_school = newSchool;
			setFileName(fileName);
			return _user.showNotifications();
		}
	}

	/**
	 * TODO
	 *
	 * @param fileName - Name of the file where the state of the core will be stored
	 *
	 * @throws IOException
	 */
	public void saveState(String fileName) throws IOException {
		try (ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(fileName))) {
			objOut.writeObject(_school);
		}
		setFileName(fileName);
	}

	/**
	 * Command that is aplicable for every type of person in the system.
	 * Gets a formatted <code>String</code> containing the logged person information.
	 * 
	 * @return The info of the person to be visualized
	 */
	public String showPerson() {
		return _user.toString();
	}

	/**
	 * Command that is aplicable for every type of person in the system.
	 * Gets a formatted <code>String</code> containing the information of all the people in the system.
	 * 
	 * @return The info of all the people to be visualized
	 */
	public String showAllPersons() {
		return _school.showAllPersons();
	}

	/**
	 * Command that is aplicable for every type of person in the system.
	 * Searches for a person in the system.
	 * 
	 * @param name - <code>String</code> to be searched for in the persons name
	 * @return The info of all the people that have passed <code>String</code> in their name
	 */
	public String searchPerson(String name) {
		return _school.searchPerson(name);
	}

	/**
	 * Command that is aplicable for every type of person in the system.
	 * Sets a new phone number for the logged in person.
	 * 
	 * @param phoneNum - Person new phone number
	 */
	public void changePhoneNumber(int phoneNum) {
		_user.setPhoneNumber(phoneNum);
	}

	/**
	 * Command that is only aplicable for teachers in the system.
	 * Creates a new project in a discipline given by the teacher.
	 * 
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines given by the teacher
	 * @throws DuplicateProjectIdException When the passed project ID is already in use in the discipline
	 */
	public void createProject(String disName, String projName) throws NoSuchDisciplineIdException, DuplicateProjectIdException {
		((Teacher) _user).createProject(disName, projName);
	}

	/**
	 * Command that is only aplicable for teachers in the system.
	 * Closes a project in a discipline given by the teacher.
	 * 
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines given by the teacher
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the disciline given by the teacher
	 */
	public void closeProject(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException {
		((Teacher) _user).closeProject(disName, projName);
	}

	/**
	 * Command that is only aplicable for teachers in the system.
	 * Gets a formatted <code>String</code> with the information of a project submissions in a given discipline.
	 * 
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 * @return The information on the submissions to be visualized
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines given by the teacher
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the disciline given by the teacher
	 */
	public String showSubmissions(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException {
		return ((Teacher) _user).showSubmissions(disName, projName);
	}

	/**
	 * Command that is only aplicable for teachers in the system.
	 * Gets a formatted <code>String</code> containing the information of all the students in a given discipline.
	 * 
	 * @param disName - Name ID of the discipline
	 * @return The info of all the students to be visualized
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines given by the teacher
	 */
	public String showStudents(String disName) throws NoSuchDisciplineIdException {
		return ((Teacher) _user).showStudents(disName);
	}

	/**
	 * Command that is only aplicable for students in the system.
	 * Submits a project to a enrolled discipline.
	 * 
	 * @param disName    - Name ID of the discipline
	 * @param projName   - Name ID of the project
	 * @param submission - Concrete submission to the project, represented by a <code>String</code>
	 * 
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines the student is enrolled
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the chosen disciline
	 * @throws ClosedProjectException When the project to submit to has already closed
	 */
	public void submitProject(String disName, String projName, String submission) throws NoSuchDisciplineIdException, NoSuchProjectIdException, ClosedProjectException {
		((Student) _user).submitProject(disName, projName, submission);
	}

	/**
	 * Command that is only aplicable for students in the system.
	 * Fills the survey of the given project from the given discipline.
	 * 
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 * @param time     - Time that took to fill the survey
	 * @param comment  - Comment on the project
	 * 
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines the student is enrolled
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the chosen disciline
	 * @throws NoAssociatedSurveyException When the chosen project does not have a survey associated with it
	 * @throws NoSubmissionsMadeException When the current user did not submit to the project
	 * @throws InvalidSurveyOperationException When the survey is not opened
	 */
	public void answerSurvey(String disName, String projName, int time, String comment) throws NoSuchDisciplineIdException, NoSuchProjectIdException, NoAssociatedSurveyException, NoSubmissionsMadeException, InvalidSurveyOperationException {
		((Student) _user).answerSurvey(disName, projName, time, comment);
	}

	/**
	 * Command that is aplicable for students and teachers in the system.
	 * Gets a formatted <code>String</code> containing the information of a survey in a given project in a given discipline.
	 *
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 * @return The info of the survey to be visualized
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines the student is enrolled/given by the teacher
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the chosen disciline
	 * @throws NoAssociatedSurveyException When the chosen project does not have a survey associated with it
	 */
	public String showSurveyResults(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException, NoAssociatedSurveyException {
		return ((SurveyShowable) _user).showSurveyResults(disName, projName);
	}

	/**
	 * Command that is only aplicable for representative students in the system.
	 * Creates a new survey for the given project of a given discipline.
	 * 
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the course the student is part of
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the chosen disciline
	 * @throws ClosedProjectException When the chosen project to add a survey to has already closed
	 * @throws DuplicateAssociatedSurveyException When the chosen project already has a survey associated with it
	 */
	public void createSurvey(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException, ClosedProjectException, DuplicateAssociatedSurveyException {
		((Student) _user).asRepresentative().createSurvey(disName, projName);
	}

	/**
	 * Command that is only aplicable for representative students in the system.
	 * Cancels an existing survey in the given project of a given discipline.
	 * 
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the course the student is part of
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the chosen disciline
	 * @throws NoAssociatedSurveyException When the chosen project does not have a survey associated with it
	 * @throws NonEmptyAssociatedSurveyException When the survey being canceled has already received answers
	 * @throws InvalidSurveyOperationException When the survey being canceled has already been finalized
	 */
	public void cancelSurvey(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException, NoAssociatedSurveyException, NonEmptyAssociatedSurveyException, InvalidSurveyOperationException {
		((Student) _user).asRepresentative().cancelSurvey(disName, projName);
	}

	/**
	 * Command that is only aplicable for representative students in the system.
	 * Opens an existing survey in the given project of a given discipline.
	 * 
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the course the student is part of
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the chosen disciline
	 * @throws NoAssociatedSurveyException When the chosen project does not have a survey associated with it
	 * @throws InvalidSurveyOperationException When the survey being opened is already finalized or opened
	 */
	public void openSurvey(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException, NoAssociatedSurveyException, InvalidSurveyOperationException {
		((Student) _user).asRepresentative().openSurvey(disName, projName);
	}

	/**
	 * Command that is only aplicable for representative students in the system.
	 * Closes an existing survey in the given porject of a given discipline.
	 * 
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the course the student is part of
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the chosen disciline
	 * @throws NoAssociatedSurveyException When the chosen project does not have a survey associated with it
	 * @throws InvalidSurveyOperationException When the survey being closed is already finalized or not yet opened
	 */
	public void closeSurvey(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException, NoAssociatedSurveyException, InvalidSurveyOperationException {
		((Student) _user).asRepresentative().closeSurvey(disName, projName);
	}

	/**
	 * Command that is only aplicable for representative students in the system.
	 * Finalizes an existing survey in the given porject of a given discipline.
	 * 
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the course the student is part of
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the chosen disciline
	 * @throws NoAssociatedSurveyException When the chosen project does not have a survey associated with it
	 * @throws InvalidSurveyOperationException When the survey being finalized is not closed
	 */
	public void finalizeSurvey(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException, NoAssociatedSurveyException, InvalidSurveyOperationException {
		((Student) _user).asRepresentative().finalizeSurvey(disName, projName);
	}

	/**
	 * Command that is aplicable for representative students in the system.
	 * Gets a formatted <code>String</code> containing the information of all surveys in a given discipline.
	 *
	 * @param disName - Name ID of the discipline
	 * @return The info of the surveys to be visualized
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the course the student is part of
	 */
	public String showSurveyResults(String disName) throws NoSuchDisciplineIdException {
		return ((Student) _user).asRepresentative().showSurveyResults(disName, "");
	}
}