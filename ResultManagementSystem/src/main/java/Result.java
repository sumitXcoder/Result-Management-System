
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import utility.DB;
import utility.DB.Query;

@WebServlet("/Result")
public class Result extends HttpServlet {
	private static final long serialVersionUID = 1L;

	static String css = """
			    <style>
					        body {
					            margin: 0;
					            display: flex;
					            flex-direction: column;
					            align-items: center;
					        }

					        .container {
					            display: flex;
					            flex-direction: column;
					            align-items: center;
					            width: 95%;
					            font-family: sans-serif;
					            border: 1px solid #ddd;
					            margin-top: 2.5em;
					        }

					        h1 {
					            width: 100%;
					            text-align: center;
					            margin-top: 0;
					            padding-block: .3em .5em;
					            font-family: sans-serif;
					            box-shadow: 0 2px 10px 1px rgba(0, 0, 0, .1);
					        }

					        h1>div {
					            width: 5em;
					            display: inline-block;
					            line-height: .5em;
					        }

					        h1 span {
					            font-size: .5em;
					            letter-spacing: 7px;
					            margin-left: .25em;
					        }

					        img {
					            height: 2em;
					            position:relative;
					            left:.25em;
					            top:.5em;
					        }

					        .exam-results {
					            display: flex;
					            align-items: center;
					            justify-content: center;
					            color: black;
					            width: 100%;
					            background-color: #efefef;
					            height: 5em;
					            border-radius: .25em;
					            margin-block: 0 3em;
					            letter-spacing: 1px;
					            font-weight: 400;
					        }

					        .details,
					        .results {
					            display: flex;
					            width: 85%;
					            justify-content: space-between;
					            margin-bottom: 3em;
					            height:max-content;
					        }

					        .details>div {
					            font-size: .85em;
					        }

					        .details>div>div {
					            font-size: 1rem;
					            letter-spacing: 2px;
					            margin-top:5px;
					        }

					        table,
					        th,
					        td {
					            border: 1px solid #999;
					            text-align: center;
					            color: #666;
					        }

					        th,
					        td {
					            padding: .5em .25em;
					        }

					        th {
					            background-color: rgb(63, 63, 116);
					            padding-block: 1.5em;
					            color: white;
					            font-weight: 400;
					        }

					        td {
					            font-weight: 200;
					        }

					        tr>td:nth-child(3) {
					            text-align: left;
					        }

					        table {
					            width: 75%;
					            border-collapse: collapse;
					        }

					        table>caption {
					            margin-bottom: 1em;
					            font-weight: bold;
					            letter-spacing: 1px;
					        }

					        .results {
					            margin-top: 3em;
					        }

					        .results>div {
					            height: 3em;
					            padding: .25em 1.5em;
					            line-height: 3em;
					            background-color: #eee;
					            border-radius: .5em;
					        }

					        button {
					            background-color: blue;
					            color: white;
					            font-size: 1.1em;
					            padding: .5em 1em;
					            border-radius: .25em;
					            cursor: pointer;
					            margin-block: 2.5em;
					            border-radius:10px;
					        }
					    </style>
			""";

	static PrintWriter out;

	public Result() {
		super();
	}

	static String resultTable(ResultSet rs, int sem) throws SQLException {
		int i = 1, col = 1;
		String table = String.format("""
				 <table>
					<caption>Semester %d</caption>
				          <tr>
				                <th>S.No.</th>
				                <th>Course Code</th>
				                <th>Course Name</th>
				                <th>Grade</th>
				                <th>Credits</th>
				                <th>Status</th>
				            </tr>
				""", sem);

		while (rs.next()) {
			table += "<tr><td>" + Integer.toString(col++) + ".</td>";
			for (i = 1; i < 5; i++) {
				table += "<td>" + rs.getObject(i) + "</td>";
			}
			table += "<td>";
			String status = rs.getString("status");
			if (status.startsWith("SAT"))
				table += "<span style=\"color:limegreen\">" + status
						+ " </span>";
			else {
				char[] ch = rs.getString("status").toCharArray();
				for (char c : ch) {
					if (c == 'F')
						table += "<span style=\"color:red\">" + c + " </span>";
					else
						table += "<span style=\"color:limegreen\">" + c
								+ " </span>";
				}
				table += "</tr>";
			}
		}
		table += "</table>";

		return table;
	}

	
	static String getSemResult(String rollno, int n) throws SQLException {

		PreparedStatement[] ps = new PreparedStatement[3];
		ResultSet[] rs = new ResultSet[3];
		String query, sem, sgpa, cgpa, cr, tc;

			ps[0] = DB.getPreparedStatement(Query.TABLE);
			ps[0].setString(1, rollno);
			ps[0].setInt(2, n);

			sem = Integer.toString(n);
			query = String.format(Query.GPA.query, sem, sem, sem, sem);
			ps[1] = DB.conn.prepareStatement(query);
			ps[1].setString(1, rollno);

			ps[2] = DB.getPreparedStatement(Query.CREDITS);
			ps[2].setString(1, rollno);
			ps[2].setInt(2, n);
			
			for(int i=0;i<3;i++) {
				rs[i] = ps[i].executeQuery(); 
				rs[i].next();
			}
			
			sgpa = rs[1].getString(1);
			cgpa = rs[1].getString(2);
			cr = rs[1].getString(1);
			tc = rs[1].getString(2);
			
			if(sgpa == null && cgpa == null && cr == null && tc == null)
				return "<h2 style = \" color : gray\">Unable to fetch the result for Semester "+sem+".<br> Please contact your administrative department</h2>";
			else {
				return resultTable(rs[0], n) + """
					<div class="results">
					<div>
					    	SGPA :""" + " " + sgpa + """
					</div>
					    <div>CGPA :""" + " " + cgpa+ """
					</div>
					<div>Total secured credits : """
					+ cr + " / " + tc + "</div></div>" ;				
			}
		
	}
	

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String Result = "";
		String rollno = request.getParameter("rollno").toUpperCase();
		String getAllSems = request.getParameter("all-sem");
		out = response.getWriter();
		
		try {
			
			DB.connect();
			
			PreparedStatement name = DB.getPreparedStatement(Query.NAME);
			name.setString(1, rollno);
			ResultSet nameRS = name.executeQuery();
			nameRS.next();
			
			if (getAllSems != null) {
				Result = getSemResult(rollno, 1);
				for (int i = 2; i < 6; i++) 
					Result += "<br><br><br>" + getSemResult(rollno, i);
			}
			else {
				int sem = Integer.parseInt(request.getParameter("sem"));
				Result = getSemResult(rollno, sem);
			}
				

			out.println("""
					<!DOCTYPE html>
					<html>
					<head>
					    <title>Anurag University Results</title>
					""" + css + """
					</head>

					<body>
					    <h1>
					    <img src = "au.png"/> Anurag University
					    </h1>
					    <div class="container">
					        <h3 class="exam-results">Exam Results</h3>
					        <div class="details">
					            <div>Hallticket Number
					                <div>""" + rollno + """
					                		</div></div>
						<div>Student Name<div>
					""" + nameRS.getString(1) + """
					</div>
					          </div>
					          <div>Program
					              <div>B.TECH in CSE</div>
					          </div>
					      </div>
							""" +
							Result
							    + """		    
							    </div>
							    <button onclick="window.print()">Print</button>
							</body>
							</html>""");

		} catch (Exception e) {
			out.println(e.getMessage());
//			response.sendRedirect("Form.html");
		}
	}

}
