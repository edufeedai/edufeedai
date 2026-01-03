/*
 * EduFeedAi - Automated AI-based feedback for student submissions in vocational education.
 * Copyright (c) 2026 EduFeedAi Contributors
 * SPDX-License-Identifier: MIT
 */

package com.github.edufeedai.model.openai.platform.api.interfaces;

import com.github.edufeedai.model.openai.platform.api.interfaces.exceptions.APIException;

public interface CorrectionPromptBuilder {

    public String generatePromptCheckString() throws APIException;

}
