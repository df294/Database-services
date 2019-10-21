package com.viome.study.viomedb.viomephiAnswerSvc.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.viome.dto.viomephi.AnswerDTO;
import com.viome.study.viomedb.viomephiAnswerSvc.entity.ViomephiAnswer;
import com.viome.study.viomedb.viomephiAnswerSvc.repository.ViomephiAnswerRepo;

import java.util.List;
import java.util.Optional;


@Service
public class ViomephiAnswerServiceImpl implements ViomephiAnswerService {

    @Autowired
    private ViomephiAnswerRepo answerRepo;
    
    @Override
    public List<ViomephiAnswer> getAll(){
    	return answerRepo.findAll();
    }
	
    public List<ViomephiAnswer> getAllMostRecent(){
    	return answerRepo.findMostRecentAnswers();
    }
    
	public List<ViomephiAnswer> getAnswerByUserId(Long userId){
		return answerRepo.findByUserId(userId);
	}
	

	public List<ViomephiAnswer> getRecentAnswerByUserId(Long userId){
		return answerRepo.findMostRecentAnswersByUserId(userId);
	}
	
	
	public List<ViomephiAnswer> getAnswerByQId(Long qId){
		return answerRepo.findByQuestionId(qId);
	}
	
	public List<ViomephiAnswer> getRecentAnswerByQId(Long qId){
		return answerRepo.findMostRecentAnswersByQuestionId(qId);
	}
	
}
