/* Our API can do several functions:
 * 		Retrieve all user+question combinations, all columns
 * 		*Retrieve all answered questions, all columns
 * 		Retrieve all answers by single user
 * 		Retrieve all answers to a question
 * 		Retrieve the latest answer to a user-question combination
 * 		Retrieve all answers to a user-question combination
 */

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AnswerLogicService {

	HashMap<Long, HashMap> getAllUserHashmap();
	HashMap<Long, String> getHashmapByUserId(Long userId);
	HashMap<String, List> getHashmapByQId(Long qId);
	
	Set<Long> youngerThan(int years);
	Set<Long> atLeastAge(int years);
	
	Set<Long> underHeight(int inches);
	Set<Long> atLeastHeight(int inches);
	
	Set<Long> underBMI(double bmi);
	Set<Long> atLeastBMI(double bmi);
	
	Set<Long> userSelect(Long qId, String answer, String minAnswerDate);
	Set<Long> userSelectMany(String Q_answer, String minAnswerDate);
	
	Set<Long> ansAtLeast(Long qId, Double rangeStart, String minAnswerDate);
	Set<Long> ansUnder(Long qId, Double rangeEnd, String minAnswerDate);
	
}
