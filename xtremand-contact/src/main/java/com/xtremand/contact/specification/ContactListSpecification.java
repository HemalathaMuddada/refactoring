package com.xtremand.contact.specification;

import com.xtremand.domain.entity.ContactList;
import org.springframework.data.jpa.domain.Specification;

public final class ContactListSpecification {

    // Private constructor to prevent instantiation
    private ContactListSpecification() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    public static Specification<ContactList> hasNameOrDescriptionLike(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String likePattern = "%" + search.toLowerCase() + "%";

            return builder.or(
                    builder.like(builder.lower(root.get("name")), likePattern),
                    builder.like(builder.lower(root.get("description")), likePattern)
            );
        };
    }
}
