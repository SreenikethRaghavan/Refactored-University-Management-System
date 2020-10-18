package com.softeng306.io;

import com.softeng306.domain.student.Student;
import com.softeng306.main.Main;
import com.softeng306.validation.ValidationMgr;

import java.util.Scanner;

public class StudentMgrIO {

    private static Scanner reader;


    public StudentMgrIO(){
        reader = new Scanner(System.in);
    }

    public void printMenu(){
        System.out.println("addStudent is called");
        System.out.println("Choose the way you want to add a student:");
        System.out.println("1. Manually input the student ID.");
        System.out.println("2. Let the system self-generate the student ID.");
    }

    public boolean systemGenerateID(){
        int choice;
        do {
            System.out.println("Please input your choice:");
            if (reader.hasNextInt()) {
                choice = reader.nextInt();
                reader.nextLine();
                if (choice < 1 || choice > 2) {
                    System.out.println("Invalid input. Please re-enter.");
                } else {
                    if(choice == 1){
                        return false;
                    }
                    return true;
                }
            } else {
                System.out.println("Your input " + reader.nextLine() + " is not an integer.");
            }
        } while (true);

    }


    public String getStudentID(){
        while (true) {
            System.out.println("The student ID should follow:");
            System.out.println("Length is exactly 9");
            System.out.println("Start with U (Undergraduate)");
            System.out.println("End with a uppercase letter between A and L");
            System.out.println("Seven digits in the middle");
            System.out.println();
            System.out.println("Give this student an ID: ");
            String studentID = reader.nextLine();
            if (ValidationMgr.checkValidStudentIDInput(studentID)) {
                if (ValidationMgr.checkStudentExists(studentID) == null) {
                    return studentID;
                }
            }
        }
    }


    public String getStudentName(){
        String studentName;
        while (true) {
            System.out.println("Enter student Name: ");
            studentName = reader.nextLine();
            if (ValidationMgr.checkValidPersonNameInput(studentName)) {
                return studentName;
            }
        }
    }


    public String getSchoolName(){
        String studentSchool;
        while (true) {
            System.out.println("Enter student's school (uppercase): ");
            System.out.println("Enter -h to print all the schools.");
            studentSchool = reader.nextLine();
            while ("-h".equals(studentSchool)) {
                HelpInfoMgr.printAllDepartment();
                studentSchool = reader.nextLine();
            }

            if (ValidationMgr.checkDepartmentValidation(studentSchool)) {
                return studentSchool;
            }
        }
    }


    public String getStudentGender(){
        String studentGender;
        while (true) {
            System.out.println("Enter student gender (uppercase): ");
            System.out.println("Enter -h to print all the genders.");
            studentGender = reader.nextLine();
            while ("-h".equals(studentGender)) {
                HelpInfoMgr.printAllGender();
                studentGender = reader.nextLine();
            }

            if (ValidationMgr.checkGenderValidation(studentGender)) {
                return studentGender;
            }
        }
    }

    public int getStudentYear(){
        int studentYear;
        do {
            System.out.println("Enter student's school year (1-4) : ");
            if (reader.hasNextInt()) {
                studentYear = reader.nextInt();
                reader.nextLine();
                if (studentYear < 1 || studentYear > 4) {
                    System.out.println("Your input is out of bound.");
                    System.out.println("Please re-enter an integer between 1 and 4");
                } else {
                    return studentYear;
                }
            } else {
                System.out.println("Your input " + reader.nextLine() + " is not an integer");
                System.out.println("Please re-enter.");
            }
        } while (true);
    }

    public void printStudentID(String name, String ID){
        System.out.println("Student named: " + name + " is added, with ID: " + ID);
        System.out.println("Student List: ");
        System.out.println("| Student ID | Student Name | Student School | Gender | Year | GPA |");
        for (Student student : Main.students) {
            String GPA = "not available";
            if (Double.compare(student.getGPA(), 0.0) != 0) {
                GPA = String.valueOf(student.getGPA());
            }
            System.out.println(" " + student.getStudentID() + " | " + student.getStudentName() + " | " + student.getStudentSchool() + " | " + student.getGender() + " | " + student.getStudentYear() + " | " + GPA);
        }
    }


}
