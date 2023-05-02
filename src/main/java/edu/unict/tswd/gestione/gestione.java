package edu.unict.tswd.gestione;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.io.PrintWriter;

import java.sql.*;
import java.util.Map;

public class gestione extends HttpServlet {
    Connection dbConnection = null;

    public void init() {
        try {
            // establish connection to database
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/flist", "root", "1000005206");
            System.out.println("Connected:" + dbConnection.toString());
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        } // end catch
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Statement aStatement = null;
        ResultSet resultSet = null;
        String TitoloRandom = "";
        String RegistaRandom = "";
        String query = "";

        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            System.out.println("Exception" + e);
        }

        try {
            aStatement = this.dbConnection.createStatement();
            query = "select titolo,regista from flist ORDER BY RAND() LIMIT 1";
            resultSet = aStatement.executeQuery(query);
            resultSet.next();
            TitoloRandom = resultSet.getString("titolo");
            RegistaRandom = resultSet.getString("regista");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        out.write("<html>");
        out.write("<head><title></title></head>");
        out.write("<body>");

        if (!TitoloRandom.equals("")) {
            out.write("<h1>Il film consigliato </h1>");
            out.write("Titolo" + " " + TitoloRandom + " " + RegistaRandom);
        }

        out.write("<form method=\"POST\" , action=\"/sendData\">" +
                "<input type\"text\" name=\"titolo\" placeholder=\"Titolo\" >" +
                "<input type\"text\" name=\"regista\" placeholder=\"Regista\" >" +
                "<input type=\"submit\" name=\"action\" value=\"Cerca\">" +
                "</form>");
        out.write("</body></html>");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PreparedStatement aStatement1 = null;
        PreparedStatement aStatement2 = null;
        Statement normalQuery = null;
        ResultSet resultSet = null;
        String query = "";
        String query2 = "";
        String titolo = "";
        String regista = "";
        String action = "";
        boolean ok;

        response.setContentType("text/html");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            System.out.println("Exception" + e);
        }

        titolo = request.getParameter("titolo");
        regista = request.getParameter("regista");
        action = request.getParameter("action");


        switch (action) {

            case "Cerca": {
                if (titolo.equals("") && regista.equals("")) {

                    out.write("<h1> Inserire almeno un criterio di ricerca </h1>");
                    break;
                }
                try {
                    if (!titolo.equals("") && regista.equals("")) {

                        query = "select titolo,regista from flist where titolo=?";
                        aStatement1 = this.dbConnection.prepareStatement(query);
                        aStatement1.setString(1, titolo);

                        query2 = "select titolo,regista from wlist where titolo=?";
                        aStatement2 = this.dbConnection.prepareStatement(query2);
                        aStatement2.setString(1, titolo);
                    } else if (titolo.equals("") && !regista.equals("")) {

                        query = "select titolo,regista from flist where regista=? ";
                        aStatement1 = this.dbConnection.prepareStatement(query);
                        aStatement1.setString(1, regista);

                        query2 = "select titolo,regista from wlist where regista=? ";
                        aStatement2 = this.dbConnection.prepareStatement(query2);
                        aStatement2.setString(1, regista);
                    } else {

                        query = "select titolo,regista from flist where titolo=? AND regista=?";
                        aStatement1 = this.dbConnection.prepareStatement(query);
                        aStatement1.setString(1, titolo);
                        aStatement1.setString(2, regista);

                        query2 = "select titolo,regista from wlist where titolo=? AND regista=?";
                        aStatement2 = this.dbConnection.prepareStatement(query2);
                        aStatement2.setString(1, titolo);
                        aStatement2.setString(2, regista);

                    }
                    resultSet = aStatement1.executeQuery();
                    ok = true;
                    String eliminaTitolo ="";
                    String eliminaRegista ="";
                    while (resultSet.next()) {
                        if (ok) {
                            out.write("Il film è presente");
                            ok = false;
                            eliminaTitolo =resultSet.getString("titolo");
                            eliminaRegista =resultSet.getString("regista");
                            out.write("<br>");
                            System.out.println("Elimina 1" +eliminaTitolo);
                            out.write("<form method=\"POST\" action=\"/sendData\" >"+
                            "<input type=\"hidden\" name=\"titolo\" value=\"" +eliminaTitolo +"\">" +
                            "<input type=\"hidden\" name=\"regista\" value=\"" +eliminaRegista +"\">" +
                            "<input type=\"submit\" name=\"action\" value=\"Elimina\">" +
                            "</from>");
                        }
                        out.write(
                                "Titolo" + " " + resultSet.getString("titolo"));
                               

                    }
                    
                    resultSet.close();

                    if (ok) {
                        System.out.println("Ciao2");
                        resultSet = aStatement2.executeQuery();
                        while (resultSet.next()) {
                            System.out.println("Ciao3");
                            if (ok) {
                                out.write("Il film è presente nella wish list");
                                ok = false;
                            }
                            out.write("Titolo" + " " + resultSet.getString("titolo"));
                        }
                        aStatement1.close();
                        aStatement2.close();
                        if (ok) {
                            out.write("Il film non è presente nella wish list lo vuoi aggiungere");
                            out.write("<form method=\"POST\" action=\"/sendData\">" +
                                    "<input type=\"hidden\" name=\"titolo\" value=\"" + titolo + "\">" +
                                    "<input type=\"hidden\" name=\"regista\" value=\"" + regista + "\">" +
                                    "<input type=\"submit\" name=\"action\" value=\"Si\">" +
                                    "<input type=\"submit\" name=\"action\" value=\"No\">" +
                                    "</form>");
                        }
                    } else {
                        out.write("<br><br><a href=\"/sendData\" >Torna alla home </a>");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            }


            case "Elimina":{
                try{
                    System.out.println("ok" +titolo);
                    if(!titolo.equals("")){
                        query="DELETE FROM flist WHERE titolo= '"+titolo+"' ";
                        normalQuery=this.dbConnection.createStatement();
                        normalQuery.executeUpdate(query);
                        System.out.println("Sono stato qui");
                        
                    }else if(!titolo.equals("") && !regista.equals("")){
                        query="DELETE FROM flist WHERE titolo= '"+titolo+"' AND regista= '"+regista+"' ";
                        normalQuery=this.dbConnection.createStatement();
                        normalQuery.executeUpdate(query);
                        System.out.println("Sono stato qui");
                        
                    }
                    out.write("<h1> E' stato eliminato " + " " + titolo + " " + "del regista" + " " + regista + "</h1>");
                    out.write("<br><br><a href=\"/sendData\" >Torna alla home </a>");
                }catch(SQLException e){
                    e.printStackTrace();
                }

                break;
            }

            case "Si" :{
                try{
                    System.out.println("ok" +titolo);
                    if(!titolo.equals("") && regista.equals("")){
                        query=" inser into wlist(titolo) values(?) ";
                        aStatement1=this.dbConnection.prepareStatement(query);
                        aStatement1.setString(1,titolo);
                        aStatement1.executeUpdate();
                    }else if(!titolo.equals("") && !regista.equals("")){
                        query=" insert into wlist(titolo,regista) values (?,?) ";
                        aStatement1=this.dbConnection.prepareStatement(query);
                        aStatement1.setString(1,titolo);
                        aStatement1.setString(2,regista);
                        aStatement1.executeUpdate();
                    }
                    out.write("<h1> E stato aggiunto" + " " + titolo + " "+"del regosta" + " " + regista + "</h1>");
                    out.write("<br><br><a href=\"/sendData\">Torna alla home</a>");

                }catch(SQLException e){
                    e.printStackTrace();
                }
            }

        }
    }

    public void destory(){
        try{
            dbConnection.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}
