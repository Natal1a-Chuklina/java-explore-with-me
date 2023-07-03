package ru.practicum.category;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.category.model.Category;

public interface CategoryStorage extends JpaRepository<Category, Long> {
}
