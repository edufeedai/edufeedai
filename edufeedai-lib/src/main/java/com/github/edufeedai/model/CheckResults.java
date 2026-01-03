/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.github.edufeedai.model.exceptions.AssessmentErrorException;
import com.google.gson.Gson;

public class CheckResults {

    private static final String API_URL = "http://localhost:3000/grade";

    private static CheckResults _instance;

    public static synchronized CheckResults getInstance(){
        if (null == _instance){
            _instance = new CheckResults();
        }
        return _instance;
    }

    private CheckResults(){

    }

    public Assessment createNewAssessment(String gradingCriteria,String taskSubmitted) throws AssessmentErrorException {

        Gson gson = new Gson();

        AssessmentBase assessmentBase = new AssessmentBase(gradingCriteria, taskSubmitted);

        String json = gson.toJson(assessmentBase);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URL(API_URL).toURI())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            //client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            //        .thenApply(HttpResponse::body)
            //        .thenAccept(System.out::println)
            //        .join();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            AssessmentBase assessmentBaseResponse = gson.fromJson(response.body(), AssessmentBase.class);

            return new Assessment(assessmentBaseResponse);

        } catch (Exception e) {
            throw new AssessmentErrorException(e);
        }

    }

    public void gradeAssessment(Assessment assessment) throws AssessmentErrorException {

        throw new UnsupportedOperationException("Not supported yet.");

    }

}
