package com.techelevator.projects.model.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.DepartmentDAO;

public class JDBCDepartmentDAO implements DepartmentDAO {

	private JdbcTemplate jdbcTemplate;

	public JDBCDepartmentDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<Department> getAllDepartments() {
		List<Department> departments = new ArrayList<>();

		String sql = "SELECT department_id, name " + "FROM department";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql);

		Department d = null;
		while (results.next()) {
			d = new Department();
			d.setId(results.getLong("department_id"));
			d.setName(results.getString("name"));

			departments.add(d);
		}

		return departments;
	}

	@Override
	public List<Department> searchDepartmentsByName(String nameSearch) {
		List<Department> departments = new ArrayList<>();

		String sql = "SELECT department_id, name " + "FROM department " + "WHERE name LIKE ?";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, "%" + nameSearch + "%");

		Department d = null;
		while (results.next()) {
			d = new Department();
			d.setId(results.getLong("department_id"));
			d.setName(results.getString("name"));

			departments.add(d);
		}

		return departments;
	}

	@Override
	public void saveDepartment(Department updatedDepartment) {
		String sql = "UPDATE department " +
					 "SET name = ? " +
					 "WHERE department_id = ?";
		jdbcTemplate.update(sql, updatedDepartment.getName(), updatedDepartment.getId());
	}

	@Override
	public Department createDepartment(Department newDepartment) {
		String sql = "INSERT INTO department (name) " +
				     "VALUES (?) RETURNING department_id";
	
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, newDepartment.getName());
		if(results.next()) {
			newDepartment.setId(results.getLong("department_id"));
		}
		
		return newDepartment;
	}

	@Override
	public Department getDepartmentById(Long id) {
		Department department = null;
		String sql = "SELECT department_id, name " +
					 "FROM department " + 
					 "WHERE department_id = ?";
		
		SqlRowSet results = this.jdbcTemplate.queryForRowSet(sql, id);
		
		if(results.next()) {
			department = new Department();
			department.setId(results.getLong("department_id"));
			department.setName(results.getString("name"));
		}
		return department;
	}

}
