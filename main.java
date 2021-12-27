import java.io.FileNotFoundException;
import java.sql.*;
import java.io.File;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

// change to get ATHINA to update

public class main {

    private static void delEmp(final String creds, final String empNo) {
        // have to make sure database connection can be established
        try (
            // create connection to database
            Connection conn = DriverManager.getConnection(creds);

            // create a statement object
            Statement stmt = conn.createStatement()
        ) {
            // query to check if employee with given empNo exists
            final String query = "SELECT * FROM employees.employees WHERE emp_no=" + empNo;

            // create prepared statement that will be used to check if an employee with the given empNo exists
            final PreparedStatement prepSt = conn.prepareStatement(query);
            ResultSet rset = prepSt.executeQuery();
            final boolean exists = rset.next();

            // will only delete if the employee exists
            if (exists) {
                // statements to delete employee with empNo from all tables
                final String delEmpSal  = "DELETE FROM employees.salaries WHERE emp_no=" + empNo;
                final String delEmpEmp  = "DELETE FROM employees.employees WHERE emp_no=" + empNo;
                final String delDeptMan = "DELETE FROM employees.dept_manager WHERE emp_no=" + empNo;
                final String delDeptEmp = "DELETE FROM employees.dept_emp WHERE emp_no=" + empNo;

                // statements to get the employee first/last name that is getting deleted
                final String getFirst = "SELECT first_name FROM employees.employees WHERE emp_no=" + empNo;
                final String getLast  = "SELECT last_name FROM employees.employees WHERE emp_no=" + empNo;

                // set ResultSet to firstName
                rset = stmt.executeQuery(getFirst);

                // get the first name out of the results
                rset.next();
                final String firstName = rset.getString("first_name");

                // set ResultSet to lastName
                rset = stmt.executeQuery(getLast);

                // get the last name out of results
                rset.next();
                final String lastName = rset.getString("last_name");

                // run the delete statements on the database
                stmt.executeUpdate(delEmpSal);
                stmt.executeUpdate(delEmpEmp);
                stmt.executeUpdate(delDeptMan);
                stmt.executeUpdate(delDeptEmp);

                // print name of deleted employee
                System.out.println("Employee " + firstName + " " + lastName + " deleted!");
            }

            // if record with empNo was not found warn the user and go back to main()
            else {
                System.out.println("Employee with id " + empNo + " does not exist.");
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void salarySum(final String creds) {
        try (
            // establish a connection to the database with the given credentials stored in the "creds" String
            Connection conn = DriverManager.getConnection(creds);

            // create statement object
            Statement stmt = conn.createStatement()
        ) {
            // set mysql query
            final String strSelect = "SELECT SUM(salaries.salary) FROM salaries salaries WHERE to_date=\"9999-01-01\"";

            // make a ResultSet object
            final ResultSet rset = stmt.executeQuery(strSelect);

            // get next rset result
            rset.next();
            final long salSum = rset.getLong("SUM(salaries.salary)"); // retrieve a 'String'-cell in the row
            System.out.println("$" + salSum);

        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void showDept(final String creds, final String dept) {
        try (
            Connection conn = DriverManager.getConnection(creds);

            Statement stmt = conn.createStatement()
        ) {
            final String strSelect = "select employees.first_name, employees.last_name, employees.emp_no from departments "
                                   + "join dept_emp on departments.dept_no=dept_emp.dept_no "
                                   + "join employees.employees on dept_emp.emp_no=employees.employees.emp_no "
                                   + "where dept_name=\"" + dept + "\"";

            //System.out.println("The SQL statement is: " + strSelect + "\n"); // Echo For debugging

            final ResultSet rset = stmt.executeQuery(strSelect);

            while(rset.next()) {
                final int    empNo     = rset.getInt("emp_no");
                final String firstName = rset.getString("first_name");
                final String lastName  = rset.getString("last_name");
                System.out.printf("%s, %20s, %20s\n", empNo, firstName, lastName);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void addEmp(final String creds, final String[] info) {
        try (
                Connection conn = DriverManager.getConnection(creds);
                Statement stmt = conn.createStatement()
        ) {
            final String idSelect = "SELECT MAX(emp_no) FROM employees";
            ResultSet rset = stmt.executeQuery(idSelect);
            rset.next();
            int empNo = rset.getInt("MAX(emp_no)");

            //System.out.println(empNo);

            empNo++;

            //System.out.println(empNo);

            //System.out.println(info[2]);

            final String deptIDSelect = "SELECT dept_no FROM employees.departments WHERE dept_name=\"" + info[2] + "\"";
            rset = stmt.executeQuery(deptIDSelect);
            rset.next();
            final String deptNo = rset.getString("dept_no");

            //System.out.println(deptNo);

            final Calendar calendar = Calendar.getInstance();
            final Date startDate = new Date(calendar.getTime().getTime());

            final String insertEmp  = "INSERT INTO employees(emp_no, first_name, last_name, gender, birth_date, hire_date) "
                              + "VALUES(" + empNo + ",?,?,?,?,?);";

            final String insertSal  = "INSERT INTO salaries(emp_no, salary, from_date, to_date) "
                              + "VALUES(" + empNo + ",?,?, '9999-01-01');";

            final String insertDept = "INSERT INTO dept_emp(emp_no, dept_no, from_date, to_date) "
                              + "VALUES(" + empNo + ",'" + deptNo + "',?, '9999-01-01');";

            final PreparedStatement prepStmtEmp = conn.prepareStatement(insertEmp);

            // insert into strSelect for employees table
            prepStmtEmp.setString(1, info[0]); // first_name
            prepStmtEmp.setString(2, info[1]); // last_name
            prepStmtEmp.setString(3, info[4]); // gender
            prepStmtEmp.setString(4, info[3]); // birth_date
            prepStmtEmp.setDate(5, startDate); // hire date

            System.out.println(prepStmtEmp);

            prepStmtEmp.executeUpdate();



            final PreparedStatement prepStmtSal = conn.prepareStatement(insertSal);

            // insert into strSelect for salaries table
            prepStmtSal.setInt(1, Integer.parseInt(info[5])); // salary
            prepStmtSal.setDate(2, startDate); // from_date

            prepStmtSal.executeUpdate();


            final PreparedStatement prepStmtDept = conn.prepareStatement(insertDept);

            // insert into strSelect for dept_emp table
            prepStmtDept.setDate(1, startDate); // from_date

            prepStmtDept.executeUpdate();



            for (int i = 0; i < info.length; i++) {
                System.out.println("" + i + " " + info[i]);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(final String[] args) {
        // create "creds" string and fill it with the credentials given withing "credentials.txt" assumed to be
        // in the same directory as this java script
        String creds = "empty";

        // must surround in try-catch or IDE complains about not handling error when "credendials.txt" cannot
        // be found
        try {
            final File credentialsTXT = new File("credentials.txt");
            final Scanner scan = new Scanner(credentialsTXT);
            creds = scan.nextLine();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        if (args[0].equals("add")) {
            //System.out.println("call add function");

            if(args.length < 8){
                System.out.println("Not enough information for a valid employee!");
                return;
            } else {
                System.out.println("Employee " + args[2] + " " + args[3] + " added!");
            }

            String[] employeeInfo = new String[6];

            employeeInfo[0] = args[2]; // first_name
            employeeInfo[1] = args[3]; // last_name

            // if the dept_name is 2 words it needs some special handling because the space breaks up the name
            // into 2 tokens
            if(args[4].equals("Customer") || args[4].equals("Human") || args[4].equals("Quality")) {
                employeeInfo[2] = args[4] + " " + args[5];
                employeeInfo[3] = args[6];
                employeeInfo[4] = args[7];
                employeeInfo[5] = args[8];
            } else {
                employeeInfo[2] = args[4];
                employeeInfo[3] = args[5];
                employeeInfo[4] = args[6];
                employeeInfo[5] = args[7];
            }

            addEmp(creds, employeeInfo);
        }

        else if (args[0].equals("delete")) {
            //System.out.println("call delete function");
            //System.out.println(args[2]);

            // argument 2 is the employee number being deleted
            delEmp(creds, args[2]);
        }

        else if (args[0].equals("show") && args[1].equals("salaries")) {
            //System.out.println("call show salaries function");

            // will print out total of all salaries currently being paid
            salarySum(creds);
        }

        else if (args[0].equals("show") && args[1].equals("employees")) {
            //System.out.println("call show dept function");

            String deptName;

            switch(args[3].toLowerCase()){
                case "finance":
                    deptName = "Finance";
                    showDept(creds, deptName);
                    break;
                case "'customer":
                    if(args[4].equalsIgnoreCase("service'")) {
                        deptName = "Customer Service";
                        showDept(creds, deptName);
                    }
                    break;
                case "development":
                    deptName = "Development";
                    showDept(creds, deptName);
                    break;
                case "'human":
                    if(args[4].equalsIgnoreCase("resources'")) {
                        deptName = "Human Resources";
                        showDept(creds, deptName);
                    }
                    break;
                case "marketing":
                    deptName = "Marketing";
                    showDept(creds, deptName);
                    break;
                case "production":
                    deptName = "Production";
                    showDept(creds, deptName);
                    break;
                case "'quality":
                    if(args[4].equalsIgnoreCase("management'")) {
                        deptName = "Quality Management";
                        showDept(creds, deptName);
                    }
                    break;
                case "research":
                    deptName = "Research";
                    showDept(creds, deptName);
                    break;
                case "sales":
                    deptName = "Sales";
                    showDept(creds, deptName);
                    break;
            }
        }

        else {
            System.out.println("Syntax error.");
        }
    }
}
