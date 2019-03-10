package ru.eludia.products.mosgis.web;

import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FileSvcServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger (FileSvcServlet.class.getName ());
        
    @Override
    protected void doPut (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
logger.info (request.getPathInfo ());
        throw new ServletException ("Yes");
    }

    @Override
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException ("No");
    }
    
    @Override
    protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException ("No");        
    }

    @Override
    protected void doDelete (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new ServletException ("No");
    }

    @Override
    protected void doHead (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new ServletException ("No");
    }

    @Override
    protected void doOptions (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new ServletException ("No");
    }

    @Override
    protected void doTrace (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new ServletException ("No");
    }   

    @Override
    public String getServletInfo () {
        return "Services";
    }

}
