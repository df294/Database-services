
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.time.LocalDateTime;

public class Answer implements Comparable<Answer>{

	private Long id;
	
	private Long userId;
	
	private Long questionId;
	
	private String answer;
	
	private LocalDateTime answerDate;

	private String kitId;

	private Long testId;

	private Long questionnaireId;

	public long getUserId() {
		return userId;
	}

	public long getQuestionId() {
		return questionId;
	}

	public String getAnswer() {
		return answer;
	}

	public LocalDateTime getAnswerDate() {
		return answerDate;
	}
	
	public int compareTo(Answer other) {
		Long thisAns = Long.parseLong(this.answer);
		return thisAns.compareTo(Long.parseLong(other.getAnswer()));
	}
}