
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
public class AnswerLogicImpl implements AnswerLogicService {

    @Value("${answer_url}")
    private String AnswerUrl;
    
    @Value("${answer_url_uname}")
    private String AnswerUrlUName;

    @Value("${answer_url_pass}")
    private String AnswerUrlPass;
	
    private final RestTemplate restTemplate;
    
    private final Long ageQuestionId = 1L;
    private final Long heightQuestionId = 2L;
    private final Long weightQuestionId = 3L;
    
    public AnswerLogicImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }
    
    HttpHeaders headers = new HttpHeaders();
  
    public HashMap<Long, String> getHashmapByUserId(Long userId){ 	
    	List<Answer> userAnsList = this.getMostRecentAnswerByUserId(userId);
 
    	return getUserHashMap(userAnsList);
    }
  
    public HashMap<Long, HashMap> getAllUserHashmap(){
  	
	  	List<Answer> allUserAns = this.getAllMostRecentAnswer();	//List of all rows in ANSWER table
	  	HashMap<Long, List> userIdMap = new HashMap<Long, List>();			//Helper mapping of userIds to lists of their Answers
	  	HashMap<Long, HashMap> hmap = new HashMap<Long, HashMap>();	
	  	
	  	//For each ANSWER table row, assign that row to its userId in the userId map
	  	for (int i = 0; i < allUserAns.size(); i++) {
	  		Answer userAns = allUserAns.get(i);
	
	  		//If userIdMap contains the userId key, add the row to that key's list. Else, make a new key:list pair.
	  		if (userIdMap.containsKey(userAns.getUserId())) {
	  			userIdMap.get(userAns.getUserId()).add(userAns);
	  		}
	  		else {
	  			List<Answer> userAnsList = new ArrayList();
	  			userAnsList.add(userAns);
	  			userIdMap.put(userAns.getUserId(), userAnsList);
	  		}
	  	}	
  		
	  	List<Long> userIds = new ArrayList<Long>(userIdMap.keySet());
	  	//For each userId, use getUserHashMap() to generate userId:hashMap pairs from list stored in userIdMap
	  	for (int i = 0; i < userIds.size(); i++) {
	  		Long userIdtoAdd = userIds.get(i);
	  		hmap.put(userIdtoAdd, getUserHashMap(userIdMap.get(userIdtoAdd)));
	  	}
  	
  		return hmap;
    }
  
    public HashMap<String, List> getHashmapByQId(Long qId) {
    	List<Answer> allQAns = this.getMostRecentAnswerByQuestionId(qId);
  	
    	HashMap<String, List> hmap = new HashMap<String, List>();
  	
	  	//For each answer, if it exists as a key in hmap we insert the Object into the list for that key.
	  	//If the answer does not exist as a key, we create the key:list pair in hmap.
	  	for (int i = 0; i < allQAns.size(); i++) {
	  		Answer currAns = allQAns.get(i);
	  		String key = currAns.getAnswer();
	  		
	  		if (hmap.containsKey(key)) {
	  			hmap.get(key).add(currAns);
	  		}
	  		else {
	  			ArrayList<Answer> userList = new ArrayList();
	  			userList.add(currAns);
	  			hmap.put(key, userList);
	  		}
	  	}
	  	
	  	return hmap;
    }
  
    public Set<Long> youngerThan(int years) {
    	LocalDate date = LocalDate.now().minusYears(years);
    	Set<Long> userIdList = new HashSet<Long>();
	  	List<Answer> ansList = this.getMostRecentAnswerByQuestionId(ageQuestionId);
	  	
	  	for (int i = 0; i < ansList.size(); i++) {
	  		Answer currAns = ansList.get(i);
	  		LocalDate currDate = LocalDate.parse(currAns.getAnswer());
	  		
	  		if (currDate.compareTo(date) > 0) {
	  			userIdList.add(currAns.getUserId());
	  		}
	  	}
	  	return userIdList;
    }
  
    public Set<Long> atLeastAge(int years) {
	  	LocalDate date = LocalDate.now().minusYears(years);
	    Set<Long> userIdList = new HashSet<Long>();
	  	
	    List<Answer> ansList = this.getMostRecentAnswerByQuestionId(ageQuestionId);
	  	
	  	for (int i = 0; i < ansList.size(); i++) {
	  		Answer currAns = ansList.get(i);
	  		LocalDate currDate = LocalDate.parse(currAns.getAnswer());
	  		
	  		if (currDate.compareTo(date) < 0 || currDate.isEqual(date)) {
	  			userIdList.add(currAns.getUserId());
	  		}
	  	}
	  	return userIdList;
    }
  
    public Set<Long> underHeight(int inches) {
	  	Set<Long> userIdList = new HashSet<Long>();
	  	List<Answer> ansList = this.getMostRecentAnswerByQuestionId(heightQuestionId);
	  	
	  	for (int i = 0; i < ansList.size(); i++) {
	  		String currAns = ansList.get(i).getAnswer();
	  		if (currAns.isEmpty() 			||				//This block skips incorrectly formatted test db entries
	  			!currAns.contains("ft") 	||				
	  			!currAns.contains("in")		||
	  			currAns.contains(".")
	  			){
	  			continue;
	  		}
	  		
	  		if (convertToInches(currAns) < inches) {
	  			userIdList.add(ansList.get(i).getUserId());
	  		}
	  	}
	  	return userIdList;	
    }
  
    public Set<Long> atLeastHeight(int inches) {
    	Set<Long> userIdList = new HashSet<Long>();
	  	List<Answer> ansList = this.getMostRecentAnswerByQuestionId(heightQuestionId);
	  	
	  	int currHeight, idxFoot, idxIn, currFt, currIn;
	  	
	  	for (int i = 0; i < ansList.size(); i++) {
	  		String currAns = ansList.get(i).getAnswer();
	  		if (currAns.isEmpty() 			||				//This block skips incorrectly formatted test db entries
	  			!currAns.contains("ft") 	||
	  			!currAns.contains("in")		||
	  			currAns.contains(".")
	  			){
	  			continue;
	  		}
	  		
	  		if (convertToInches(currAns) >= inches) {
	  			userIdList.add(ansList.get(i).getUserId());
	  		}
	  	}
	  	
	  	return userIdList;
    }
  
    public Set<Long> underBMI(double bmi) {
    	Set<Long> userIdList = new HashSet<Long>();
	  	List<Answer> heightList = this.getMostRecentAnswerByQuestionId(heightQuestionId);
	  	List<Answer> weightList = this.getMostRecentAnswerByQuestionId(weightQuestionId);
	  	
	  	HashMap<Long, Double> weightMap = new HashMap<Long, Double>();		//Helper hashMap of user:weight mappings
	  	
	  	Answer currAns;
	  	double weight, height, currBMI;
	  	
	  	//For each weight answer to questionId = 3, we add a pair to the user:weight helper hashMap
	  	for (int i = 0; i < weightList.size(); i++) {
	  		currAns = weightList.get(i);
	  		weight = Double.parseDouble(currAns.getAnswer());
	  		weightMap.put(currAns.getUserId(), weight);
	  	}
	  	
	  	//For each height answer to questionId = 2, we use that value with that user's weight to calculate BMI
	  	for (int i = 0; i < heightList.size(); i++) {
	  		currAns = heightList.get(i);
	  		height = Double.valueOf(
	  					convertToInches(currAns.getAnswer())
	  					);
	  		weight = weightMap.get(currAns.getUserId());
	  		currBMI = (703*weight) / (height*height);
	  		
	  		if (currBMI < bmi) {
	  			userIdList.add(currAns.getUserId());
	  		}	
	  	}
	 
	  	return userIdList;
    }
  
    public Set<Long> atLeastBMI(double bmi) {
    	Set<Long> userIdList = new HashSet<Long>();
    	List<Answer> heightList = this.getMostRecentAnswerByQuestionId(heightQuestionId);
	  	List<Answer> weightList = this.getMostRecentAnswerByQuestionId(weightQuestionId);
	  	
	  	HashMap<Long, Double> weightMap = new HashMap<Long, Double>();		//Helper hashMap of user:weight mappings
	  	
	  	Answer currAns;
	  	double weight, height, currBMI;
	  	
	  	//For each weight answer to questionId = 3, we add a pair to the user:weight helper hashMap
	  	for (int i = 0; i < weightList.size(); i++) {
	  		currAns = weightList.get(i);
	  		weight = Double.parseDouble(currAns.getAnswer());
	  		weightMap.put(currAns.getUserId(), weight);
	  	}
	  	
	  	//For each height answer to questionId = 2, we use that value with that user's weight to calculate BMI
	  	for (int i = 0; i < heightList.size(); i++) {
	  		currAns = heightList.get(i);
	  		height = Double.valueOf(
	  					convertToInches(currAns.getAnswer())
	  					);
	  		weight = weightMap.get(currAns.getUserId());
	  		currBMI = (703*weight) / (height*height);
	  		
	  		if (currBMI >= bmi) {
	  			userIdList.add(currAns.getUserId());
	  		}	
	  	}
 
	  	return userIdList;
    }
  
	public Set<Long> userSelect(Long qId, String answer, String minAnswerDate) {		

		Set<Long> userIdList = new HashSet<Long>();
	  	List<Answer> ansList = this.getMostRecentAnswerByQuestionId(qId);
	  	
	  	LocalDateTime minDate = getDateFromString(minAnswerDate);

	  	for (int i = 0; i < ansList.size(); i++) {
	  		if (answer.equals(ansList.get(i).getAnswer()) &&
	  			(ansList.get(i).getAnswerDate().compareTo(minDate) >= 0 || ansList.get(i).getAnswerDate().equals(minDate))){
	  			
	  			userIdList.add(ansList.get(i).getUserId());
	  		}
	  	}
	  	
	  	return userIdList;
    }
  
	public Set<Long> userSelectMany(String questionId_answers, String minAnswerDate) {
		Set<Long> userIdList = new HashSet<Long>();

	  	List<String> strList = new ArrayList<String>(Arrays.asList(questionId_answers.split("~")));
	  	Long qId = Long.parseLong(strList.get(0));
	  	strList.remove(0);
	  	
	  	LocalDateTime minDate = getDateFromString(minAnswerDate);

	  	List<Answer> ansList = this.getMostRecentAnswerByQuestionId(qId);
	  	
	  	for (int i = 0; i < ansList.size(); i++) {
	  		if (strList.contains(ansList.get(i).getAnswer()) &&
	  			(ansList.get(i).getAnswerDate().compareTo(minDate) >= 0 || ansList.get(i).getAnswerDate().equals(minDate))
	  				){
	  			userIdList.add(ansList.get(i).getUserId());
	  		}
	  	}
	  	
	  	return userIdList;
	}
  
	public Set<Long> ansAtLeast(Long qId, Double rangeStart, String minAnswerDate) {
		Set<Long> userIdList = new HashSet<Long>();
  	
	  	List<Answer> ansList = this.getMostRecentAnswerByQuestionId(qId);
	  	
	  	LocalDateTime minDate = getDateFromString(minAnswerDate);

	  	Double currAns = 0.0;
	  	for (int i = 0; i < ansList.size(); i++) {
	  		
	  		try {
	      		currAns = Double.parseDouble(ansList.get(i).getAnswer());
	  		}
	  		catch(Exception e) {
	  			System.err.println("Answer data for input questionId is not numeric!");
	  			return null;
	  		}
	  		
	  		if (currAns >= rangeStart &&
	  			(ansList.get(i).getAnswerDate().compareTo(minDate) >= 0 || ansList.get(i).getAnswerDate().equals(minDate))){
	  			userIdList.add(ansList.get(i).getUserId());
	  		}
	  	}
	  	
	  	return userIdList;
	}
  
  	public Set<Long> ansUnder(Long qId, Double rangeEnd, String minAnswerDate) {
  		Set<Long> userIdList = new HashSet<Long>();
  	
	  	List<Answer> ansList = this.getMostRecentAnswerByQuestionId(qId);
	  	
	  	LocalDateTime minDate = getDateFromString(minAnswerDate);

	  	Double currAns = 0.0;
	  	for (int i = 0; i < ansList.size(); i++) {
	  		try {
	      		currAns = Double.parseDouble(ansList.get(i).getAnswer());
	  		}
	  		catch(Exception e) {
	  			System.err.println("Answer data for input questionId is not numeric!");
	  			return null;
	  		}
	  		
	  		if (currAns < rangeEnd && 
	  			(ansList.get(i).getAnswerDate().compareTo(minDate) >= 0 || ansList.get(i).getAnswerDate().equals(minDate))){
	  			userIdList.add(ansList.get(i).getUserId());
	  		}
	  	}
  	
	  	return userIdList;
  	} 

  /*
   * 	getUserHashMap() is a helper method that returns a hashmap of questionId:answer pairs for an
   * 	input list of Answer objects. The input list objects must contain the same userId. 
   */

  private HashMap<Long, String> getUserHashMap(List<Answer> userAnsList){
  	HashMap<Long, String> hmap = new HashMap<Long, String>();						
  	HashMap<Long, LocalDateTime> dateMap = new HashMap<Long, LocalDateTime>(); //Indicates most recent answer	
  
  	//For each Q&A, we check if the question has already been answered, and add the most recent answer
  	for (int i = 0; i < userAnsList.size(); i++) {
  		Answer userAns = userAnsList.get(i);
  		long qId = userAns.getQuestionId();
  		LocalDateTime userAnsDate = userAns.getAnswerDate();
  		
  		//If the questionId has been seen already, compare the date value of the competing entries
  		if (hmap.containsKey(qId) && dateMap.containsKey(qId)) {
  			if (userAnsDate.compareTo(dateMap.get(qId)) > 0){
  				hmap.put(qId, userAns.getAnswer());
  				dateMap.put(qId, userAnsDate);
  			}
  		}
  		else {
  			hmap.put(qId, userAns.getAnswer());
  			if (userAns.getAnswerDate() != null) {
  				dateMap.put(qId, userAns.getAnswerDate());
  			}
  		}
  	}
  	
  	return hmap;
  }

  /*
   * convertToInches is a helper method that converts strings from "[n]ft [m]in" format, for answers to questionId=2, 
   * to int value in inches.
   */
  private int convertToInches(String answer) {
  	int currHeight, idxFoot, idxIn, currFt, currIn;
  	
		idxFoot = answer.indexOf("ft");
		idxIn = answer.indexOf("in");
		int breakLen = 3;
		
		currFt = Integer.parseInt(answer.substring(0, idxFoot));
		currIn = Integer.parseInt(answer.substring(idxFoot+breakLen, idxIn));
		
		currHeight = currFt*12 + currIn;
  	
  	return currHeight;
  }
  
  /*
   * getDateFromString is a helper method that converts strings from "YY-MM-DD" format to a LocalDateTime object.
   */
  private LocalDateTime getDateFromString(String dateString) {
	  
	  List<String> strList = new ArrayList<String>(Arrays.asList(dateString.split("-")));
	  int minYear = Integer.parseInt(strList.get(0));
	  int minMonth = Integer.parseInt(strList.get(1));
	  int minDay = Integer.parseInt(strList.get(2));
	  LocalDateTime localDateTime = LocalDateTime.of(minYear, minMonth, minDay, 0, 0, 0, 0);
	  
	  return localDateTime;
  }
	
	
	//Helper methods to call the AnswerSvc for data from the ANSWER table
	private List<Answer> getAllMostRecentAnswer(){
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBasicAuth(AnswerUrlUName, AnswerUrlPass);
        
    	ResponseEntity<List<Answer>> responseEntity = restTemplate.exchange(
        		AnswerUrl + "?recent=true",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Answer>>(){});

    	return responseEntity.getBody();
	}
	
	private List<Answer> getMostRecentAnswerByUserId(Long userId){
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBasicAuth(AnswerUrlUName, AnswerUrlPass);
        
    	ResponseEntity<List<Answer>> responseEntity = restTemplate.exchange(
        		AnswerUrl + "/users/" + userId.toString() + "?recent=true",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Answer>>(){});

    	return responseEntity.getBody();
		
	}
	
	private List<Answer> getMostRecentAnswerByQuestionId(Long questionId){
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBasicAuth(AnswerUrlUName, AnswerUrlPass);
        
    	ResponseEntity<List<Answer>> responseEntity = restTemplate.exchange(
        		AnswerUrl + "/questions/" + questionId.toString() + "?recent=true",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<Answer>>(){});

    	return responseEntity.getBody();
	}
	
}
