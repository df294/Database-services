import java.util.List;

import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface AnswerRepo extends JpaRepository<Answer, String>{

	List<Answer> findByUserId(Long id);
	List<Answer> findByQuestionId(Long id);
	
	@Query(value = "SELECT id, \"userId\", \"questionId\", answer, answerdate, kit_id, test_id, questionnaire_id \n" + 
				   "FROM(\n" + 
				    	"with temp as (\n" + 
				    	"SELECT row_number() over (partition by \"userId\", \"questionId\" order by answerdate desc) as row, *  \n" + 
				    	"FROM \"ANSWER\")\n" + 
				   "SELECT * from temp \n" + 
				   "WHERE row = 1) as foo",
				nativeQuery = true)
	List<Answer> findMostRecentAnswers();
	
	@Query(value = "SELECT id, \"userId\", \"questionId\", answer, answerdate, kit_id, test_id, questionnaire_id \n" + 
  				   "FROM(\n" + 
						"with temp as (\n" + 
						"SELECT row_number() over (partition by \"userId\", \"questionId\" order by answerdate desc) as row, *  \n" + 
						"FROM \"ANSWER\")\n" + 
					"SELECT * from temp \n" + 
					"WHERE row = 1 AND \"userId\" = ?1) as foo",
				nativeQuery = true)
	List<Answer> findMostRecentAnswersByUserId(Long userId);
	
	@Query(	value = "SELECT id, \"userId\", \"questionId\", answer, answerdate, kit_id, test_id, questionnaire_id  "
  				+   "FROM"
				+   	"(with temp as" + "(" + 
						"SELECT row_number() over (partition by \"userId\", \"questionId\" " +
						"ORDER BY answerdate desc) as row, * " + 
						"FROM \"ANSWER\") " + 
					"SELECT * from temp " + 
					"WHERE row = 1 AND \"questionId\" = ?1) as foo",			
				nativeQuery = true)
	List<Answer> findMostRecentAnswersByQuestionId(Long questionId);

	
}