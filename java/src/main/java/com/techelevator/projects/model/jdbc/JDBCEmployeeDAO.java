package com.techelevator.projects.model.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Employee;
import com.techelevator.projects.model.EmployeeDAO;

public class JDBCEmployeeDAO implements EmployeeDAO {

	private JdbcTemplate jdbcTemplate;

	public JDBCEmployeeDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public List<Employee> getAllEmployees() {
		List<Employee> employees = new ArrayList<>();

		String sql = "SELECT employee_id, department_id, first_name, last_name, birth_date, gender, hire_date " + "FROM employee";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql);

		Employee e = null;
		while (results.next()) {
			e = this.mapRowToEmployee(results);
			employees.add(e);
		}

		return employees;
	}

	@Override
	public List<Employee> searchEmployeesByName(String firstNameSearch, String lastNameSearch) {
		List<Employee> employees = new ArrayList<>();

		String sql = "SELECT employee_id, department_id, first_name, last_name, birth_date, gender, hire_date " + 
					 "FROM employee " +
					 "WHERE first_name LIKE ? AND last_name LIKE ?";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, firstNameSearch, lastNameSearch);

		Employee e = null;
		while (results.next()) {
			e = this.mapRowToEmployee(results);
			employees.add(e);
		}

		return employees;
	}

	@Override
	public List<Employee> getEmployeesByDepartmentId(long id) {
		List<Employee> employees = new ArrayList<>();

		String sql = "SELECT employee.employee_id, employee.department_id, first_name, last_name, birth_date, gender, hire_date " + 
					 "FROM employee " +
					 "JOIN department ON employee.department_id = department.department_id " +
					 "WHERE department.department_id = ?";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);

		Employee e = null;
		while (results.next()) {
			e = this.mapRowToEmployee(results);
			employees.add(e);
		}

		return employees;
	}

	@Override
	public List<Employee> getEmployeesWithoutProjects() {
		List<Employee> employees = new ArrayList<>();

		String sql = "SELECT employee_id, department_id, first_name, last_name, birth_date, gender, hire_date " + 
				 	 "FROM employee " + 
				 	 "WHERE employee_id NOT IN (SELECT employee.employee_id " + 
				 						   	   "FROM employee " + 
				 						       "JOIN project_employee " + 
				 						       "ON employee.employee_id = project_employee.employee_id)";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql);

		Employee e = null;
		while (results.next()) {
			e = this.mapRowToEmployee(results);
			employees.add(e);
		}

		return employees;
	}

	@Override
	public List<Employee> getEmployeesByProjectId(Long projectId) {
		List<Employee> employees = new ArrayList<>();

		String sql = "SELECT employee.employee_id, department_id, first_name, last_name, birth_date, gender, hire_date " + 
					 "FROM employee " + 
					 "JOIN project_employee ON project_employee.employee_id = employee.employee_id " + 
					 "WHERE project_employee.project_id = ?";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, projectId);

		Employee e = null;
		while (results.next()) {
			e = this.mapRowToEmployee(results);
			employees.add(e);
		}

		return employees;
	}

	@Override
	public void changeEmployeeDepartment(Long employeeId, Long departmentId) {
		String sql = "UPDATE employee " +
					 "SET department_id = ? " +
					 "WHERE employee_id = ?";
		jdbcTemplate.update(sql, departmentId, employeeId);
	}
	
	private Employee mapRowToEmployee(SqlRowSet row) {
		Employee e = new Employee();
		
		e.setId(row.getLong("employee_id"));
		e.setDepartmentId(row.getLong("department_id"));
		e.setFirstName(row.getString("first_name"));
		e.setLastName(row.getString("last_name"));
		e.setBirthDay(row.getDate("birth_date").toLocalDate());
		e.setGender(row.getString("gender").charAt(0));
		e.setHireDate(row.getDate("hire_date").toLocalDate());
		
		return e;
	}
}
