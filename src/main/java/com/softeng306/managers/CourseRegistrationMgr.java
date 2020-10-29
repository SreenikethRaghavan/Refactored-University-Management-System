package com.softeng306.managers;

import com.softeng306.domain.course.Course;
import com.softeng306.domain.course.courseregistration.CourseRegistration;
import com.softeng306.domain.course.group.Group;
import com.softeng306.domain.student.Student;

import com.softeng306.fileprocessing.CourseRegistrationFileProcessor;
import com.softeng306.fileprocessing.IFileProcessor;

import com.softeng306.enums.GroupType;

import com.softeng306.io.CourseRegistrationMgrIO;

import com.softeng306.validation.CourseRegistrationValidator;
import com.softeng306.validation.CourseValidator;
import com.softeng306.validation.StudentValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CourseRegistrationMgr {
    private static Scanner scanner = new Scanner(System.in);
    /**
     * A list of all the course registration records in this school.
     */
    private List<CourseRegistration> courseRegistrations;

    private static CourseRegistrationMgr singleInstance = null;

    private final IFileProcessor<CourseRegistration> courseRegistrationFileProcessor;

    /**
     * Override default constructor to implement singleton pattern
     */
    private CourseRegistrationMgr() {
        courseRegistrationFileProcessor = new CourseRegistrationFileProcessor();
        courseRegistrations = courseRegistrationFileProcessor.loadFile();
    }

    /**
     * Return the CourseRegistrationMgr singleton, if not initialised already, create an instance.
     *
     * @return CourseRegistrationMgr the singleton instance
     */
    public static CourseRegistrationMgr getInstance() {
        if (singleInstance == null) {
            singleInstance = new CourseRegistrationMgr();
        }

        return singleInstance;
    }


    /**
     * Registers a course for a student
     */
    public List<String> registerCourse(String studentID, String courseID) {
        CourseRegistrationMgrIO io = new CourseRegistrationMgrIO();
        Student currentStudent = StudentValidator.getStudentFromId(studentID);
        Course currentCourse = CourseValidator.getCourseFromId(courseID);

        if (CourseRegistrationValidator.checkCourseRegistrationExists(studentID, courseID) != null) {
            return null;
        }

        if (currentCourse.getMainComponents().isEmpty()) {
            io.printNoAssessmentMessage(currentCourse.getProfInCharge().getProfName());
            return null;
        }

        if (currentCourse.getVacancies() == 0) {
            io.printNoVacancies();
            return null;
        }

        io.printPendingRegistrationMethod(currentStudent.getStudentName(), currentStudent.getStudentID(), currentCourse.getCourseID(), currentCourse.getCourseName());

        List<Group> lecGroups = new ArrayList<>();
        lecGroups.addAll(currentCourse.getLectureGroups());

        GroupMgr groupMgr = GroupMgr.getInstance();

        Group selectedLectureGroup = groupMgr.printGroupWithVacancyInfo(GroupType.LECTURE_GROUP, lecGroups);

        List<Group> tutGroups = new ArrayList<>();
        tutGroups.addAll(currentCourse.getTutorialGroups());

        Group selectedTutorialGroup = groupMgr.printGroupWithVacancyInfo(GroupType.TUTORIAL_GROUP, tutGroups);

        List<Group> labGroups = new ArrayList<>();
        labGroups.addAll(currentCourse.getLabGroups());

        Group selectedLabGroup = groupMgr.printGroupWithVacancyInfo(GroupType.LAB_GROUP, labGroups);

        currentCourse.enrolledIn();
        CourseRegistration courseRegistration = new CourseRegistration(currentStudent, currentCourse, selectedLectureGroup, selectedTutorialGroup, selectedLabGroup);
        courseRegistrationFileProcessor.writeNewEntryToFile(courseRegistration);

        MarkMgr.getInstance().getMarks().add(MarkMgr.getInstance().initialiseMark(currentStudent, currentCourse));

        courseRegistrations.add(courseRegistration);

        List<String> registrationInfo = new ArrayList<>();
        registrationInfo.add(currentStudent.getStudentName());

        registrationInfo.add(selectedLectureGroup.getGroupName());

        if (selectedTutorialGroup != null) {
            registrationInfo.add(selectedTutorialGroup.getGroupName());
        } else {
            registrationInfo.add("");
        }

        if (selectedLabGroup != null) {
            registrationInfo.add(selectedLabGroup.getGroupName());
        } else {
            registrationInfo.add("");
        }

        return registrationInfo;
    }

    /**
     * Prints the students in a course according to their lecture group, tutorial group or lab group.
     */
    public void printStudents(String courseID, int opt) {
        CourseRegistrationMgrIO io = new CourseRegistrationMgrIO();

        Course currentCourse = CourseValidator.getCourseFromId(courseID);

        // READ courseRegistrationFILE
        // return List of Object(student,course,lecture,tut,lab)
        List<CourseRegistration> allCourseRegistrations = courseRegistrationFileProcessor.loadFile();

        List<CourseRegistration> courseRegistrationList = new ArrayList<>();
        for (CourseRegistration courseRegistration : allCourseRegistrations) {
            if (courseRegistration.getCourse().getCourseID().equals(currentCourse.getCourseID())) {
                courseRegistrationList.add(courseRegistration);
            }
        }

        if (courseRegistrationList.isEmpty()) {
            io.printNoEnrolmentsError();
        }

        if (opt == 1) {
            sortByLectureGroup(courseRegistrationList);
            List<String> groupString = getGroupString(courseRegistrationList, GroupType.LECTURE_GROUP);
            io.printGroupString(groupString);
        } else if (opt == 2) {
            if (!courseRegistrationList.isEmpty() && courseRegistrationList.get(0).getCourse().getTutorialGroups().isEmpty()) {
                io.printNoGroup(GroupType.TUTORIAL_GROUP.toString());
                io.printEndOfSection();
                return;
            }
            sortByTutorialGroup(courseRegistrationList);
            List<String> groupString = getGroupString(courseRegistrationList, GroupType.TUTORIAL_GROUP);
            io.printGroupString(groupString);

        } else if (opt == 3) {
            if (!courseRegistrationList.isEmpty() && courseRegistrationList.get(0).getCourse().getLabGroups().isEmpty()) {
                io.printNoGroup(GroupType.LAB_GROUP.toString());
                io.printEndOfSection();
                return;
            }
            sortByLabGroup(courseRegistrationList);
            List<String> groupString = getGroupString(courseRegistrationList, GroupType.LAB_GROUP);
            io.printGroupString(groupString);

        } else {
            io.printInvalidInputError();
        }

        io.printEndOfSection();
    }

    /**
     * Return the list of all course registrations in the system.
     *
     * @return An list of all course registrations.
     */
    public List<CourseRegistration> getCourseRegistrations() {
        return courseRegistrations;
    }

    /**
     * Sort the list of course registrations of a course according to their ascending
     * normal alphabetical order of names of the lecture groups, ignoring cases.
     *
     * @param courseRegistrations All the course registrations of the course.
     */
    private void sortByLectureGroup(List<CourseRegistration> courseRegistrations) {
        courseRegistrations.sort((o1, o2) -> {
            // in the case where there are no lectures, we don't care about
            // the ordering.
            if (o1.getLectureGroup() == null || o2.getLectureGroup() == null) {
                return 0;
            }

            String group1 = o1.getLectureGroup().getGroupName().toUpperCase();
            String group2 = o2.getLectureGroup().getGroupName().toUpperCase();

            //ascending order
            return group1.compareTo(group2);

        });
    }

    /**
     * Sort the list of course registrations of a course according to their ascending
     * normal alphabetical order of the names of the tutorial groups, ignoring cases.
     *
     * @param courseRegistrations All the course registrations of the course.
     */
    private void sortByTutorialGroup(List<CourseRegistration> courseRegistrations) {
        courseRegistrations.sort((s1, s2) -> {
            // in the case where there are no tutorials, we don't care about
            // the ordering.
            if (s1.getTutorialGroup() == null || s2.getTutorialGroup() == null) {
                return 0;
            }

            String group1 = s1.getTutorialGroup().getGroupName().toUpperCase();
            String group2 = s2.getTutorialGroup().getGroupName().toUpperCase();

            //ascending order
            return group1.compareTo(group2);

        });
    }

    /**
     * Sort the list of course registrations of a course according to their ascending
     * normal alphabetical order of the names of the lab groups, ignoring cases.
     *
     * @param courseRegistrations All the course registrations of the course.
     */
    private void sortByLabGroup(List<CourseRegistration> courseRegistrations) {
        courseRegistrations.sort((o1, o2) -> {
            // in the case where there are no labs, we don't care about
            // the ordering.
            if (o1.getLabGroup() == null || o2.getLabGroup() == null) {
                return 0;
            }

            String group1 = o1.getLabGroup().getGroupName().toUpperCase();
            String group2 = o2.getLabGroup().getGroupName().toUpperCase();

            //ascending order
            return group1.compareTo(group2);
        });
    }

    public List<String> getCourseIdsForStudentId(String studentId) {
        List<String> courseIds = new ArrayList<>();
        for (CourseRegistration courseRegistration : courseRegistrations) {
            if (courseRegistration.getStudent().getStudentID().equals(studentId)) {
                courseIds.add(courseRegistration.getCourse().getCourseID());
            }
        }

        return courseIds;
    }

    public int getStudentTotalAU(Student student) {
        int total = 0;
        for (CourseRegistration courseRegistration : courseRegistrations) {
            if (courseRegistration.getStudent().equals(student)) {
                total += courseRegistration.getCourse().getAU();
            }
        }
        return total;
    }

    public List<String> getUniqueGroupNames(List<CourseRegistration> courseRegistrations, GroupType groupType) {
        List<String> uniqueGroupNames = new ArrayList<>();
        for (CourseRegistration courseRegistration : courseRegistrations) {
            if (!uniqueGroupNames.contains(courseRegistration.getGroupByType(groupType).getGroupName())) {
                uniqueGroupNames.add(courseRegistration.getTutorialGroup().getGroupName());
            }
        }
        return uniqueGroupNames;
    }


    /**
     * This method prints the students of a given group
     *
     * @param courseRegistrations the list registrations for a course
     * @param groupType           the group of registration that we want to print
     */
    public List<String> getGroupString(List<CourseRegistration> courseRegistrations, GroupType groupType) {
        List<String> groupStringInfo = new ArrayList<>();
        if (courseRegistrations.isEmpty()) {
            return groupStringInfo;
        }

        String groupName = "";
        for (CourseRegistration courseRegistration : courseRegistrations) {
            String courseRegGroupName = courseRegistration.getGroupByType(groupType).getGroupName();
            if (!groupName.equals(courseRegGroupName)) {
                groupName = courseRegGroupName;
                groupStringInfo.add(groupType.getNameWithCapital() + " group : " + groupName);
            }

            groupStringInfo.add("Student Name: " + courseRegistration.getStudent().getStudentName() + " Student ID: " + courseRegistration.getStudent().getStudentID());
        }
        groupStringInfo.add("");

        return groupStringInfo;
    }


}
