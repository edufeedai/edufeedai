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

package com.github.edufeedai.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.edufeedai.model.openai.platform.api.OpenAICorrectionPromptBuilder;

import io.github.cdimascio.dotenv.Dotenv;

@Disabled // Estoy haciendo pruebas con maven y no quiero que se ejecute este test
public class OpenAICorrectionPromptBuilderTest {

    String OpenAI_KEY = Dotenv.load().get("OPENAI_API_KEY");

    String messageForRoleSystem = "Eres un corrector de actividades de un curso de Formación Profesional y ahora te van a proporcionar el Contexto, el trabajo a realizar y la rubrica de evaluación para que "+ 
            "elabores un mensaje de configuración para un system porque luego este mensaje se va a utilizar para configurar una ia que corregira automaticamente el trabajo enviado por el alumno";
    String context = "Es un segundo curso de un ciclo formativo de grado superior de ASIX en la comunidad valenciana";
    String activityStatement = "Se pide realizar una configuración DNS en linux utilizando bind 9 para un dominio llamado asixalcoy.org";
    String rubric = "Ha creado los ficheros de zona: db.asixalcoy.org y db.192.168.0 y ha configurado el servidor DNS para que resuelva correctamente los nombres de los equipos de la red " + 
                    "y el nombre del dominio asixalcoy.org a la dirección IP" + 
                    "ha configurado el servidor DHCP para que informe del nuevo dns a los clientes";


    @Test
    void testGeneratePromptCheckString() {

        OpenAICorrectionPromptBuilder oacpb = new OpenAICorrectionPromptBuilder(context, activityStatement, rubric, OpenAI_KEY);

        try {

            String prompt = oacpb.generatePromptCheckString();
            System.out.println(prompt);
            assertNotNull(prompt);

        } catch (Exception e) {
            fail(e);
        }


    }
}
