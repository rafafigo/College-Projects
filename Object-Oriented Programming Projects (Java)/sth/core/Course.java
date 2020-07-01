package sth.core;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import sth.core.exception.NoSuchDisciplineIdException;

/**
 * TODO
 *
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
class Course implements Comparable<Course>, java.io.Serializable {

    /** Serial number for serialization */
    private static final long serialVersionUID = 201811152204L;

    /** Maximum number of representatives in a course */
    private static final int MAX_REPRESENTATIVES = 7;
    
    /** Course name */
    private String _name;
    
    /** Disciplines that are part of the course */
    private Map<String, Discipline> _disciplines;
    
    /** Students in the course (include representatives) */
    private Set<Student> _students;
    
    /** Representatives in the course */
    private Set<Student> _representatives;

    /**
     * Creates a new course.
     *
     * @param name - Name of the course
     */
    Course(String name) {
        _disciplines = new HashMap<>();
        _students = new HashSet<>();
        _representatives = new HashSet<>();
        _name = name;
    }

    /**
     * Adds a new dicipline to the course.
     * 
     * @param name - Discipline name
     * @return The added <code>Discipline</code>
     */
    Discipline parseDiscipline(String name) {
        addDiscipline(name);
        return _disciplines.get(name);
    }

    /**
     * Creates and adds a new discipline to the course.
     * If the discipline already exists, does not add it.
     * 
     * @param name - Discipline name
     * @return true if the discipline was successfully added, false otherwise
     */
    boolean addDiscipline(String name) {
        if (_disciplines.containsKey(name))
            return false;

        Discipline discipline = new Discipline(name, this);
        _disciplines.put(name, discipline);

        for (Student r : _representatives)
            discipline.addNotifiable(r);

        return true;
    }

    /**
     * Adds a student to the course.
     * If the student is already in the course, it is not added again.
     * 
     * @param student - <code>Student</code> to add
     * @return true if the Student was successfully added, false otherwise
     */ 
    boolean addStudent(Student student) {
        return _students.add(student) && (student.isRepresentative() ? addRepresentative(student) : true);
    }

    /**
     * Adds a representative to the course.
     * If the course cant have more representatives, then the student is not added.
     * @see #addStudent(Student)
     * 
     * @param student - <code>Student</code> to add as representative
     * @return true if the student was successfully added, false otherwise
     */
    boolean addRepresentative(Student student) {
        if (maxNumRepresentatives() || !_representatives.add(student))
            return false;

        for (Discipline d : _disciplines.values())
            d.addNotifiable(student);

        return true;
    }

    /**
     * Removes a representative from the course.
     * If the passed student is not a representative, it is not removed.
     * 
     * @param student - Representative to remove
     * @return true if the student was a representative in this course, false otherwise
     */
    boolean removeRepresentative(Student student) {
        return _representatives.remove(student);
    }
    
    /**
     * Tells if the course has reached the max number of representatives.
     * 
     * @return true if the limit of representatives has been reached, false otherwise
     */
    boolean maxNumRepresentatives() {
        return getNumRepresentatives() == MAX_REPRESENTATIVES;
    }

    /**
     * Gets the number of representatives in the course.
     * 
     * @return Number of representatives in the course
     */
    int getNumRepresentatives() {
        return _representatives.size();
    }

    /**
     * Gets a specific discipline from this course, given its name.
     * 
     * @param disName - Name ID of the discipline
     *
     * @return The discipline whose name was passed in
     * @throws NoSuchDisciplineIdException When the passed discipline ID is not part of the course
     */
    Discipline getDiscipline(String disName) throws NoSuchDisciplineIdException {
        if (!_disciplines.containsKey(disName))
            throw new NoSuchDisciplineIdException(disName);
        return _disciplines.get(disName);
    }

    /**
     * Gets the name of the course.
     * 
     * @return Course name
     */
    String getName() {
        return _name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null &&
               obj instanceof Course &&
               ((Course) obj)._name.equals(_name);
    }

    @Override
    public int compareTo(Course c) {
        return _name.compareTo(c._name);
    }
}
