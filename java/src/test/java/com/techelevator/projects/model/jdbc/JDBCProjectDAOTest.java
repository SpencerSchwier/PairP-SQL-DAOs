package com.techelevator.projects.model.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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


public class JDBCProjectDAOTest {

	private static final String INACTIVE_PROJECT_NAME = "Test Project Inactive";
	private static final LocalDate INACTIVE_PROJECT_FROM_DATE = LocalDate.parse("1900-01-01");
	private static final LocalDate INACTIVE_PROJECT_TO_DATE = LocalDate.parse("2000-01-01");
	
	private static final String ACTIVE_PROJECT_NAME = "Test Project Active";
	private static final LocalDate ACTIVE_PROJECT_FROM_DATE = LocalDate.parse("2010-01-01");
	private static final LocalDate ACTIVE_PROJECT_TO_DATE = LocalDate.parse("2020-01-01");
		
	private static final String TEST_EMPLOYEE_FIRST_NAME = "test_first_name";
	private static final String TEST_EMPLOYEE_LAST_NAME = "test_last_name";
	private static final LocalDate TEST_BIRTH_DATE = LocalDate.parse("2050-01-01");
	private static final LocalDate TEST_HIRE_DATE = LocalDate.parse("2005-01-01");
	private static final char TEST_GENDER = 'M';
	
	private static final String TEST_DEPARTMENT_NAME = "Test Department";

	
	private static SingleConnectionDataSource dataSource;
	
	private JdbcTemplate template;
	private JDBCProjectDAO dao;
	
	private Project testProjectActive;
	private Department testDepartment;
	private Employee testEmployee;
	
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
		String sql= "DELETE FROM project_employee;" +
					"DELETE FROM project;" +
					"DELETE FROM employee;" + 
					"DELETE FROM department";
		template.update(sql);
		
		this.createAndStoreProject(INACTIVE_PROJECT_NAME, INACTIVE_PROJECT_FROM_DATE, INACTIVE_PROJECT_TO_DATE);
		this.testProjectActive = this.createAndStoreProject(ACTIVE_PROJECT_NAME, ACTIVE_PROJECT_FROM_DATE, ACTIVE_PROJECT_TO_DATE);
		this.testDepartment = this.createAndStoreDepartment();
		this.testEmployee = this.createAndStoreEmployee(testDepartment.getId());
		
		
		dao = new JDBCProjectDAO(dataSource);
	}
	
	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}
	
	@Test
	public void gets_all_active_projects() {
		List<Project> activeProjects = dao.getAllActiveProjects();
		
		
		assertEquals(activeProjects.size(), 1);
		Project p = activeProjects.get(0);
		
		assertEquals(p.getId(), testProjectActive.getId());
		assertEquals(p.getName(), testProjectActive.getName());
		assertEquals(p.getStartDate(), testProjectActive.getStartDate());
		assertEquals(p.getEndDate(), testProjectActive.getEndDate());
	}
	
	@Test
	public void employee_can_be_removed_from_project() {
		String sql = "INSERT INTO project_employee (project_id, employee_id) " +
					 "VALUES (?, ?)";
		
		template.update(sql, testProjectActive.getId(), testEmployee.getId());
		
		dao.removeEmployeeFromProject(testProjectActive.getId(), testEmployee.getId());
		
		sql = "SELECT employee_id " + 
			  "FROM project_employee " +
			  "WHERE employee_id = ? AND project_id = ?";
		SqlRowSet results = template.queryForRowSet(sql, testEmployee.getId(), testProjectActive.getId());
		
		assertFalse(results.next());
	}
	
	@Test
	public void employee_can_be_added_to_project() {
		dao.addEmployeeToProject(testProjectActive.getId(), testEmployee.getId());
		
		String sql = "SELECT employee_id " + 
					 "FROM project_employee " +
					 "WHERE employee_id = ? AND project_id = ?";
		SqlRowSet results = template.queryForRowSet(sql, testEmployee.getId(), testProjectActive.getId());
		
		assertTrue(results.next());
		assertTrue(results.isLast());
		assertEquals(results.getLong("employee_id"), testEmployee.getId().longValue());		
	}
	
	private Project createAndStoreProject(String name, LocalDate fromDate, LocalDate toDate) {
		Project newProject = new Project();
		newProject.setName(name);
		newProject.setStartDate(fromDate);
		newProject.setEndDate(toDate);
		
		String sql = "INSERT INTO project (name, from_date, to_date) " + 
				 "VALUES (?,?,?) RETURNING project_id";
		
		SqlRowSet results = template.queryForRowSet(sql, newProject.getName(), 
				newProject.getStartDate(), 
				newProject.getEndDate());
		if (results.next()) {
			newProject.setId(results.getLong("project_id"));
		}
		
		return newProject;
	}
	
	private Department createAndStoreDepartment() {
		Department newDepartment = new Department();
		newDepartment.setName(TEST_DEPARTMENT_NAME);
		
		String sql = "INSERT INTO department (name) " + 
				 "VALUES (?) RETURNING department_id";
		
		SqlRowSet results = template.queryForRowSet(sql, newDepartment.getName());
		
		if (results.next()) {
			newDepartment.setId(results.getLong("department_id"));
		}
		
		return newDepartment;
	}
	
	private Employee createAndStoreEmployee(Long departmentId) {
		Employee newEmployee = new Employee();
		newEmployee.setFirstName(TEST_EMPLOYEE_FIRST_NAME);
		newEmployee.setLastName(TEST_EMPLOYEE_LAST_NAME);
		newEmployee.setBirthDay(TEST_BIRTH_DATE);
		newEmployee.setHireDate(TEST_HIRE_DATE);
		newEmployee.setGender(TEST_GENDER);
		newEmployee.setDepartmentId(departmentId);
		
		String sql = "INSERT INTO employee (first_name, last_name, birth_date, hire_date, gender, department_id) " + 
				 "VALUES (?, ?, ?, ?, ?, ?) RETURNING employee_id";
		
		SqlRowSet results = template.queryForRowSet(sql, 
				newEmployee.getFirstName(),
				newEmployee.getLastName(),
				newEmployee.getBirthDay(),
				newEmployee.getHireDate(),
				newEmployee.getGender(),
				newEmployee.getDepartmentId());
		if(results.next()) {
			newEmployee.setId(results.getLong("employee_id"));
		}
		
		return newEmployee;
	}
}
