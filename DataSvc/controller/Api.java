/**
 * The AnswerSvc project generates an endpoint for access to the ANSWER table of the database.
 * 
 * By passing in a value of true for the parameter "recent", requests to this endpoint will include, for
 * each user/question combination, only the one most recent answer. For example, if a user answered Question 1 
 * four times, and Question 2 three times, only one recent response for each question will be retrieved if 
 * "?recent=true" is in the GET request url.
 * 
 * NOTE: As of 7/26/19 the pom.xml file is set only for local database access.
 * 
 * @param /answers 											returns all rows of ANSWER table
 * @param /answers/users/{userId} 							returns all rows corresponding to userId
 * @param /answers/questions/{qId}?recent={true/false}		returns all rows for questionId except those for obsoleted answers
 * 
 * @author David Fu
 */

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class Api {

    @Autowired
    private AnswerService answerSvc;
    
	/**
	 * Returns a list of all answers of all users for the ANSWER table.
	 * 
	 * The returned list is comprised of Answer objects. Each Answer represents one row entry
	 * in the ANSWER table - in other words, one user's answer to one question.
	 * 
	 * @param recent - if true, for each question:user combination, only one most recent answer is returned
	 * @return JSON List<Answer> of all users and all questions
	 */
    @GetMapping(path = "/answers", produces = "application/json")
    public List<Answer> getAll(@RequestParam(value = "recent", defaultValue = "false") boolean recent) {
    	if (recent) {
    		return answerSvc.getAllMostRecent();
    	}
    	return answerSvc.getAll();
    }

	/**
	 * Returns a list of all answers for a given user for the ANSWER table.
	 * 
	 * The returned list is comprised of Answer objects. Each Answer represents one row entry
	 * in the ANSWER table - in other words, one user's answer to one question.
	 * 
	 * @param recent - if true, for each question:user combination, only one most recent answer is returned
	 * @param userId - the userId field from the ANSWER table
	 * @return JSON List<Answer> of all answers of user with userId
	 */
    @GetMapping(path = "/answers/users/{userId}", produces = "application/json")
    public List<Answer> getRecentAnswerByUserId(@PathVariable("userId") Long userId, 
    													@RequestParam(value = "recent", defaultValue = "false") boolean recent) {
    	if (recent) {
    		return answerSvc.getRecentAnswerByUserId(userId);
    	}
    	return answerSvc.getAnswerByUserId(userId);
    }

	/**
	 * Returns a list of all answers for a given question for the ANSWER table.
	 * 
	 * The returned list is comprised of Answer objects. Each Answer represents one row entry
	 * in the ANSWER table - in other words, one user's answer to one question.
	 * 
	 * @param recent - if true, for each question:user combination, only one most recent answer is returned
	 * @param qId    - an entry from the questionId field from the ANSWER table
	 * @return JSON List<Answer> of all answers for question qId
	 */
    @GetMapping(path = "/answers/questions/{qId}", produces = "application/json")
    public List<Answer> getRecentAnswerByQId(@PathVariable("qId") Long qId, 
    												 @RequestParam(value = "recent", defaultValue = "false") boolean recent) {
    	if (recent) {
        	return answerSvc.getRecentAnswerByQId(qId);
    	}
    	return answerSvc.getAnswerByQId(qId);
    }

    
    @ExceptionHandler({ NoSuchElementException.class })
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
    }

    
}
