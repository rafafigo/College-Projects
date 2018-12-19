package sth.core;

import java.util.List;
import java.util.ArrayList;

/**
 * TODO
 *
 * @author Miguel Levezinho,  No 90756
 * @author Rafael Figueiredo, No 90770
 * @version 2.0
 */
class Employee extends Person implements java.io.Serializable {

	/** Serial number for serialization */
    private static final long serialVersionUID = 201811152209L;

    /**
	 * Creates a new employee.
	 *
	 * @param id       - Employee ID number
     * @param phoneNum - Employee phone number
     * @param name     - Employee name
	 */
	Employee(int id, int phoneNum, String name) {
		super(id, phoneNum, name);
	}

	@Override
    String getType() {
        return "FUNCIONÁRIO";
    }

    @Override
    String getInfo() {
    	return "";
    }
}
