package bbs;

import java.awt.Window.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;

import javax.servlet.http.Part;

//DAO 클래스는 데이터베이스에 접근하여 데이터를 처리하는 역할을 수행합니다.
public class BbsDAO {
	
	private Connection conn;	// 데이터베이스 연결을 위한 Connection 객체
	private ResultSet rs;		// 결과셋을 담기 위한 ResultSet 객체
	
	// 생성자: 데이터베이스 연결을 초기화합니다.
	public BbsDAO() {
		try {
		// SQL Server JDBC 드라이버 클래스를 로드합니다.
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		// 데이터베이스 연결 정보를 설정합니다.
		String connectionUrl = "jdbc:sqlserver://smtv.iptime.org:2433;databaseName=Notice;integratedSecurity=false;"
				+ "encrypt=false;trustServerCertificate=true;"
				+ "user=sa;"
				+ "password=@admin9150;";
		// Connection을 생성하여 데이터베이스에 접속합니다.
		conn = DriverManager.getConnection(connectionUrl);
		System.out.println("서버접속 성공");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 현재 날짜와 시간을 가져오는 메소드
	public String getDate() {
		String SQL = "SELECT NOW()";
		try {
			PreparedStatement pstmt = conn.prepareStatement(SQL);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	// 다음 게시글 번호를 가져오는 메소드
	public int getNext() {
		String SQL = "SELECT Board_ID FROM CS_Ques ORDER BY Board_ID DESC";
		try {
			PreparedStatement pstmt = conn.prepareStatement(SQL);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) + 1;
			}
			return 1; // 첫 번째 게시물인 경우
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1; // 데이터베이스 오류
	}
	

	public int write(String comment_ID, String title, String contents, String fileName) {
	    String SQL = "INSERT INTO CS_Ques (Comment_ID, Title, Contents, File_Name, Ins_Date_Time) VALUES (?, ?, ?, ?, GETDATE())";
	    try {
	        PreparedStatement pstmt = conn.prepareStatement(SQL);
	        pstmt.setString(1, comment_ID);
	        pstmt.setString(2, title);
	        pstmt.setString(3, contents);
	        
	        // 파일 이름을 바이트 배열로 변환하여 저장
	        byte[] fileNameBytes = fileName.getBytes("UTF-8");
	        pstmt.setBytes(4, fileNameBytes);

	        return pstmt.executeUpdate(); 
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return -1; // 데이터베이스 오류
	}



	
	// 페이지에 해당하는 게시글 목록을 가져오는 메소드
	public ArrayList<Bbs> getList(int pageNumber, int pageSize) {
	    int offset = (pageNumber - 1) * pageSize; // 페이지 번호에 따른 오프셋 계산

	    String SQL = "SELECT * FROM CS_Ques ORDER BY Board_ID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
	    ArrayList<Bbs> list = new ArrayList<Bbs>();
	    try {
	        PreparedStatement pstmt = conn.prepareStatement(SQL);
	        pstmt.setInt(1, offset);
	        pstmt.setInt(2, pageSize);
	        rs = pstmt.executeQuery();
	        while (rs.next()) {
	            Bbs bbs = new Bbs();
	            bbs.setBoard_ID(rs.getInt("Board_ID")); 
	            bbs.setComment_ID(rs.getString("Comment_ID"));
	            bbs.setTitle(rs.getString("Title"));
	            bbs.setContents(rs.getString("Contents"));
	            bbs.setIns_Date_Time(rs.getString("Ins_Date_Time"));
	            list.add(bbs);
	        }
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return list;
	}

	public boolean nextPage(int pageNumber, int pageSize) {
	    String SQL = "SELECT COUNT(*) FROM CS_Ques";
	    
	    try {
	        PreparedStatement pstmt = conn.prepareStatement(SQL);
	        rs = pstmt.executeQuery();
	        if (rs.next()) {
	            int totalRecords = rs.getInt(1);
	            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
	            return pageNumber < totalPages;
	        }
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return false;
	}




	// 특정 게시글 정보를 가져오는 메소드
	public Bbs getBbs(int board_id) {
	    String SQL = "SELECT * FROM CS_Ques WHERE Board_ID = ?";
	    
	    try {
	        PreparedStatement pstmt = conn.prepareStatement(SQL);
	        pstmt.setInt(1, board_id);
	        rs = pstmt.executeQuery();
	        if (rs.next()) {
	            Bbs bbs = new Bbs();
	            bbs.setBoard_ID(rs.getInt("Board_ID"));
	            bbs.setComment_ID(rs.getString("Comment_ID"));
	            bbs.setTitle(rs.getString("Title"));
	            bbs.setContents(rs.getString("Contents"));
	            bbs.setIns_Date_Time(rs.getString("Ins_Date_Time"));
	            return bbs;
	        }            
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	// 게시글 수정 메소드
	public int update(int board_id, String title, String contents) {
	    String SQL = "UPDATE CS_Ques SET Title = ?, Contents = ?, Upd_Date_Time = GETDATE() WHERE Board_ID =?";
	    try {
	        PreparedStatement pstmt = conn.prepareStatement(SQL);
	        pstmt.setString(1, title);
	        pstmt.setString(2, contents);
	        pstmt.setInt(3, board_id);
	        
	        return pstmt.executeUpdate(); 
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return -1; // 데이터베이스 오류
	}
	
	// 게시글 삭제 메소드
	public int delete(int board_id) {
		String SQL = "DELETE FROM CS_Ques WHERE Board_ID = ?";
		try {
			PreparedStatement pstmt = conn.prepareStatement(SQL);
			pstmt.setInt(1, board_id);
			
			return pstmt.executeUpdate(); 
		} catch(Exception e) {
			e.printStackTrace();
		}
		return -1; // 데이터베이스 오류
	}
	
	  public static String getFile_Name(Part part) {
	        String contentDisposition = part.getHeader("content-disposition");
	        String[] elements = contentDisposition.split(";");
	        for (String element : elements) {
	            if (element.trim().startsWith("filename")) {
	                return element.substring(element.indexOf('=') + 1).trim().replace("\"", "");
	            }
	        }
	        return "";
	    }

	  
	  public ArrayList<Bbs> searchList(String searchKeyword, int pageNumber, int pageSize) {
		    int offset = (pageNumber - 1) * pageSize;

		    String SQL = "SELECT * FROM CS_Ques WHERE Title LIKE ? ORDER BY Board_ID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
		    ArrayList<Bbs> list = new ArrayList<Bbs>();
		    try {
		        PreparedStatement pstmt = conn.prepareStatement(SQL);
		        pstmt.setString(1, "%" + searchKeyword + "%");
		        pstmt.setInt(2, offset);
		        pstmt.setInt(3, pageSize);
		        rs = pstmt.executeQuery();
		        while (rs.next()) {
		            Bbs bbs = new Bbs();
		            bbs.setBoard_ID(rs.getInt("Board_ID")); 
		            bbs.setComment_ID(rs.getString("Comment_ID"));
		            bbs.setTitle(rs.getString("Title"));
		            bbs.setContents(rs.getString("Contents"));
		            bbs.setIns_Date_Time(rs.getString("Ins_Date_Time"));
		            list.add(bbs);
		        }
		    } catch(Exception e) {
		        e.printStackTrace();
		    }
		    return list;
		}

}
