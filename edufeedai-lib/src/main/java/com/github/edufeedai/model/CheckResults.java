/*
 * EduFeedAi is an automated system for retrieving, evaluating, and generating AI-based feedback for student submissions in vocational education.
 *
 * Copyright (C) 2026 Arturo Candela
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
