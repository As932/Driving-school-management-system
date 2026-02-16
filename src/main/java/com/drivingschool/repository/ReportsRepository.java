package com.drivingschool.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Reports Repository
 * All table and column names are UPPERCASE (H2 converts unquoted identifiers to uppercase)
 */

@Repository
public class ReportsRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * COMPLEX QUERY 1: Trainees with more sessions than average
     * uses nested subqueries to calculate and compare with average
     */
    public List<Map<String, Object>> findTraineesWithAboveAverageHours() {
        String sql = """
            SELECT 
                T.TRAINEEID,
                T.FIRSTNAME,
                T.LASTNAME,
                COUNT(S.SESSIONID) AS TOTALSESSIONS
            FROM TRAINEE T
            LEFT JOIN SESSION S ON T.TRAINEEID = S.TRAINEEID 
                AND S.STATUS = 'Completed'
                AND S.SESSIONTYPE = 'Practical'
            GROUP BY T.TRAINEEID, T.FIRSTNAME, T.LASTNAME
            HAVING COUNT(S.SESSIONID) > (
                SELECT AVG(CAST(SESSION_COUNT AS DOUBLE))
                FROM (
                    SELECT COUNT(SESSIONID) AS SESSION_COUNT
                    FROM SESSION
                    WHERE STATUS = 'Completed'
                      AND SESSIONTYPE = 'Practical'
                    GROUP BY TRAINEEID
                )
            )
            ORDER BY TOTALSESSIONS DESC
            """;

        return jdbcTemplate.queryForList(sql);
    }

    /**
     * COMPLEX QUERY 2: Top instructors by student pass rate
     * uses multiple correlated subqueries with IN clause and CASE
     */
    public List<Map<String, Object>> findTopInstructorsByPassRate() {
        String sql = """
            SELECT 
                I.INSTRUCTORID,
                I.FIRSTNAME,
                I.LASTNAME,
                (SELECT COUNT(DISTINCT TRAINEEID)
                 FROM TRAINEE
                 WHERE ASSIGNEDINSTRUCTORID = I.INSTRUCTORID
                ) AS TOTALSTUDENTS,
                (SELECT COUNT(*)
                 FROM EXAM
                 WHERE TRAINEEID IN (
                     SELECT TRAINEEID 
                     FROM TRAINEE 
                     WHERE ASSIGNEDINSTRUCTORID = I.INSTRUCTORID
                 )
                 AND STATUS = 'Completed'
                ) AS PASSEDEXAMS,
                CASE 
                    WHEN (SELECT COUNT(DISTINCT TRAINEEID)
                          FROM TRAINEE
                          WHERE ASSIGNEDINSTRUCTORID = I.INSTRUCTORID) > 0
                    THEN CAST((SELECT COUNT(*)
                               FROM EXAM
                               WHERE TRAINEEID IN (
                                   SELECT TRAINEEID 
                                   FROM TRAINEE 
                                   WHERE ASSIGNEDINSTRUCTORID = I.INSTRUCTORID
                               )
                               AND STATUS = 'Completed') AS DOUBLE) * 100.0 / 
                         (SELECT COUNT(DISTINCT TRAINEEID)
                          FROM TRAINEE
                          WHERE ASSIGNEDINSTRUCTORID = I.INSTRUCTORID)
                    ELSE 0
                END AS PASSRATE
            FROM INSTRUCTOR I
            WHERE (SELECT COUNT(DISTINCT TRAINEEID)
                   FROM TRAINEE
                   WHERE ASSIGNEDINSTRUCTORID = I.INSTRUCTORID) > 0
            ORDER BY PASSRATE DESC, PASSEDEXAMS DESC
            """;

        return jdbcTemplate.queryForList(sql);
    }

    /**
     * COMPLEX QUERY 3: Instructors with more sessions than average
     * uses nested subqueries to calculate average and compare
     */
    public List<Map<String, Object>> findMostUtilizedCars() {
        String sql = """
            SELECT 
                I.INSTRUCTORID AS CARID,
                I.FIRSTNAME AS BRAND,
                I.LASTNAME AS MODEL,
                'N/A' AS LICENSEPLATE,
                COUNT(S.SESSIONID) AS SESSIONCOUNT
            FROM INSTRUCTOR I
            LEFT JOIN SESSION S ON I.INSTRUCTORID = S.INSTRUCTORID
            GROUP BY I.INSTRUCTORID, I.FIRSTNAME, I.LASTNAME
            HAVING COUNT(S.SESSIONID) > (
                SELECT AVG(CAST(SESSION_COUNT AS DOUBLE))
                FROM (
                    SELECT COUNT(SESSIONID) AS SESSION_COUNT
                    FROM SESSION
                    GROUP BY INSTRUCTORID
                )
            )
            ORDER BY SESSIONCOUNT DESC
            """;

        return jdbcTemplate.queryForList(sql);
    }

    /**
     * COMPLEX QUERY 4: Trainees behind schedule (fewer sessions than average for their enrollment period)
     * uses nested subqueries with date calculations to compare progress
     */
    public List<Map<String, Object>> findTraineesBehindSchedule() {
        String sql = """
            SELECT 
                T.TRAINEEID,
                T.FIRSTNAME,
                T.LASTNAME,
                T.ENROLLMENTDATE,
                DATEDIFF('DAY', T.ENROLLMENTDATE, CURRENT_DATE) AS DAYSENROLLED,
                COUNT(S.SESSIONID) AS COMPLETEDSESSIONS,
                COUNT(S.SESSIONID) AS COMPLETEDHOURS
            FROM TRAINEE T
            LEFT JOIN SESSION S ON T.TRAINEEID = S.TRAINEEID 
                AND S.STATUS = 'Completed'
                AND S.SESSIONTYPE = 'Practical'
            WHERE T.STATUS = 'Active'
              AND DATEDIFF('DAY', T.ENROLLMENTDATE, CURRENT_DATE) >= 30
            GROUP BY T.TRAINEEID, T.FIRSTNAME, T.LASTNAME, T.ENROLLMENTDATE
            HAVING COUNT(S.SESSIONID) < (
                SELECT AVG(CAST(SESSION_COUNT AS DOUBLE))
                FROM (
                    SELECT COUNT(S2.SESSIONID) AS SESSION_COUNT
                    FROM TRAINEE T2
                    JOIN SESSION S2 ON T2.TRAINEEID = S2.TRAINEEID
                    WHERE S2.STATUS = 'Completed'
                      AND S2.SESSIONTYPE = 'Practical'
                      AND DATEDIFF('DAY', T2.ENROLLMENTDATE, CURRENT_DATE) >= 30
                    GROUP BY T2.TRAINEEID
                )
            )
            ORDER BY COMPLETEDSESSIONS ASC
            """;

        return jdbcTemplate.queryForList(sql);
    }
}