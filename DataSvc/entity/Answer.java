import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "`ANSWER`", schema = "public")
public class Answer{

	@Id
	@Column(name = "id")
	private Long id;
	
	@Column(name = "`userId`")
	private Long userId;
	
	@Column(name = "`questionId`")
	private Long questionId;
	
	@Column(name = "answer")
	private String answer;
	
	@Column(name = "answerdate")
	private LocalDateTime answerDate;

	@Column(name = "kit_id")
	private String kitId;

	@Column(name = "test_id")
	private Long testId;

	@Column(name = "questionnaire_id")
	private Long questionnaire_id;

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
	
}