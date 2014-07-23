package org.softeg.slartus.forpdaapi.post;/*
 * Created by slinkin on 15.07.2014.
 */

import java.util.ArrayList;
import java.util.List;

public class Interview {
    private String title;
    private List<Question> questions = new ArrayList<>();

    public List<Question> getQuestions() {
        return questions;
    }

    public class Question {
        private boolean multy = false;
        private List<String> answers = new ArrayList<>();

        public List<String> getAnswers() {
            return answers;
        }
    }
}
