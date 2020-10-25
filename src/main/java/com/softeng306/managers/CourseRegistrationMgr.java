package com.softeng306.managers;

import com.softeng306.Enum.GroupType;
import com.softeng306.domain.course.Course;
import com.softeng306.domain.course.courseregistration.CourseRegistration;
import com.softeng306.domain.course.group.Group;
import com.softeng306.domain.student.Student;
import com.softeng306.io.CourseRegistrationManagerIO;
import com.softeng306.io.FILEMgr;
import com.softeng306.io.MainMenuIO;
import com.softeng306.validation.*;

import java.util.*;

public class CourseRegistrationMgr {
    private static Scanner scanner = new Scanner(System.in);
    /**
     * A list of all the course registration records in this school.
     */
    private List<CourseRegistration> courseRegistrations;

    private static CourseRegistrationMgr singleInstance = null;

    /**
     * Override default constructor to implement singleton pattern
     */
    private CourseRegistrationMgr(List<CourseRegistration> courseRegistrations) {
        this.courseRegistrations = courseRegistrations;
    }

    /**
     * Return the CourseRegistrationMgr singleton, if not initialised already, create an instance.
     *
     * @return CourseRegistrationMgr the singleton instance
     */
    public static CourseRegistrationMgr getInstance() {
        if (singleInstance == null) {
            singleInstance = new CourseRegistrationMgr(FILEMgr.loadCourseRegistration());
        }

        return singleInstance;
    }


    /**
     * Registers a course for a student
     */
    public void registerCourse() {
        MainMenuIO.printMethodCall("registerCourse");
        String selectedLectureGroupName = null;
        String selectedTutorialGroupName = null;
        String selectedLabGroupName = null;

        Student currentStudent = StudentValidator.checkStudentExists();
        String studentID = currentStudent.getStudentID();

        DepartmentValidator.checkCourseDepartmentExists();

        Course currentCourse = CourseValidator.checkCourseExists();
        String courseID = currentCourse.getCourseID();


        if (CourseRegistrationValidator.checkCourseRegistrationExists(studentID, courseID) != null) {
            return;
        }

        if (currentCourse.getMainComponents().size() == 0) {
            CourseRegistrationManagerIO.printNoAssessmentMessage(currentCourse);
            return;
        }

        if (currentCourse.getVacancies() == 0) {
            CourseRegistrationManagerIO.printNoVacancies();
            return;
        }

        CourseRegistrationManagerIO.printPendingRegistrationMethod(currentCourse, currentStudent);

        List<Group> lecGroups = new ArrayList<>(0);
        lecGroups.addAll(currentCourse.getLectureGroups());

        GroupMgr groupMgr = GroupMgr.getInstance();

        selectedLectureGroupName = groupMgr.printGroupWithVacancyInfo("lecture", lecGroups);

        List<Group> tutGroups = new ArrayList<>(0);
        tutGroups.addAll(currentCourse.getTutorialGroups());

        selectedTutorialGroupName = groupMgr.printGroupWithVacancyInfo("tutorial", tutGroups);

        List<Group> labGroups = new ArrayList<>(0);
        labGroups.addAll(currentCourse.getLabGroups());

        selectedLabGroupName = groupMgr.printGroupWithVacancyInfo("lab", labGroups);

        currentCourse.enrolledIn();
        CourseRegistration courseRegistration = new CourseRegistration(currentStudent, currentCourse, selectedLectureGroupName, selectedTutorialGroupName, selectedLabGroupName);
        FILEMgr.writeCourseRegistrationIntoFile(courseRegistration);

        courseRegistrations.add(courseRegistration);

        MarkMgr.getInstance().getMarks().add(MarkMgr.getInstance().initializeMark(currentStudent, currentCourse));

        CourseRegistrationManagerIO.printSuccessfulRegistration(currentCourse, currentStudent, selectedLectureGroupName, selectedTutorialGroupName, selectedLabGroupName);
    }

    /**
     * Prints the students in a course according to their lecture group, tutorial group or lab group.
     */
    public void printStudents() {
        MainMenuIO.printMethodCall("printStudent");

        Course currentCourse = CourseValidator.checkCourseExists();
        CourseRegistrationManagerIO.printOptions();

        // READ courseRegistrationFILE
        // return List of Object(student,course,lecture,tut,lab)
        List<CourseRegistration> allCourseRegistrations = FILEMgr.loadCourseRegistration();

        List<CourseRegistration> stuArray = new ArrayList<>(0);
        for (CourseRegistration courseRegistration : allCourseRegistrations) {
            if (courseRegistration.getCourse().getCourseID().equals(currentCourse.getCourseID())) {
                stuArray.add(courseRegistration);
            }
        }

        int opt;
        do {
            opt = scanner.nextInt();
            scanner.nextLine();

            // TODO: replace these common ui elements with a library
            System.out.println("------------------------------------------------------");

            if (stuArray.size() == 0) {
               CourseRegistrationManagerIO.printNoEnrolmentsError();
            }
            if(opt == 1){
                sortByLectureGroup(stuArray);
                CourseRegistrationManagerIO.printByGroup(stuArray, GroupType.LectureGroup);

            } else if (opt == 2){
                if (stuArray.size() > 0 && stuArray.get(0).getCourse().getTutorialGroups().size() == 0) {
                    CourseRegistrationManagerIO.printNoGroup(GroupType.TutorialGroup);
                    CourseRegistrationManagerIO.printEndOfSection();
                    return;
                }
                sortByTutorialGroup(stuArray);
                CourseRegistrationManagerIO.printByGroup(stuArray, GroupType.TutorialGroup);

            } else if (opt == 3){
                if (stuArray.size() > 0 && stuArray.get(0).getCourse().getLabGroups().size() == 0) {
                    CourseRegistrationManagerIO.printNoGroup(GroupType.LabGroup);
                    CourseRegistrationManagerIO.printEndOfSection();
                    return;
                }
                sortByLabGroup(stuArray);
                CourseRegistrationManagerIO.printByGroup(stuArray, GroupType.LabGroup);

            } else {
                CourseRegistrationManagerIO.printInvalidInputError();
            }
            CourseRegistrationManagerIO.printEndOfSection();
        } while (opt < 1 || opt > 3);
    }

    /**
     * Return the list of all course registrations in the system.
     * @return An list of all course registrations.
     */
    public List<CourseRegistration> getCourseRegistrations() {
        return courseRegistrations;
    }

    /**
     * Sort the list of course registrations of a course according to their ascending
     * normal alphabetical order of the lecture groups, ignoring cases.
     * @param courseRegistrations All the course registrations of the course.
     */
    private void sortByLectureGroup(List<CourseRegistration> courseRegistrations) {
        courseRegistrations.sort((o1, o2) -> {
            // in the case where there are no lectures, we don't care about
            // the ordering.
            if (o1.getLectureGroup() == null || o2.getLectureGroup() == null) {
                return 0;
            }

            String group1 = o1.getLectureGroup().toUpperCase();
            String group2 = o2.getLectureGroup().toUpperCase();

            //ascending order
            return group1.compareTo(group2);

        });
    }

    /**
     * Sort the list of course registrations of a course according to their ascending
     * normal alphabetical order of the tutorial groups, ignoring cases.
     * @param courseRegistrations All the course registrations of the course.
     */
    private void sortByTutorialGroup(List<CourseRegistration> courseRegistrations) {
        courseRegistrations.sort((s1, s2) -> {
            // in the case where there are no tutorials, we don't care about
            // the ordering.
            if (s1.getTutorialGroup() == null || s2.getTutorialGroup() == null) {
                return 0;
            }

            String group1 = s1.getTutorialGroup().toUpperCase();
            String group2 = s2.getTutorialGroup().toUpperCase();

            //ascending order
            return group1.compareTo(group2);

        });
    }

    /**
     * Sort the list of course registrations of a course according to their ascending
     * normal alphabetical order of the lab groups, ignoring cases.
     * @param courseRegistrations All the course registrations of the course.
     */
    private void sortByLabGroup(List<CourseRegistration> courseRegistrations) {
        courseRegistrations.sort((o1, o2) -> {
            // in the case where there are no labs, we don't care about
            // the ordering.
            if (o1.getLabGroup() == null || o2.getLabGroup() == null) {
                return 0;
            }

            String group1 = o1.getLabGroup().toUpperCase();
            String group2 = o2.getLabGroup().toUpperCase();

            //ascending order
            return group1.compareTo(group2);
        });
    }

}
