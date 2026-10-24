package com.educonnect.specification;

import com.educonnect.entity.Resource;
import com.educonnect.enums.ResourceCategory;
import com.educonnect.enums.ResourceStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Data JPA Specification builder for dynamic multi-criteria resource searches.
 */
public class ResourceSpecification {

    /**
     * Builds dynamic specification for filtering resources.
     *
     * @param keyword    query string matching title, description, subject, or department
     * @param category   optional category filter
     * @param department optional department filter
     * @param semester   optional semester filter
     * @param subject    optional subject course filter
     * @param fileType   optional file format filter (e.g. "PDF")
     * @param status     workflow status filter (null for all statuses, e.g. for admin)
     */
    public static Specification<Resource> filter(
            String keyword,
            String category,
            String department,
            String semester,
            String subject,
            String fileType,
            ResourceStatus status
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Status Filter (e.g. APPROVED for public browsing)
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 2. Keyword Search across multiple fields
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                Predicate subjectLike = cb.like(cb.lower(root.get("subject")), pattern);
                Predicate deptLike = cb.like(cb.lower(root.get("department")), pattern);
                Predicate tagsLike = cb.like(cb.lower(root.get("tags")), pattern);

                predicates.add(cb.or(titleLike, descLike, subjectLike, deptLike, tagsLike));
            }

            // 3. Category Filter
            if (StringUtils.hasText(category)) {
                ResourceCategory catEnum = ResourceCategory.fromString(category);
                predicates.add(cb.equal(root.get("category"), catEnum));
            }

            // 4. Department Filter
            if (StringUtils.hasText(department)) {
                predicates.add(cb.equal(cb.lower(root.get("department")), department.trim().toLowerCase()));
            }

            // 5. Semester Filter
            if (StringUtils.hasText(semester)) {
                predicates.add(cb.equal(cb.lower(root.get("semester")), semester.trim().toLowerCase()));
            }

            // 6. Subject Filter
            if (StringUtils.hasText(subject)) {
                predicates.add(cb.equal(cb.lower(root.get("subject")), subject.trim().toLowerCase()));
            }

            // 7. File Type Filter
            if (StringUtils.hasText(fileType)) {
                predicates.add(cb.equal(cb.upper(root.get("fileType")), fileType.trim().toUpperCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
