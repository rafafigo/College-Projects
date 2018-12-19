package sth.core;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sth.core.exception.BadEntryException;
import sth.core.exception.NoSuchPersonIdException;

/**
 * Class representing a school that serves as the starting point to populate the rest of the core dominion.<br>
 * Its methods are used by the <code>SchoolManager</code> class to apply commands and by the <code>Parser</code> class
 * to parse information from a file.<br>
 * Objects of this class are also serialized to save the state of the core.
 * @see SchoolManager
 * @see Parser
 *
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
class School implements java.io.Serializable {
	
	/** Serial number for serialization */
	private static final long serialVersionUID = 201810051538L;

	/** Name of the school */
	private String _name;

	/** Stores a collection of courses that the school has */
	private Map<String, Course> _courses;

	/** Stores all the people the related to the school by their IDs */
	private Map<Integer, Person> _people;

	/**
	 * Constructor of <code>School</code> objects.
	 *
	 * @param name - Name of the school
	 */
	public School(String name) {
		_name = name;
		_courses = new HashMap<>();
		_people = new HashMap<>();
	}

	/**
	 * Creates a <code>Parser</code> instance to interpret the file passed to the method.
	 *
	 * @param fileName - Name of the file with information of the school dominion
	 *
	 * @throws BadEntryException When the contents of the file are not formatted correctly
	 * @throws IOException When the file with name <code>fileName</code> could not be read 
	 */
	void importFile(String fileName) throws IOException, BadEntryException {
		Parser schoolParser = new Parser(this);
		schoolParser.parseFile(fileName);
	}

	/**
	 * Adds a Course to the school in the context of the parser.
	 *
	 * @param name - Name of the course to parse
	 *
	 * @return The <code>Course</code> whose name matches the one passed to the method
	 */
	Course parseCourse(String name) {
		addCourse(name);
		return _courses.get(name);
	}

	/**
	 * Adds a <code>Course</code> to the school.
	 *
	 * @param name - Name of the course to add to the school
	 *
	 * @return true if the a course was created and added, false if the course with name <code>name</code> already existed
	 */
	boolean addCourse(String name) {
        if (_courses.containsKey(name))
        	return false;

        Course course = new Course(name);
        _courses.put(name, course);
        return true;
    }

    /**
     * Adds a <code>Person</code> to the school.
	 * 
	 * @param name - Name of the person to add to the school
	 *
	 * @return true if the person was successfuly added, false if it already exists in the school
	 */
	boolean addPerson(Person person) {
		if (_people.containsValue(person))
			return false;
		_people.put(person.getId(), person);
		return true;
	}

	/**
	 * Gets the <code>Person</code> whose id is passed to the method.
	 *
	 * @param id - Person identifier number
	 *
	 * @return The <code>Person</code> whose ID is passed to the method
	 * @throws NoSuchPersonIdException When no one with the passed id exists in the school 
	 */
	Person getPerson(int id) throws NoSuchPersonIdException {
		if (!_people.containsKey(id))
			throw new NoSuchPersonIdException(id);
		return _people.get(id);
	}

	/**
	 * Shows all the people in the school by appending a String with all
	 * the information of each person.
	 *
	 * @return Formatted information of all the people in the school
	 */
	String showAllPersons() {
		String info = "";

		List<Person> people = new ArrayList<>(_people.values());
		Collections.sort(people);

		for (Person p : people)
			info += p.toString();

		return info;
	}

	/**
	 * Seraches for people that have the string <code>name</code> in their name.
	 *
	 * @param name - Name of the person to search for
	 *
	 * @return Formatted information about the person(s) after the serach
	 */
	String searchPerson(String name) {
		String info = "";

		List<Person> people = new ArrayList<>(_people.values());
		Collections.sort(people, Person.getComparatorByName());

		for (Person p : people)
			if (p.getName().contains(name))
				info += p.toString();

		return info;
	}
}
