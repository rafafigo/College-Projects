package sth.core;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

import sth.core.exception.BadEntryException;

class Parser {

	private School _school;
	private Person _currPerson;

	Parser(School school) {
		_school = school;
	}

	void parseFile(String fileName) throws IOException, BadEntryException {
		String line;

		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			while ((line = reader.readLine()) != null)
				parseLine(line);
		}
	}

	private void parseLine(String line) throws BadEntryException {
		if (line.startsWith("#"))
			parseContext(line);
		else
			parseHeaderPerson(line);
	}

	private void parseHeaderPerson(String header) throws BadEntryException {
		String[] components = header.split("\\|");
		int id;
		int phoneNum;

		if (components.length != 4)
			throw new BadEntryException("Invalid line: " + header);

		id = Integer.parseInt(components[1]);
		phoneNum = Integer.parseInt(components[2]);

		switch (components[0]) {
			case "ALUNO":
				_currPerson = new Student(id, phoneNum, components[3], false);
				break;
			case "DELEGADO":
				_currPerson = new Student(id, phoneNum, components[3], true);
				break;
			case "DOCENTE":
				_currPerson = new Teacher(id, phoneNum, components[3]);
				break;
			case "FUNCIONÁRIO":
				_currPerson = new Employee(id, phoneNum, components[3]);
				break;
			default:
				throw new BadEntryException("Invalid token " + components[0] + " in line describing a person");
		}

		_school.addPerson(_currPerson);
	}

	private void parseContext(String line) throws BadEntryException {
		String lineContext = line.substring(2);

		_currPerson.parseContext(lineContext, _school);
	}
}
