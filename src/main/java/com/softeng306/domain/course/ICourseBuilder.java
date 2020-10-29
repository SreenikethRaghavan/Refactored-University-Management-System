package com.softeng306.domain.course;

import java.util.Map;

public interface ICourseBuilder {
    void setCourseID(String id);

    void setCourseName(String name);

    void setProfInCharge(String profInCharge);

    void setTotalSeats(int totalSeats);

    void setLectureGroups(Map<String, Double> lectureGroups);

    void setTutorialGroups(Map<String, Double> tutorialGroups);

    void setLabGroups(Map<String, Double> labGroups);

    void setAU(int AU);

    void setCourseDepartment(String department);

    void setCourseType(String Type);

    void setLecWeeklyHour(int lecWeeklyHour);

    void setTutWeeklyHour(int tutWeeklyHour);

    void setLabWeeklyHour(int labWeeklyHour);

    Course build();
}
