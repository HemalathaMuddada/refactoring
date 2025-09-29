package com.xtremand.common.constants;

import java.util.HashSet;
import java.util.Set;

public class BusinessErrorCodes {

    private BusinessErrorCodes() {}

    public static final Set<String> CONFLICT_CODES = Set.of(
            ErrorCodes.DUPLICATE_MAPPING,
            ErrorCodes.SUPPORT_USER_OWNERSHIP_CONFLICT,
            ErrorCodes.DUPLICATE_EMAIL,
            ErrorCodes.DUPLICATE_SUPPORT_ASSIGNMENT,
            ErrorCodes.DUPLICATE_GROUP
    );

    public static final Set<String> NOT_FOUND_CODES = new HashSet<>();

    static {
        NOT_FOUND_CODES.add(ErrorCodes.MAPPING_NOT_FOUND);
    }

}
