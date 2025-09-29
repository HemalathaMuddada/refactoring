package com.xtremand.email.specification;

import org.springframework.data.jpa.domain.Specification;
import com.xtremand.domain.entity.EmailTemplate;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public final class TemplateListSpecification {

	private TemplateListSpecification() {
	}

	public static Specification<EmailTemplate> build(String category, String search) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (category != null && !category.isEmpty()) {
				predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
			}
			if (search != null && !search.isEmpty()) {
				String pattern = "%" + search.toLowerCase() + "%";
				Predicate namePredicate = cb.like(cb.lower(root.get("name")), pattern);
				Predicate subjPredicate = cb.like(cb.lower(root.get("subjectLine")), pattern);
				Predicate varsPredicate = cb.like(cb.lower(root.get("variables")), pattern);
				Predicate contentPredicate = cb.like(root.get("content"), "%" + search + "%");
				predicates.add(cb.or(namePredicate, subjPredicate, contentPredicate, varsPredicate));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
