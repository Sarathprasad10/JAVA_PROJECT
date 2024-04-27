import java.util.*;
import java.io.*;
import java.sql.*;
class UserReg
{
    String name;
    int no;
    int userid;
    String pass;
    void read() 
    {
        Scanner s=new Scanner(System.in);
        System.out.println("Enert the user name:");
        name=s.nextLine();
        System.out.println("Enert the user phone number:");
        no=s.nextInt();
        s.nextLine();
        System.out.println("Enert the user Passworde:");
        pass= s.nextLine();
    }
}
class login
{
    String lname;
    String lpass;
    //System.out.println("LOGIN");
    void read()
    {
        Scanner s =new Scanner(System.in);
        System.out.println("enter the username");
        lname=s.nextLine();
        System.out.println("enter the passworde");
        lpass=s.nextLine();
    }   
}
class cenima
{
    Connection con;
	Statement st;
	ResultSet rs;
    PreparedStatement pstmt;
    Scanner s=new Scanner(System.in);
    public void disp(int uid)
    {   

        try
        {   
            int uid1=uid;
            Class.forName("com.mysql.jdbc.Driver");
	        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviebooking?characterEncoding=utf8","root","");
	        st = con.createStatement();
	        String str = "select * from movies";
	        rs = st.executeQuery(str);
            System.out.println("BOOK Ur' Tickets Now");
            System.out.println("Movie No        | Movie Name                     | Language");
            System.out.println("-------------------------------------------------------------");
	        while (rs.next()) 
            {
                // Format movie information into tabular format
                String movieId = String.format("%-15s", rs.getString("M_id"));
                String movieName = String.format("%-30s", rs.getString("M_name"));
                String language = rs.getString("Language");

                System.out.println(movieId + " | " + movieName + " | " + language);
            }
            System.out.println("Enter the Movie No to book your tickets");
            String ch=s.nextLine();
            TheaterSelection(ch,uid1);
        }
        catch(Exception e)
        {
            System.out.println("Error"+e);
        }
    }
    public void TheaterSelection(String Mid,int uid)// teater selection
    {
        int uid1=uid;
       try
       {    
            String qry="SELECT t.T_name,t.T_id,t.Location, s.As, s.Date, s.Time FROM theater t INNER JOIN showtime s ON t.T_id = s.T_id WHERE s.M_id = ?";
            pstmt=con.prepareStatement(qry);
            pstmt.setString(1,Mid);
            rs=pstmt.executeQuery();
            System.out.println("Theater Name              | Theater Id | Location   | Date       | Time       |Available Seats");
            System.out.println("-------------------------------------------------------------------------------------------------");
            while(rs.next())
            {
                String T_name = String.format("%-25s", rs.getString("T_name"));
                String loc = String.format("%-10s", rs.getString("Location"));
                int As = rs.getInt("As");
                String T = String.format("%-10s", rs.getString("Time"));
                String D = String.format("%-10s", rs.getString("Date"));
                String T_id = String.format("%-10s", rs.getString("T_id"));
                System.out.println(T_name + " | " + T_id + " | " + loc + " | " + D + " | " + T + " | " + As);  
            } 
            String ch;
            System.out.println("enter the Theater Id to book :");
            ch=s.nextLine();
            bookticket(ch,uid1,Mid);   
       }
       
         catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }
       
    }

public void bookticket(String ch, int uid,String Mid) //ch is teater id
{
    int Tnos = 0;
    int As = 0;
    String S_id = "";
    int uid1 = uid;
    
    try 
    {
        // Retrieve showtime information
        String qry = "SELECT s.As, s.Tnos, s.S_id FROM showtime s WHERE s.T_id = ?";
        pstmt = con.prepareStatement(qry);
        pstmt.setString(1, ch);
        rs = pstmt.executeQuery();
        
        if (rs.next()) 
        {
            As = rs.getInt("As");
            Tnos = rs.getInt("Tnos");
            S_id = rs.getString("S_id");
        }

        // Display Seat Layout
        Set<Integer> reservedSeats = new HashSet<>();
        String reservedSeatsQuery = "SELECT Rs_id FROM reservedseats WHERE T_id = ? AND S_id = ?";
        pstmt = con.prepareStatement(reservedSeatsQuery);
        pstmt.setString(1, ch);
        pstmt.setString(2, S_id);
        rs = pstmt.executeQuery();

        System.out.println("");
        System.out.println("[---------------------------------------------]       <---- Screen this Way\n");
        while (rs.next()) 
        {
            reservedSeats.add(rs.getInt("Rs_id"));
        }

        for (int i = 0; i < Tnos; i++) 
        {
            if (reservedSeats.contains(i + 1)) 
            {
                System.out.print("[X] ");
            } else 
            {
                System.out.print("[" + (i + 1) + "] ");
            }

            if ((i + 1) % 10 == 0) 
            {
                System.out.println();
            }
        }

        // Book Seats
        System.out.println("\nEnter the number of seats to book:");
        int Nostb = s.nextInt();
        s.nextLine();
        String insertQuery = "INSERT INTO reservedseats (Rs_id, T_id, S_id, uid) VALUES (?, ?, ?, ?)";
        pstmt = con.prepareStatement(insertQuery);

        for (int i = 0; i < Nostb; i++) 
        {
            System.out.println("Enter the seat number:");
            int seatNumber = s.nextInt();
            s.nextLine();

            if (seatNumber < 1 || seatNumber > Tnos) 
            {
                System.out.println("Invalid seat number. Please try again.");
                continue;
            }

            if (reservedSeats.contains(seatNumber)) 
            {
                System.out.println("Seat " + seatNumber + " is already booked. Please choose another seat.");
                continue;
            }

            pstmt.setInt(1, seatNumber);
            pstmt.setString(2, ch);
            pstmt.setString(3, S_id);
            pstmt.setInt(4, uid1);
            pstmt.executeUpdate();
            System.out.println("Seat " + seatNumber + " booked successfully!");
        }
        System.out.print("\033[H\033[2J");//CLEAR screen

        //display ticket
        String dis = "SELECT T_name FROM  theater WHERE T_id=?";
        pstmt = con.prepareStatement(dis);
        pstmt.setString(1, ch);
        rs = pstmt.executeQuery();
        if (rs.next()) 
        {
            String Tname = rs.getString("T_name");
         
        }
        String SOW = "SELECT Date, Time FROM  showtime  WHERE S_id=?";
        pstmt = con.prepareStatement(SOW);
        pstmt.setString(1, S_id);
        rs = pstmt.executeQuery();
        String date="";
        String Time="";
        if (rs.next())
        {
             date = rs.getString("Date");
             Time = rs.getString("Time");
        }
        String M="SELECT M_name FROM  Movies  WHERE M_id=?";
        pstmt = con.prepareStatement(M);
        pstmt.setString(1, Mid);
        rs = pstmt.executeQuery();
        String mname="";
         if (rs.next())
        {
             mname = rs.getString("M_name");
        }
        System.out.println("");
        System.out.println("           TICKET           ");
        System.out.println("----------------------------");
        System.out.println("Movie Name:"+mname);
        System.out.println("DATE:"+date);
        System.out.println("Time:"+Time);
        System.out.println("Number of Seats Reserved:"+Nostb);

        
        // Update available seats in the showtime table
        int updatedAvailableSeats = As - Nostb;
        String updateAsQuery = "UPDATE showtime s SET s.As = ? WHERE s.T_id = ? AND s.S_id = ?";
        pstmt = con.prepareStatement(updateAsQuery);
        pstmt.setInt(1, updatedAvailableSeats);
        pstmt.setString(2, ch);
        pstmt.setString(3, S_id);
        pstmt.executeUpdate();
        System.out.println("Available seats updated successfully!");
        
    } 
    catch (SQLException e)
    {
        System.out.println("Error while booking ticket: " + e.getMessage());
    }
    finally
    {
        // Close resources (ResultSet, Statement)
        // Handle exceptions if necessary
    }
}
}

class Movies
{
    public static void main(String args[])
    {
        Connection con;
        Statement st;
        ResultSet rs;
        PreparedStatement pstmt;
        int ch;
        Scanner s=new Scanner(System.in);
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            con=DriverManager.getConnection("jdbc:mysql://localhost:3306/moviebooking?characterEncoding=utf8","root","");
            st=con.createStatement();
            System.out.println("MENU");
            System.out.println("1.LOGIN");
            System.out.println("2.REGESTER");
            System.out.println("3.EXIT");
            System.out.println("Enter your choice:");
            ch=s.nextInt();
            s.nextLine();
            switch(ch)
            {
                case 1:System.out.println("Login");
                        login l=new login();
                        l.read();
                        String qry ="select * from UserReg where uname=?AND password=?";
                        // rs =st.executeQuery(str);
                        pstmt=con.prepareStatement(qry);
                        pstmt.setString(1,l.lname);
                       pstmt.setString(2,l.lpass);
                       rs=pstmt.executeQuery();
                       if(rs.next())
                       {
                           // System.out.print("\033[H\033[2J");  // ANSI escape codes to clear screen
                            //System.out.flush();
                            System.out.println("login Successful");
                            int uid=rs.getInt("uid");
                            System.out.println("hello "+rs.getString("uname")+" ");
                            cenima c=new cenima();
                            c.disp(uid);
                        }    
                       else
                        {
                            System.out.println("Login failed. Invalid username or password.");
                       }
                break;
                case 2:System.out.println("register");
                        UserReg ur=new UserReg();
                        ur.read();
                        //insert into movies(1,"sss",77867867)
                        String str="insert into UserReg values(";
                        str=str+ur.userid+",";
                        str=str+"'"+ur.name+"',";   
                        str=str+ur.no+",";
                        str=str+"'"+ur.pass+"')";
                        System.out.println(str);
                        st.executeUpdate(str);
                        System.out.println("one record is inserted..");
                        System.out.print("\033[H\033[2J");
                break;
                case 3:System.out.println("Exit");
                break;

            }
        
        }
        catch(Exception e)
        {
            System.out.println("error"+e);
        }
    }
}

