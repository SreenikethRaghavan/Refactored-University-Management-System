package com.softeng306.managers;


import com.softeng306.domain.course.Course;
import com.softeng306.domain.course.component.CourseworkComponent;
import com.softeng306.domain.course.component.MainComponent;
import com.softeng306.domain.course.component.SubComponent;
import com.softeng306.domain.mark.MainComponentMark;
import com.softeng306.domain.mark.Mark;
import com.softeng306.domain.mark.SubComponentMark;
import com.softeng306.domain.student.Student;
import com.softeng306.io.FILEMgr;
import com.softeng306.validation.CourseValidator;
import com.softeng306.validation.StudentValidator;

import java.util.*;

/**
 * Manages all the mark related operations.
 */

public class MarkMgr {
    private static Scanner scanner = new Scanner(System.in);
    /**
     * A list of all the student mark records in this school.
     */
    public static List<Mark> marks = new ArrayList<>(0);

    private static MarkMgr singleInstance = null;

    /**
     * Override default constructor to implement singleton pattern
     */
    private MarkMgr(List<Mark> marks) {
        this.marks = marks;
    }

    /**
     * Return the MarkMgr singleton, if not initialised already, create an instance.
     *
     * @return MarkMgr the singleton instance
     */
    public static MarkMgr getInstance() {
        if (singleInstance == null) {
            singleInstance = new MarkMgr(FILEMgr.loadStudentMarks());
        }

        return singleInstance;
    }


    /**
     * Initializes marks for a student when he/she just registered a course.
     *
     * @param student the student this mark record belongs to.
     * @param course  the course this mark record about.
     * @return the new added mark.
     */
    public static Mark initializeMark(Student student, Course course) {
        List<MainComponentMark> courseWorkMarks = new ArrayList<>();
        double totalMark = 0d;
        List<MainComponent> mainComponents = course.getMainComponents();

        for (MainComponent mainComponent : mainComponents) {
            MainComponentMark mainComponentMark = new MainComponentMark(mainComponent, 0d);

            for (SubComponent subComponent: mainComponent.getSubComponents()) {
                mainComponentMark.addSubComponentMark(new SubComponentMark(subComponent, 0d));
            }
            courseWorkMarks.add(mainComponentMark);
        }
        Mark mark = new Mark(student, course, courseWorkMarks, totalMark);
        FILEMgr.updateStudentMarks(mark);
        return mark;
    }

    /**
     * Sets the coursework mark for the mark record.
     *
     * @param isExam whether this coursework component refers to "Exam"
     */
    public void setCourseWorkMark(boolean isExam) {
        System.out.println("enterCourseWorkMark is called");

        String studentID = StudentValidator.checkStudentExists().getStudentID();
        String courseID = CourseValidator.checkCourseExists().getCourseID();

        for (Mark mark : MarkMgr.marks) {
            if (mark.getCourse().getCourseID().equals(courseID) && mark.getStudent().getStudentID().equals(studentID)) {
                //put the set mark function here
                if (!isExam) {
                    System.out.println("Here are the choices you can have: ");

                    ArrayList<String> availableChoices = new ArrayList<String>(0);
                    ArrayList<Double> weights = new ArrayList<Double>(0);
                    ArrayList<Boolean> isMainAss = new ArrayList<Boolean>(0);

                    for (MainComponentMark mainComponentMark: mark.getCourseWorkMarks()) {
                        MainComponent mainComponent = mainComponentMark.getMainComponent();
                        if (!mainComponent.getComponentName().equals("Exam")
                                && !mainComponentMark.hasSubComponents()) {
                            availableChoices.add(mainComponent.getComponentName());
                            weights.add((double) mainComponent.getComponentWeight());
                            isMainAss.add(true);
                        }

                        for (SubComponentMark subComponentMark: mainComponentMark.getSubComponentMarks()) {
                            SubComponent subComponent = subComponentMark.getSubComponent();
                            availableChoices.add(mainComponent.getComponentName() + "-" + subComponent.getComponentName());
                            weights.add((double) mainComponent.getComponentWeight() * (double) subComponent.getComponentWeight() / 100d);
                            isMainAss.add(false);
                        }
                    }

                    for (int i = 0; i < availableChoices.size(); i++) {
                        System.out.println((i + 1) + ". " + availableChoices.get(i) + " Weight in Total: " + weights.get(i) + "%");
                    }
                    System.out.println((availableChoices.size() + 1) + ". Quit");

                    int choice;
                    System.out.println("Enter your choice");
                    choice = scanner.nextInt();
                    scanner.nextLine();

                    while (choice > (availableChoices.size() + 1) || choice < 0) {
                        System.out.println("Please enter choice between " + 0 + "~" + (availableChoices.size() + 1));
                        System.out.println("Enter your choice");
                        choice = scanner.nextInt();
                        scanner.nextLine();
                    }

                    if (choice == (availableChoices.size() + 1)) {
                        return;
                    }

                    double assessmentMark;
                    System.out.println("Enter the mark for this assessment:");
                    assessmentMark = scanner.nextDouble();
                    scanner.nextLine();
                    while (assessmentMark > 100 || assessmentMark < 0) {
                        System.out.println("Please enter mark in range 0 ~ 100.");
                        assessmentMark = scanner.nextDouble();
                        scanner.nextLine();
                    }

                    if (isMainAss.get(choice - 1)) {
                        // This is a stand alone main assessment
                        mark.setMainCourseWorkMarks(availableChoices.get(choice - 1), assessmentMark);
                    } else {
                        mark.setSubCourseWorkMarks(availableChoices.get(choice - 1).split("-")[1], assessmentMark);
                    }

                } else {
                    // The user want to enter exam mark.
                    double examMark;
                    System.out.println("Enter exam mark:");
                    examMark = scanner.nextDouble();
                    scanner.nextLine();
                    while (examMark > 100 || examMark < 0) {
                        System.out.println("Please enter mark in range 0 ~ 100.");
                        examMark = scanner.nextDouble();
                        scanner.nextLine();
                    }
                    mark.setMainCourseWorkMarks("Exam", examMark);
                }

                return;
            }
        }

        System.out.println("This student haven't registered " + courseID);

    }

    /**
     * Computes the sum of marks for a particular component of a particular course
     *
     * @param thisCourseMark    the list of mark records belong to a particular course
     * @param thisComponentName the component name interested.
     * @return the sum of component marks
     */
    public double computeMark(List<Mark> thisCourseMark, String thisComponentName) {
        double averageMark = 0;
        for (Mark mark : thisCourseMark) {
            List<MainComponentMark> thisComponentMarks = mark.getCourseWorkMarks();

            for (MainComponentMark mainComponentMark : thisComponentMarks) {
                MainComponent mainComponent = mainComponentMark.getMainComponent();
                if (mainComponent.getComponentName().equals((thisComponentName))) {
                    averageMark += mainComponentMark.getMark();
                    break;
                }

                for (SubComponentMark subComponentMark: mainComponentMark.getSubComponentMarks()) {
                    SubComponent subComponent = subComponentMark.getSubComponent();
                    if (subComponent.getComponentName().equals((thisComponentName))) {
                        averageMark += subComponentMark.getMark();
                        break;
                    }
                }
            }
        }
        return averageMark;
    }



    /**
     * Prints transcript (Results of course taken) for a particular student
     */
    public void printStudentTranscript() {
        String studentID = StudentValidator.checkStudentExists().getStudentID();

        double studentGPA = 0d;
        int thisStudentAU = 0;
        List<Mark> thisStudentMark = new ArrayList<>(0);
        for (Mark mark : MarkMgr.marks) {
            if (mark.getStudent().getStudentID().equals(studentID)) {
                thisStudentMark.add(mark);
                thisStudentAU += mark.getCourse().getAU();
            }
        }

        if (thisStudentMark.size() == 0) {
            System.out.println("------ No transcript ready for this student yet ------");
            return;
        }
        System.out.println("----------------- Official Transcript ------------------");
        System.out.print("Student Name: " + thisStudentMark.get(0).getStudent().getStudentName());
        System.out.println("\tStudent ID: " + thisStudentMark.get(0).getStudent().getStudentID());
        System.out.println("AU for this semester: " + thisStudentAU);
        System.out.println();


        for (Mark mark : thisStudentMark) {
            System.out.print("Course ID: " + mark.getCourse().getCourseID());
            System.out.println("\tCourse Name: " + mark.getCourse().getCourseName());

            for (MainComponentMark mainComponentMark: mark.getCourseWorkMarks()) {
                MainComponent mainComponent = mainComponentMark.getMainComponent();
                Double result = mainComponentMark.getMark();

                System.out.println("Main Assessment: " + mainComponent.getComponentName() + " ----- (" + mainComponent.getComponentWeight() + "%)");
                int mainAssessmentWeight = mainComponent.getComponentWeight();

                for (SubComponentMark subComponentMark: mainComponentMark.getSubComponentMarks()) {
                    SubComponent subComponent = subComponentMark.getSubComponent();
                    System.out.print("Sub Assessment: " + subComponent.getComponentName() + " -- (" + subComponent.getComponentWeight() + "% * " + mainAssessmentWeight + "%) --- ");
                    String subAssessmentName = subComponent.getComponentName();
                    System.out.println("Mark: " + String.valueOf(subComponentMark.getMark()));
                }

                System.out.println("Main Assessment Total: " + result);
                System.out.println();
            }

            System.out.println("Course Total: " + mark.getTotalMark());
            studentGPA += gpaCalcualtor(mark.getTotalMark()) * mark.getCourse().getAU();
            System.out.println();
        }
        studentGPA /= thisStudentAU;
        System.out.println("GPA for this semester: " + studentGPA);
        if (studentGPA >= 4.50) {
            System.out.println("On track of First Class Honor!");
        } else if (studentGPA >= 4.0) {
            System.out.println("On track of Second Upper Class Honor!");
        } else if (studentGPA >= 3.5) {
            System.out.println("On track of Second Lower Class Honor!");
        } else if (studentGPA >= 3) {
            System.out.println("On track of Third Class Honor!");
        } else {
            System.out.println("Advice: Study hard");
        }
        System.out.println("------------------ End of Transcript -------------------");
    }

    /**
     * Computes the gpa gained for this course from the result of this course.
     *
     * @param result result of this course
     * @return the grade (in A, B ... )
     */
    public double gpaCalcualtor(double result) {
        if (result > 85) {
            // A+, A
            return 5d;
        } else if (result > 80) {
            // A-
            return 4.5;
        } else if (result > 75) {
            // B+
            return 4d;
        } else if (result > 70) {
            // B
            return 3.5;
        } else if (result > 65) {
            // B-
            return 3d;
        } else if (result > 60) {
            // C+
            return 2.5d;
        } else if (result > 55) {
            // C
            return 2d;
        } else if (result > 50) {
            // D+
            return 1.5d;
        } else if (result > 45) {
            // D
            return 1d;
        } else {
            // F
            return 0d;
        }

    }
}
