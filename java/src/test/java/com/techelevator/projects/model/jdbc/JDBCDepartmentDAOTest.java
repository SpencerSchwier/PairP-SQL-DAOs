package com.techelevator.projects.model.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.techelevator.projects.model.Department;

public class JDBCDepartmentDAOTest {
	private static final String DEPARTMENT_NAME_1 = "Test Department 1";
	private static final String DEPARTMENT_NAME_2 = "Test Department 2";

	
	private static SingleConnectionDataSource dataSource;
	
	private JDBCDepartmentDAO dao;
	
	private Department testDepartment1, testDepartment2;
	
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
		String sql = "DELETE FROM project_employee;" +
					 "DELETE FROM employee;" + 
					 "DELETE FROM department";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
		
		
		dao = new JDBCDepartmentDAO(dataSource);
		
		testDepartment1 = new Department();
		testDepartment1.setName(DEPARTMENT_NAME_1);
		testDepartment1 = dao.createDepartment(testDepartment1);
		
		testDepartment2 = new Department();
		testDepartment2.setName(DEPARTMENT_NAME_2);
		testDepartment2 = dao.createDepartment(testDepartment2);
	}
	
	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}
	
	
	@Test
	public void retrieves_all_departments_from_table() {
		List<Department> departments = dao.getAllDepartments();
		
		assertEquals(departments.size(), 2);
		if(departments.get(0).getId() == testDepartment1.getId()) {
			this.assertAreSameDepartment(departments.get(0), testDepartment1);
		} else {
			this.assertAreSameDepartment(departments.get(1), testDepartment2);
		}
	}
	
	@Test
	public void finds_departments_with_names_containing_search_string() {
		List<Department> departments = dao.searchDepartmentsByName(testDepartment1.getName());
		
		assertEquals(departments.size(), 1);
		
		this.assertAreSameDepartment(departments.get(0), testDepartment1);
	}
	
	@Test
	public void updates_department_to_match_object() {
		testDepartment1.setName("Brand new test deparment name");
		
		dao.saveDepartment(testDepartment1);
		
		Department testDepartment = dao.getDepartmentById(testDepartment1.getId());
		
		this.assertAreSameDepartment(testDepartment, testDepartment1);
	}
	
	@Test
	public void creates_new_department_from_object() {
		Department newDepartment = new Department();
		newDepartment.setName("Test Name");
		
		dao.createDepartment(newDepartment);
		Department testDepartment = dao.getDepartmentById(newDepartment.getId());
		assertNotNull(testDepartment);
		
		this.assertAreSameDepartment(newDepartment, testDepartment);
	}
	
	@Test
	public void finds_department_from_id() {
		Department d = dao.getDepartmentById(testDepartment2.getId());
		assertNotNull(d);
		
		this.assertAreSameDepartment(d, testDepartment2);
	}
	
	private void assertAreSameDepartment(Department d1, Department d2) {
		assertEquals(d1.getId(), d2.getId());
		assertEquals(d1.getName(), d2.getName());
	}
}
