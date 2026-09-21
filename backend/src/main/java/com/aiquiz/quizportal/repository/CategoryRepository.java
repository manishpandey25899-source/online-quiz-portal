package com.aiquiz.quizportal.repository;

import com.aiquiz.quizportal.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}