package sth.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sth.core.exception.BadEntryException;
import sth.core.exception.DuplicateProjectIdException;
import sth.core.exception.NoAssociatedSurveyException;
import sth.core.exception.NoSuchDisciplineIdException;
import sth.core.exception.NoSuchProjectIdException;

/**
 * TODO
 *
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
class Teacher extends Person implements SurveyShowable, java.io.Serializable {

	/** Serial number for serialization */
    private static final long serialVersionUID = 201811152208L;

	/** Disciplines the teacher gives */
	private List<Discipline> _disciplines;

	/**
	 * Creates a new teacher.
	 *
	 * @param id       - Teacher ID number
     * @param phoneNum - Teacher phone number
     * @param name     - Teacher name
	 */
	Teacher(int id, int phoneNum, String name) {
		super(id, phoneNum, name);

		_disciplines = new ArrayList<>();
	}

	@Override
	void parseContext(String context, School school) throws BadEntryException {
		String components[] =  context.split("\\|");

    	if (components.length != 2)
      		throw new BadEntryException("Invalid line context " + context);

    	Course course = school.parseCourse(components[0]);
    	Discipline discipline = course.parseDiscipline(components[1]);

    	addDiscipline(discipline);
	}

	/**
	 * Adds a new discipline to the teacher, representing a new dicipline the teacher will give.
	 *
	 * @param discipline Discipline to add
	 * @return true if the discipline was added, false otherwise
	 */
	boolean addDiscipline(Discipline discipline) {
		if (_disciplines.contains(discipline))
			return false;

		_disciplines.add(discipline);
		discipline.addTeacher(this);

		return true;
	}

	/**
	 * Opens a project in a given discipline.
	 *
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines given by the teacher
	 * @throws DuplicateProjectIdException When the passed project ID is already in use in the discipline
	 */
	void createProject(String disName, String projName) throws NoSuchDisciplineIdException, DuplicateProjectIdException {
		getDiscipline(disName).createProject(projName);
	}

	/**
	 * Closes a project in a given discipline.
	 *
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines given by the teacher
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the disciline given by the teacher
	 */
	void closeProject(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException {
		Discipline discipline = getDiscipline(disName);
		discipline.getProject(projName).close(discipline);
	}

	/**
	 * Gets a formatted <code>String</code> with the information of a project submissions in a discipline of the teacher.
	 *
	 * @param disName  - Name ID of the discipline
	 * @param projName - Name ID of the project
	 * @return Project info of the given discipline.
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines given by the teacher
	 * @throws NoSuchProjectIdException When the passed project ID is not found in the disciline given by the teacher
	 */
	String showSubmissions(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException {
		return disName + " - " + projName + "\n"
			 + getDiscipline(disName).getProject(projName).showSubmissions();
	}

	/**
	 * Gets a formatted <code>String</code> containing the information of all the students in a given discipline of the teacher.
	 *
	 * @param disName - Name ID of the discipline
	 * @return Student info of the given discipline
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines given by the teacher
	 */
	String showStudents(String disName) throws NoSuchDisciplineIdException {
		return getDiscipline(disName).showStudents();
	}

	@Override
	public String showSurveyResults(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException, NoAssociatedSurveyException {
		return getDiscipline(disName).getProject(projName).getSurvey().showResults(this);
	}

	@Override
	public String showAnswers(Survey survey) {
		return "\n" + " * Número de submissões: " + survey.getProject().getNumSubmissions()
			 + "\n" + " * Número de respostas: "  + survey.getNumAnswers()
			 + "\n" + " * Tempos de resolução (horas) (mínimo, médio, máximo): " + survey.getMinTime()
		 													   	   		  + ", " + survey.getAverageTime()
		 													   	   		  + ", " + survey.getMaxTime();
	}

	/**
	 * Gets a discipline given by the teacher.
	 * 
	 * @param disName - Name ID of the discipline
	 * @return The dicipline which corresponds to the passed name
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines given by the teacher
	 */
	private Discipline getDiscipline(String disName) throws NoSuchDisciplineIdException {	
		for (Discipline d : _disciplines)
			if (d.getName().equals(disName))
				return d;

		throw new NoSuchDisciplineIdException(disName);
	}

	/**
	 * Makes a certain discipline notify the teacher every time a survey is opened or closed in a project of the discipline.
	 * 
	 * @param disName - Name ID of the discipline
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines given by the teacher
	 */
	void enableNotifications(String disName) throws NoSuchDisciplineIdException {
        getDiscipline(disName).addNotifiable(this);
    }
    
    /**
	 * Makes a certain discipline stop notifying the teacher every time a survey is opened or closed in a project of the discipline.
	 * 
	 * @param disName - Name ID of the discipline
	 *
	 * @throws NoSuchDisciplineIdException When the passed discipline ID is not found in the discilines given by the teacher
	 */
    void disableNotifications(String disName) throws NoSuchDisciplineIdException {
        getDiscipline(disName).removeNotifiable(this);
    }

	@Override
    String getType() {
        return "DOCENTE";
    }

    @Override
    String getInfo() {
        String info = "";
		
		Collections.sort(_disciplines);
		
		for (Discipline d : _disciplines)
			info += "* " + d.getCourse().getName() + " - " + d.getName() + "\n";
		
		return info;
    }
}
