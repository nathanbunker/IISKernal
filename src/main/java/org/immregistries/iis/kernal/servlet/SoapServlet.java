package org.immregistries.iis.kernal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.immregistries.mqe.hl7util.parser.HL7Reader;
import org.immregistries.iis.kernal.logic.IncomingMessageHandler;
import org.immregistries.iis.kernal.model.OrgAccess;
import org.immregistries.iis.kernal.model.OrgMaster;
import org.immregistries.smm.cdc.CDCWSDLServer;
import org.immregistries.smm.cdc.Fault;
import org.immregistries.smm.cdc.MessageTooLargeFault;
import org.immregistries.smm.cdc.ProcessorFactory;
import org.immregistries.smm.cdc.SecurityFault;
import org.immregistries.smm.cdc.SubmitSingleMessage;
import org.immregistries.smm.cdc.UnknownFault;

@SuppressWarnings("serial")
public class SoapServlet extends HttpServlet {

  public static final String PARAM_MESSAGE = "MESSAGEDATA";
  public static final String PARAM_USERID = "USERID";
  public static final String PARAM_PASSWORD = "PASSWORD";
  public static final String PARAM_FACILITYID = "FACILITYID";

  private static SessionFactory factory;

  public static Session getDataSession() {
    if (factory == null) {
      factory = new AnnotationConfiguration().configure().buildSessionFactory();
    }
    return factory.openSession();
  }

  private static final String EXAMPLE_HL7 =
      "MSH|^~\\&|Test EHR Application|X68||NIST Test Iz Reg|20120701082240-0500||VXU^V04^VXU_V04|NIST-IZ-001.00|P|2.5.1|||ER|AL|||||Z22^CDCPHINVS\n"
          + "PID|1||D26376273^^^NIST MPI^MR||Snow^Madelynn^Ainsley^^^^L|Lam^Morgan^^^^^M|20070706|F||2076-8^Native Hawaiian or Other Pacific Islander^CDCREC|32 Prescott Street Ave^^Warwick^MA^02452^USA^L||^PRN^PH^^^657^5558563|||||||||2186-5^non Hispanic or Latino^CDCREC\n"
          + "PD1|||||||||||02^Reminder/Recall - any method^HL70215|||||A|20120701|20120701\n"
          + "NK1|1|Lam^Morgan^^^^^L|MTH^Mother^HL70063|32 Prescott Street Ave^^Warwick^MA^02452^USA^L|^PRN^PH^^^657^5558563\n"
          + "ORC|RE||IZ-783274^NDA|||||||I-23432^Burden^Donna^A^^^^^NIST-AA-1^^^^PRN||57422^RADON^NICHOLAS^^^^^^NIST-AA-1^L^^^MD\n"
          + "RXA|0|1|20120814||33332-0010-01^Influenza, seasonal, injectable, preservative free^NDC|0.5|mL^MilliLiter [SI Volume Units]^UCUM||00^New immunization record^NIP001|7832-1^Lemon^Mike^A^^^^^NIST-AA-1^^^^PRN|^^^X68||||Z0860BB|20121104|CSL^CSL Behring^MVX|||CP|A\n"
          + "RXR|C28161^Intramuscular^NCIT|LD^Left Arm^HL70163\n"
          + "OBX|1|CE|64994-7^Vaccine funding program eligibility category^LN|1|V05^VFC eligible - Federally Qualified Health Center Patient (under-insured)^HL70064||||||F|||20120701|||VXC40^Eligibility captured at the immunization level^CDCPHINVS\n"
          + "OBX|2|CE|30956-7^vaccine type^LN|2|88^Influenza, unspecified formulation^CVX||||||F\n"
          + "OBX|3|TS|29768-9^Date vaccine information statement published^LN|2|20120702||||||F\n"
          + "OBX|4|TS|29769-7^Date vaccine information statement presented^LN|2|20120814||||||F\n";

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String path = req.getPathInfo();
    final String processorName =
        path == null ? "" : (path.startsWith("/") ? path.substring(1) : path);
    try {
      CDCWSDLServer server = new CDCWSDLServer() {
        @Override
        public void process(SubmitSingleMessage ssm, PrintWriter out) throws Fault {

          String message = ssm.getHl7Message();
          String userId = ssm.getUsername();
          String password = ssm.getPassword();
          String facilityId = ssm.getFacilityID();
          String ack = "";
          Session dataSession = getDataSession();
          try {
            OrgAccess orgAccess = authenticateOrgAccess(userId, password, facilityId, dataSession);
            if (orgAccess == null) {
              throw new SecurityException("Username/password combination is unrecognized");
            } else {
              IncomingMessageHandler handler = new IncomingMessageHandler(dataSession);
              ack = handler.process(message, orgAccess);
            }
          } catch (Exception e) {
            throw new UnknownFault("Unable to process request: " + e.getMessage(), e);
          } finally {
            dataSession.close();
          }
          out.print(ack);
        }

        @Override
        public String getEchoBackMessage(String message) {
          return "End-point is ready. Echoing: " + message + "";
        }

        @Override
        public void authorize(SubmitSingleMessage ssm) throws SecurityFault {
          String userId = ssm.getUsername();
          String password = ssm.getPassword();
          String facilityId = ssm.getFacilityID();
          Session dataSession = getDataSession();
          try {
            OrgAccess orgAccess = authenticateOrgAccess(userId, password, facilityId, dataSession);
            if (orgAccess == null) {
              throw new SecurityFault("Username/password combination is unrecognized");
            }
          } finally {
            dataSession.close();
          }
        }
      };
      server.setProcessorName(processorName);
      server.process(req, resp);
    } finally {
    }



  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String wsdl = req.getParameter("wsdl");
    if (wsdl != null) {
      resp.setContentType("text/xml");
      PrintWriter out = new PrintWriter(resp.getOutputStream());
      CDCWSDLServer.printWSDL(out, "http://localhost:8282/wsdl-demo");
      out.close();
    } else {
      resp.setContentType("text/html;charset=UTF-8");

      PrintWriter out = resp.getWriter();
      try {
        out.println("<h1>IIS Kernel - CDC SOAP Endpoint</h1>");
        out.println("<p>");
        out.println("This demonstration system supports the use of the ");
        out.println(
            "<a href=\"http://www.cdc.gov/vaccines/programs/iis/technical-guidance/soap/wsdl.html\">CDC ");
        out.println("WSDL</a>");
        out.println(" which has been defined to support the transport of HL7 messages ");
        out.println("sent to Immunization Information Systems (IIS).  ");
        out.println("</p>");
        out.println("<h2>Usage Instructions</h2>");
        out.println("<h3>WSDL</h3>");
        out.println("<p><a href=\"soap?wsdl=true\">See WSDL</a></p>");
      } finally {
        out.close();
      }
    }
  }

  @SuppressWarnings("unchecked")
  public OrgAccess authenticateOrgAccess(String userId, String password, String facilityId,
      Session dataSession) {
    OrgMaster orgMaster = null;
    OrgAccess orgAccess = null;
    {
      Query query = dataSession.createQuery("from OrgMaster where organizationName = ?");
      query.setParameter(0, facilityId);
      List<OrgMaster> orgMasterList = query.list();
      if (orgMasterList.size() > 0) {
        orgMaster = orgMasterList.get(0);
      } else {
        orgMaster = new OrgMaster();
        orgMaster.setOrganizationName(facilityId);
        orgAccess = new OrgAccess();
        orgAccess.setOrg(orgMaster);
        orgAccess.setAccessName(userId);
        orgAccess.setAccessKey(password);
        Transaction transaction = dataSession.beginTransaction();
        dataSession.save(orgMaster);
        dataSession.save(orgAccess);
        transaction.commit();
      }

    }
    if (orgAccess == null) {
      Query query = dataSession
          .createQuery("from OrgAccess where accessName = ? and accessKey = ? and org = ?");
      query.setParameter(0, userId);
      query.setParameter(1, password);
      query.setParameter(2, orgMaster);
      List<OrgAccess> orgAccessList = query.list();
      if (orgAccessList.size() != 0) {
        orgAccess = orgAccessList.get(0);
      }
    }
    return orgAccess;
  }
}
