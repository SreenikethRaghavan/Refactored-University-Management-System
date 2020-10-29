package com.softeng306.validation;


import com.softeng306.domain.professor.Professor;
import com.softeng306.managers.ProfessorMgr;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProfessorValidator {

    /**
     * Checks whether this professor ID is used by other professors.
     *
     * @param profID The inputted professor ID.
     * @return the existing professor or else null.
     */
    public static Professor getProfessorFromID(String profID) {
        List<Professor> anyProf = ProfessorMgr.getInstance().getProfessors().stream().filter(p -> profID.equals(p.getProfID())).collect(Collectors.toList());
        if (anyProf.isEmpty()) {
            return null;
        }
        return anyProf.get(0);

    }

    public static boolean checkProfessorExists(String profID){
        Optional<Professor> professor = ProfessorMgr
                .getInstance()
                .getProfessors()
                .stream()
                .filter(p -> profID.equals(p.getProfID()))
                .findFirst();

        return professor.isPresent();
    }

}
