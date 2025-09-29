package com.xtremand.ai;

import com.xtremand.domain.dto.EmailRequest;

public class AiPromptBuilder {

    public static String buildGenericPrompt(EmailRequest req) {
        StringBuilder promptBuilder = new StringBuilder();

        if (req.getTemplateName() != null)
            promptBuilder.append("Template Name: ").append(req.getTemplateName()).append("\n");
        if (req.getCategory() != null)
            promptBuilder.append("Category: ").append(req.getCategory().name().replace('_', ' ')).append("\n");
        if (req.getSubjectLine() != null)
            promptBuilder.append("Subject Line: ").append(req.getSubjectLine()).append("\n");
        if (req.getCampaignType() != null)
            promptBuilder.append("Campaign Type: ").append(req.getCampaignType().name().replace('_', ' ')).append("\n");
        if (req.getTone() != null)
            promptBuilder.append("Tone: ").append(req.getTone().name().toLowerCase()).append("\n");
        if (req.getIndustry() != null)
            promptBuilder.append("Industry: ").append(req.getIndustry()).append("\n");
        if (req.getSenderName() != null)
            promptBuilder.append("Sender Name: ").append(req.getSenderName()).append("\n");
        if (req.getSenderEmail() != null)
            promptBuilder.append("Sender Email: ").append(req.getSenderEmail()).append("\n");
        if (req.getEmailPrompt() != null)
            promptBuilder.append("Prompt: ").append(req.getEmailPrompt()).append("\n");

        promptBuilder.append("\nYou can use the following merge tags in your email template if needed:\n")
                .append("[Name], [firstName], [lastName], [Email], [company], [phoneNumber], [jobtitle], [Location]\n");

        if (promptBuilder.length() == 0) {
            promptBuilder.append("Generate a generic email.");
        }

        return promptBuilder.toString();
    }
}
