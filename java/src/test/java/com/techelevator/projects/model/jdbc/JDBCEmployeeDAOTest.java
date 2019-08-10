package com.techelevator.projects.model.jdbc;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.Employee;
import com.techelevator.projects.model.Project;

public class JDBCEmployeeDAOTest {

	private static final String TEST_PROJECT_NAME = "Test Project";
	private static final LocalDate TEST_PROJECT_FROM_DATE = LocalDate.parse("2010-01-01");
	private static final LocalDate TEST_PROJECT_TO_DATE = LocalDate.parse("2020-01-01");

	private static final String EMPLOYEE_FIRST_NAME_1 = "test_first_name_1";
	private static final String EMPLOYEE_LAST_NAME_1 = "test_last_name_1";
	private static final LocalDate BIRTH_DATE_1 = LocalDate.parse("2050-01-01");
	private static final LocalDate HIRE_DATE_1 = LocalDate.parse("2005-01-01");
	private static final char GENDER_1 = 'M';

	private static final String EMPLOYEE_FIRST_NAME_2 = "test_first_name_2";
	private static final String EMPLOYEE_LAST_NAME_2 = "test_last_name_2";
	private static final LocalDate BIRTH_DATE_2 = LocalDate.parse("2065-01-01");
	private static final LocalDate HIRE_DATE_2 = LocalDate.parse("2010-01-01");
	private static final char GENDER_2 = 'F';

	private static final String DEPARTMENT_NAME_1 = "Test Department 1";
	private static final String DEPARTMENT_NAME_2 = "Test Department 2";

	private static SingleConnectionDataSource dataSource;

	private JdbcTemplate template;
	private JDBCEmployeeDAO dao;

	private Project testProject;
	private Department testDepartment1, testDepartment2;
	private Employee testEmployee1, testEmployee2;

	@BeforeClass
	public static void setupDataSource() {
		dataSource = new SingleConnectionDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5432/projects");
		dataSource.setUsername("postgres");
		dataSource.setAutoCommit(false);
	}

	@AfterClass
	public static void closeDataSource() {
		dataSource.destroy();
	}

	@Before
	public void setup() {

		template = new JdbcTemplate(dataSource);
		String sql = "DELETE FROM project_employee;" + "DELETE FROM project;" + "DELETE FROM employee;"
				+ "DELETE FROM department";
		template.update(sql);

		testProject = this.createAndStoreProject();
		testDepartment1 = this.createAndStoreDepartment(DEPARTMENT_NAME_1);
		testDepartment2 = this.createAndStoreDepartment(DEPARTMENT_NAME_2);
		testEmployee1 = this.createAndStoreEmployee(EMPLOYEE_FIRST_NAME_1, EMPLOYEE_LAST_NAME_1, BIRTH_DATE_1,
				HIRE_DATE_1, GENDER_1, testDepartment1.getId());
		testEmployee2 = this.createAndStoreEmployee(EMPLOYEE_FIRST_NAME_2, EMPLOYEE_LAST_NAME_2, BIRTH_DATE_2,
				HIRE_DATE_2, GENDER_2, testDepartment1.getId());

		dao = new JDBCEmployeeDAO(dataSource);

	}

	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}

	@Test
	public void gets_all_employees_in_table() {
		List<Employee> employees = dao.getAllEmployees();

		assertEquals(employees.size(), 2);

		Employee e1 = employees.get(0);
		Employee e2 = employees.get(1);

		if (e1.getId().equals(testEmployee1.getId())) {
			this.assertAreSameEmployee(e1, testEmployee1);
			this.assertAreSameEmployee(e2, testEmployee2);
		} else {
			this.assertAreSameEmployee(e1, testEmployee2);
			this.assertAreSameEmployee(e2, testEmployee1);
		}
	}

	@Test
	public void finds_employee_by_first_and_last_name() {
		List<Employee> employees = dao.searchEmployeesByName(EMPLOYEE_FIRST_NAME_1, EMPLOYEE_LAST_NAME_1);

		assertEquals(employees.size(), 1);
		this.assertAreSameEmployee(employees.get(0), testEmployee1);

	}

	@Test
	public void finds_employee_by_department_id() {
		List<Employee> employees = dao.getEmployeesByDepartmentId(testDepartment1.getId());

		assertEquals(employees.size(), 2);
		Employee e1 = employees.get(0);
		Employee e2 = employees.get(1);
		if(e1.getId().equals(testEmployee1.getId())) {
			this.assertAreSameEmployee(e1, testEmployee1);
			this.assertAreSameEmployee(e2, testEmployee2);
		} else {
			this.assertAreSameEmployee(e1, testEmployee2);
			this.assertAreSameEmployee(e2, testEmployee1);
		}
	}

	@Test
	public void finds_employees_that_dont_have_projects() {
		String sql = "INSERT INTO project_employee (project_id, employee_id) " + "VALUES (?, ?)";
		template.update(sql, testProject.getId(), testEmployee1.getId());

		List<Employee> employees = dao.getEmployeesWithoutProjects();

		assertEquals(employees.size(), 1);
		this.assertAreSameEmployee(employees.get(0), testEmployee2);
	}

	@Test
	public void finds_employees_by_project_id() {
		String sql = "INSERT INTO project_employee (project_id, employee_id) " + "VALUES (?, ?)";
		template.update(sql, testProject.getId(), testEmployee1.getId());

		sql = "INSERT INTO project_employee (project_id, employee_id) " + "VALUES (?, ?)";
		template.update(sql, testProject.getId(), testEmployee2.getId());

		List<Employee> employees = dao.getEmployeesByProjectId(testProject.getId());

		assertEquals(employees.size(), 2);
		Employee e1 = employees.get(0);
		Employee e2 = employees.get(1);

		if (e1.getId() == testEmployee1.getId()) {
			this.assertAreSameEmployee(e1, testEmployee1);
		} else {
			this.assertAreSameEmployee(e2, testEmployee2);
		}
	}

	@Test
	public void change_employee_department_id() {
		dao.changeEmployeeDepartment(testEmployee1.getId(), testDepartment2.getId());

		List<Employee> employees = dao.getEmployeesByDepartmentId(testDepartment2.getId());

		assertEquals(employees.size(), 1);
	}

	private void assertAreSameEmployee(Employee e1, Employee e2) {
		assertEquals(e1.getFirstName(), e2.getFirstName());
		assertEquals(e1.getLastName(), e2.getLastName());
		assertEquals(e1.getBirthDay(), e2.getBirthDay());
		assertEquals(e1.getHireDate(), e2.getHireDate());
		assertEquals(e1.getGender(), e2.getGender());
		assertEquals(e1.getDepartmentId(), e2.getDepartmentId());

	}

	private Project createAndStoreProject() {
		Project newProject = new Project();
		newProject.setName(TEST_PROJECT_NAME);
		newProject.setStartDate(TEST_PROJECT_FROM_DATE);
		newProject.setEndDate(TEST_PROJECT_TO_DATE);

		String sql = "INSERT INTO project (name, from_date, to_date) " + "VALUES (?,?,?) RETURNING project_id";

		SqlRowSet results = template.queryForRowSet(sql, newProject.getName(), newProject.getStartDate(),
				newProject.getEndDate());
		if (results.next()) {
			newProject.setId(results.getLong("project_id"));
		}

		return newProject;
	}

	private Department createAndStoreDepartment(String departmentName) {
		Department newDepartment = new Department();
		newDepartment.setName(departmentName);

		String sql = "INSERT INTO department (name) " + "VALUES (?) RETURNING department_id";

		SqlRowSet results = template.queryForRowSet(sql, newDepartment.getName());

		if (results.next()) {
			newDepartment.setId(results.getLong("department_id"));
		}

		return newDepartment;
	}

	private Employee createAndStoreEmployee(String firstName, String lastName, LocalDate birthDate, LocalDate hireDate,
			char gender, Long departmentId) {
		Employee newEmployee = new Employee();
		newEmployee.setFirstName(firstName);
		newEmployee.setLastName(lastName);
		newEmployee.setBirthDay(birthDate);
		newEmployee.setHireDate(hireDate);
		newEmployee.setGender(gender);
		newEmployee.setDepartmentId(departmentId);

		String sql = "INSERT INTO employee (first_name, last_name, birth_date, hire_date, gender, department_id) "
				+ "VALUES (?, ?, ?, ?, ?, ?) RETURNING employee_id";

		SqlRowSet results = template.queryForRowSet(sql, newEmployee.getFirstName(), newEmployee.getLastName(),
				newEmployee.getBirthDay(), newEmployee.getHireDate(), newEmployee.getGender(),
				newEmployee.getDepartmentId());
		if (results.next()) {
			newEmployee.setId(results.getLong("employee_id"));
		}

		return newEmployee;
	}
}
