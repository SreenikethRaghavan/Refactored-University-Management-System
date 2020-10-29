package com.softeng306.domain.course.courseregistration;

import com.softeng306.domain.course.Course;
import com.softeng306.domain.course.group.Group;
import com.softeng306.domain.student.Student;
import com.softeng306.enums.GroupType;

public class CourseRegistration {
    private Student student;
    private Course course;
    private Group lectureGroup;
    private Group tutorialGroup;
    private Group labGroup;

    /**
     * Default constructor. Required for Jackson serialization.
     */
    public CourseRegistration() {

    }

    public CourseRegistration(Student student, Course course, Group lectureGroup, Group tutorialGroup, Group labGroup) {
        this.student = student;
        this.course = course;
        this.lectureGroup = lectureGroup;
        this.tutorialGroup = tutorialGroup;
        this.labGroup = labGroup;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public Group getLectureGroup() {
        return lectureGroup;
    }

    public Group getTutorialGroup() {
        return tutorialGroup;
    }

    public Group getLabGroup() {
        return labGroup;
    }

    public Group getGroupByType(GroupType type) {
        if (type == GroupType.LECTURE_GROUP) {
            return lectureGroup;
        } else if (type == GroupType.TUTORIAL_GROUP) {
            return tutorialGroup;
        } else if (type == GroupType.LAB_GROUP) {
            return labGroup;
        }
        return null;
    }
}
