package sth.core;

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
interface SurveyShowable {

    /**
     * Gets a formatted <code>String</code> containing the information of a survey in a given project in a given discipline.
     *
     * @param disName  - Name ID of the discipline
     * @param projName - Name ID of the project
     * @return The info of the survey to be visualized
     *
     * @throws NoSuchDisciplineIdException When the passed discipline ID is not found
     * @throws NoSuchProjectIdException When the passed project ID is not found in the chosen disciline
     * @throws NoAssociatedSurveyException When the chosen project does not have a survey associated with it
     */
    public String showSurveyResults(String disName, String projName) throws NoSuchDisciplineIdException, NoSuchProjectIdException, NoAssociatedSurveyException;
    
    public String showAnswers(Survey survey);
}
