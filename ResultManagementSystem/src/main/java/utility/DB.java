package utility;
import java.sql.*;

public class DB {

	public enum Query {

		NAME("SELECT name FROM rollno_name WHERE rollno = ?"),
		
		TABLE("""
				SELECT DISTINCT
				    g.code,
				    s.name,
				    g.grade AS grade,
			    CASE
			        WHEN grade = 'F' THEN 0.00
			        WHEN grade = '-' THEN '-'
			        ELSE s.credits
			    END AS credits,
				   g.status
				FROM
				    grades g,
				    subject_details s
				WHERE
				   	s.code = g.code
				    	AND g.rollno = ?
				    	AND s.sem = ?;
						"""),
		
		CREDITS("""
				WITH res AS (
			 SELECT
				 DISTINCT g.code,
			 IF(g.grade <> 'F' AND g.grade <> '-',s.credits,0.00) AS cr,
			 s.credits AS tc
			 FROM 
				 grades g, subject_details s
			 WHERE 
				 s.code = g.code AND
				 g.rollno = ? AND
				 s.sem = ?
			 )
			 SELECT
			 SUM(cr) AS credits_received,
			 SUM(tc) AS total_credits
			 FROM res;
			"""),
		
		GPA("SELECT IF(sgpa%s = 0.00,'-',sgpa%s) AS sgpa, IF(cgpa%s = 0.00,'-',cgpa%s) AS cgpa  FROM gpa 			WHERE rollno = ?");
		
		
		public String query;

		Query(String query) {
			this.query = query;
		}
		
		public Query setSemester(String sem) {
			this.query = String.format(this.query,sem, sem, sem, sem);
			return this;
		}
	}
	
	private static String url = "jdbc:mysql://localhost:3306/Results";
	private static String user = "root";
	private static String password = "mysqlpassword";

	public static Connection conn;

	public static void connect() throws ClassNotFoundException, SQLException {
		if (conn != null) return;
		Class.forName("com.mysql.cj.jdbc.Driver");
		conn = DriverManager.getConnection(url, user, password);
	}
	
	public static PreparedStatement getPreparedStatement(Query query) throws SQLException {
		return conn.prepareStatement(query.query);
	}

	public static void close() throws SQLException {
		if (conn == null) return;
		conn.close();
	}
}
