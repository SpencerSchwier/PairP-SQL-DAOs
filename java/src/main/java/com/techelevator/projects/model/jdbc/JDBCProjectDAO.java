package com.techelevator.projects.model.jdbc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Project;
import com.techelevator.projects.model.ProjectDAO;

public class JDBCProjectDAO implements ProjectDAO {

	private JdbcTemplate jdbcTemplate;

	public JDBCProjectDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<Project> getAllActiveProjects() {
		List<Project> projects = new ArrayList<>();

		String sql = "SELECT project_id, name, from_date, to_date " + "FROM project";

		SqlRowSet results = jdbcTemplate.queryForRowSet(sql);

		Project p = null;
		while (results.next()) {
			p = new Project();
			p.setId(results.getLong("project_id"));
			p.setName(results.getString("name"));

			LocalDate localDate = null;
			if (results.getDate("from_date") != null) {
				localDate = results.getDate("from_date").toLocalDate();
			}
			p.setStartDate(localDate);

			localDate = null;
			if (results.getDate("to_date") != null) {
				localDate = results.getDate("to_date").toLocalDate();
			}
			p.setEndDate(localDate);

			if(p.getEndDate() == null || p.getStartDate() != null && 
										 LocalDate.now().isAfter(p.getStartDate()) && 
										 LocalDate.now().isBefore(p.getEndDate())) {
				projects.add(p);
			}
		}

		return projects;
	}

	@Override
	public void removeEmployeeFromProject(Long projectId, Long employeeId) {
		String sql = "SELECT * FROM project_employee "
				+ "WHERE project_employee.project_id = ? AND project_employee.employee_id = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, projectId, employeeId);
		if (results.next()) {
			sql = "DELETE FROM project_employee " + "WHERE employee_id = ? AND project_id = ?";
			jdbcTemplate.update(sql, employeeId, projectId);
		}
	}

	@Override
	public void addEmployeeToProject(Long projectId, Long employeeId) {
		String sql = "SELECT * FROM project_employee " +
					 "WHERE project_employee.project_id = ? AND project_employee.employee_id = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sql, projectId, employeeId);
		if (!results.next()) {
			sql = "INSERT INTO project_employee (project_id, employee_id) " + "VALUES (?, ?)";
			jdbcTemplate.update(sql, projectId, employeeId);
		}
	}
}
