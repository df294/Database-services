/**
 * The AnswerLogicSvc is an endpoint for querying the ANSWER table of the database.
 * This service does not access the database but relies on communicating with AnswerSvc.
 * 
 * NOTE: As of 7/25/19 the pom.xml file is set only for local database access.
 * 
 * @param /answer-logic/map								returns hashmap of questionId:answer pairs for userId
 * @param /answer-logic/map/users/{userid}				returns hashmap of userId:hashmap pairs for ANSWER table,
 * 											 				where value hashmap is of questionId:answer pairs for userId
 * 
 * @param /answer-logic/map/questions/{questionid} 		returns hashmap of answer:list pairs for a questionId
 * 															where the list comprises of row entries with the given answer
 * 
 * @param /answer-logic/users/age/max/{years}			returns set of userIds corresponding to ages less than years
 * @param /answer-logic/users/age/min/{years}			returns set of userIds corresponding to ages at least years
 * @param /answer-logic/users/height/max/{inches}		returns set of userIds corresponding to height under inches
 * @param /answer-logic/users/height/min/{inches}		returns set of userIds corresponding to height at least inches
 * @param /answer-logic/users/bmi/max/{bmi}				returns set of userIds corresponding to bmi less than input
 * @param /answer-logic/users/bmi/min/{bmi}				returns set of userIds corresponding to bmi at least input
 * 
 * @param /answer-logic/users/questions/{questionid}/answers/{answer}?minAnswerDate={minAnswerDate}
 * 		returns set of userIds corresponding to question and answer, ignoring answers older than minAnswerDate
 * @param /answer-logic/users/questions/any/{questionid_answers}?minAnswerDate={minAnswerDate}
 * 		returns set of userIds with one of several answers for question, ignoring answers older than minAnswerDate
 * 
 * @param /answer-logic/users/questions/{questionid}/min/{minval}?minAnswerDate={minAnswerDate}
 * 		returns set of userIds with answers at least a value for question, ignoring answers older than minAnswerDate
 * @param /answer-logic/users/questions/{questionid}/max/{maxval}?minAnswerDate={minAnswerDate}
 * 		returns set of userIds with answers less than than a value for question, ignoring answers older than minAnswerDate
 * 
 * The functions beginning with /users/ is intended to efficiently identify users for selection modules. These functions 
 * return Set objects of users, for which native intersection and union operations can be applied in other modules to easily identify 
 * userIds meeting multiple conditions.
 * 
 * Note that ALL functions EXCEPT /answer, /answer/user, and /answer/question use only the
 * most recent answers in the case of multiple answers to the same questionId. For example, if a user
 * provides answer "Fair" to question 18 and later provides answer "Good", both answers appear in /answer
 * but only "Good" appears on functions like users/q-and-a/.
 * 
 * @author David Fu
 */
import java.time.LocalDate;
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
    private AnswerLogicService answerLogicSvc;
    
	/**
	 * Returns a HashMap of HashMaps for all answers for all users from the ANSWER table.
	 * 
	 * The keys of the returned outer HashMap draw from the userId field from the ANSWER table, a Long variable. The
	 * value corresponding to each userId key is a HashMap<Long, String> mapping all questionId and answers of that
	 * user.
	 * 
	 * The keys of the inner HashMap draw from the questionId field from the ANSWER table, a Long variable. The
	 * value corresponding to each questionId key is the String answer for that questionId for the given userId.
	 * 
	 * Uses most recent answers only.
	 * 
	 * @return JSON HashMap<Long, HashMap> mapping userId to a HashMap of questionId::answer for that user.
	 */
    
    @GetMapping(path = "/answer-logic/map", produces = "application/json")
    public HashMap<Long, HashMap> getAll() {
        return answerLogicSvc.getAllUserHashmap();
    }
    
	/**
	 * Returns a HashMap for a given user, mapping questionId to their answers for the ANSWER table.
	 * 
	 * The keys of the returned HashMap draw from the questionId field from the ANSWER table, a Long variable. The
	 * value corresponding to each questionId key is the String answer for that questionId for the given userId.
	 * 
	 * Uses most recent answers only.
	 * 
	 * @param userId - an entry from the userId field from the ANSWER table
	 * @return JSON HashMap<Long, String> mapping questionId to answer.
	 */
    
    @GetMapping(path = "/answer-logic/map/users/{userid}", produces = "application/json")
    public HashMap<Long, String> getHashmapByUserId(@PathVariable("userid") Long userId){
    	return answerLogicSvc.getHashmapByUserId(userId);
    }
    
	/**
	 * Returns a HashMap mapping answer strings for a given question to row entries from the ANSWER table.
	 * 
	 * The keys of the returned HashMap draw from the answer field from the ANSWER table, a String variable. The
	 * value corresponding to each answer String is a List<Answer>, representing all answers in the ANSWER
	 * table matching that answer String.
	 * 
	 * Uses most recent answers only.
	 * 
	 * @param qId - an entry from the questionId field from the ANSWER table
	 * @return JSON HashMap<String, List> mapping answers to lists of table entries containing that answer
	 */
    
    @GetMapping(path = "/answer-logic/map/questions/{questionid}", produces = "application/json")
    public HashMap<String, List> getHashmapByQId(@PathVariable("questionid") Long qId) {
    	return answerLogicSvc.getHashmapByQId(qId);
    }
    
	/**
	 * Returns users STRICTLY younger than a given age in years from the ANSWER table.
	 * 
	 * The userId entries returned include only users strictly younger than the input age. For instance,
	 * if input is 18, a user turning 18 tomorrow is returned, but a user turning 18 today is not. 
	 * Uses most recent answers only.
	 * 
	 * @param years - the age in years users must be younger than
	 * @return JSON Set<Long> of userId entries corresponding to ages under the given age
	 */
    
    @GetMapping(path = "/answer-logic/users/age/max/{years}", produces = "application/json")
    public Set<Long> youngerThan(@PathVariable("years") int years) {
    	return answerLogicSvc.youngerThan(years);
    }
    
	/**
	 * Returns users at least a given age in years from the ANSWER table.
	 * 
	 * The userId entries returned includes users strictly equal or older than the input age.
	 * Uses most recent answers only.
	 * 
	 * @param years - the age in years users must be equal to or older than
	 * @return JSON Set<Long> of userId entries corresponding to ages under the given age
	 */
    
    @GetMapping(path = "/answer-logic/users/age/min/{years}", produces = "application/json")
    public Set<Long> atLeastAge(@PathVariable("years") int years) {
    	return answerLogicSvc.atLeastAge(years);
    }
    
	/**
	 * Returns users STRICTLY less than a given height in inches from the ANSWER table.
	 * 
	 * The userId entries returned include only users strictly under the input height. For instance, if the
	 * input is 72, users of 71 inches would be included but users of 72 would not. 
	 * 
	 * @param inches - the height in inches that users must be shorter than
	 * @return JSON Set<Long> of userId entries corresponding to heights under the given height
	 */
    
    @GetMapping(path = "/answer-logic/users/height/max/{inches}", produces = "application/json")
    public Set<Long> underHeight(@PathVariable("inches") int inches) {
    	return answerLogicSvc.underHeight(inches);
    }
    
	/**
	 * Returns users at least a given height in inches from the ANSWER table.
	 * 
	 * The userId entries returned includes users strictly equal to or over the input height.
	 * Uses most recent answers only.
	 * 
	 * @param inches - the height in inches that users must be shorter than
	 * @return JSON Set<Long> of userId entries corresponding to heights under the given height
	 */
    
    @GetMapping(path = "/answer-logic/users/height/min/{inches}", produces = "application/json")
    public Set<Long> atLeastHeight(@PathVariable("inches") int inches) {
    	return answerLogicSvc.atLeastHeight(inches);
    }
    

	/**
	 * Returns users STRICTLY less than an input BMI given their weight and height from the ANSWER table.
	 * 
	 * The userId entries returned includes users strictly under the input BMI. For example, if the input
	 * is 17, a user with BMI of 16.3 would be returned but not a user of BMI 17.0. The BMI formula used is
	 * 703 * weight / height / height.
	 * Uses most recent answers only.
	 * 
	 * @param bmi - the BMI that users must be less than
	 * @return JSON Set<Long> of userId entries corresponding to BMIs under the given input
	 */
    
    @GetMapping(path = "/answer-logic/users/bmi/max/{bmi}", produces = "application/json")
    public Set<Long> underBMI(@PathVariable("bmi") double bmi) {
    	return answerLogicSvc.underBMI(bmi);
    }
    
	/**
	 * Returns users at least an input BMI given their weight and height from the ANSWER table.
	 * 
	 * The userId entries returned includes users strictly over to and equal to the input BMI. The BMI formula used is
	 * 703 * weight / height / height.
	 * Uses most recent answers only.
	 * 
	 * @param bmi - the BMI that users must be more than
	 * @return JSON Set<Long> of userId entries corresponding to BMIs equal to or over the given input
	 */
    
    @GetMapping(path = "/answer-logic/users/bmi/min/{bmi}", produces = "application/json")
    public Set<Long> atLeastBMI(@PathVariable("bmi") double bmi) {
    	return answerLogicSvc.atLeastBMI(bmi);
    }
    
    
	/**
	 * Returns users giving specified case-sensitive answer to specified question.
	 * No answers older than minAnswerDate (format: 'YY-MM-DD') will be considered for finding users.
	 * 
	 * The userId entries returned include all users who answered with the input string to the given questionId.
	 * Uses most recent answers only. 
	 * 
	 * @param questionId 	- an entry from the questionId column of the Answer table
	 * @param answer     	- an entry from the answer column of the Answer table
	 * @param minAnswerDate - a YY-MM-DD date for which older answers will be ignored
	 * @return JSON Set<Long> of userId entries corresponding to the input question-answer combination
	 */
    
    @GetMapping(path = "/answer-logic/users/questions/{questionid}/answers/{answer}", produces = "application/json")
    public Set<Long> userSelect(@PathVariable("questionid") Long qId, 
    							@PathVariable("answer") String answer,
    							@RequestParam(value = "minAnswerDate", defaultValue = "2000-01-01") String minAnswerDate) {
    	return answerLogicSvc.userSelect(qId, answer, minAnswerDate);
    }
    
	/**
	 * Returns users giving any of several specified case-sensitive answers to specified question.
	 * No answers older than minAnswerDate (format: 'YY-MM-DD') will be considered for finding users.
	 * 
	 * The userId entries returned include all users whose answer matches one of the input strings for the
	 * input question. The parameter Q_answer to this function must be formatted as follows:
	 * 
	 * "[questionId]~[answer string 1]~[answer string 2]...~[answer string n]"
	 * 
	 * For example, 18~good~fair~excellent is a valid input to retrieve all users who answered one of "good", 
	 * "fair", or "excellent" for question 18.
	 * Uses most recent answers only.
	 * 
	 * @param Q_answer - questionId:answers combination formatted as "[questionId]~[answer]...~[answer]"
	 * @param minAnswerDate - a YY-MM-DD date for which older answers will be ignored
	 * @return JSON Set<Long> of userId entries corresponding to the input question-answers combination
	 */
    
    @GetMapping(path = "/answer-logic/users/questions/any/{questionid_answers}", produces = "application/json")
    public Set<Long> userSelectMany(@PathVariable("questionid_answers") String questionId_answers, 
    								@RequestParam(value = "minAnswerDate", defaultValue = "2000-01-01") String minAnswerDate) {
    	return answerLogicSvc.userSelectMany(questionId_answers, minAnswerDate);
    }
    
	/**
	 * Returns users giving an answer equal to or greater than a given value for a given numeric question.
	 * No answers older than minAnswerDate (format: 'YY-MM-DD') will be considered for finding users.
	 * 
	 * The parameter to this function must be formatted as "[questionId]~[value]". For example, 20~5 is a
	 * valid input to retrieve all userIds corresponding to answers of 5 or greater for question 20.
	 * Uses most recent answers only.
	 * 
	 * @param Q_answer - questionId:value combination formatted as "[questionId]~value"
	 * @param minAnswerDate - a YY-MM-DD date for which older answers will be ignored
	 * @return JSON Set<Long> of userId entries corresponding to values at least the input value for the question
	 */
    
    @GetMapping(path = "/answer-logic/users/questions/{questionid}/min/{minval}", produces = "application/json")
    public Set<Long> ansAtLeast(@PathVariable("questionid") Long qId, @PathVariable("minval") Double minVal,
    							@RequestParam(value = "minAnswerDate", defaultValue = "2000-01-01") String minAnswerDate) {
    	return answerLogicSvc.ansAtLeast(qId, minVal, minAnswerDate);
    }
    
	/**
	 * Returns users giving an answer STRICTLY less than a given value for a given numeric question.
	 * No answers older than minAnswerDate (format: 'YY-MM-DD') will be considered for finding users.
	 * 
	 * The parameter to this function must be formatted as "[questionId]~[value]". For example, 20~5 is a
	 * valid input to retrieve all userIds corresponding to answers less than 5 for question 20.
	 * Uses most recent answers only.
	 * 
	 * @param Q_answer - questionId:value combination formatted as "[questionId]~value"
	 * @param minAnswerDate - a YY-MM-DD date for which older answers will be ignored
	 * @return JSON Set<Long> of userId entries corresponding to values less than the input value for the question
	 */
    @GetMapping(path = "/answer-logic/users/questions/{questionid}/max/{maxval}", produces = "application/json")
    public Set<Long> ansUnder(@PathVariable("questionid") Long qId, @PathVariable("maxval") Double maxVal, 
    						  @RequestParam(value = "minAnswerDate", defaultValue = "2000-01-01") String minAnswerDate) {
    	return answerLogicSvc.ansUnder(qId, maxVal, minAnswerDate);
    }

    @ExceptionHandler({ NoSuchElementException.class })
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
    }

    
}
