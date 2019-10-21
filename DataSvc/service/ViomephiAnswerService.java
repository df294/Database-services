/* Our API can do several functions:
 * 		Retrieve all user+question combinations, all columns
 * 		*Retrieve all answered questions, all columns
 * 		Retrieve all answers by single user
 * 		Retrieve all answers to a question
 * 		Retrieve the latest answer to a user-question combination
 * 		Retrieve all answers to a user-question combination
 */

package com.viome.study.viomedb.viomephiAnswerSvc.service;

import java.util.List;
import java.util.Optional;

import com.viome.dto.viomephi.AnswerDTO;
import com.viome.study.viomedb.viomephiAnswerSvc.entity.ViomephiAnswer;

public interface ViomephiAnswerService {

	List<ViomephiAnswer> getAll();
	
	List<ViomephiAnswer> getAllMostRecent();
	
	List<ViomephiAnswer> getAnswerByUserId(Long userId);

	List<ViomephiAnswer> getRecentAnswerByUserId(Long userId);
	
	List<ViomephiAnswer> getAnswerByQId(Long qId);
	
	List<ViomephiAnswer> getRecentAnswerByQId(Long qId);

}
