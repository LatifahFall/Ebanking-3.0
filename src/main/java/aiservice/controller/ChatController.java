package aiservice.controller;

import aiservice.dto.ChatRequest;
import aiservice.service.OpenAIChatService;
import org.springframework.web.bind.annotation.*;
import aiservice.service.PromptService;
@RestController
@RequestMapping("/api/chat")
public class  ChatController {
    private final OpenAIChatService openAIChatService;
    private final PromptService promptService;

    public ChatController(OpenAIChatService openAIChatService, PromptService promptService) {
        this.openAIChatService = openAIChatService;
        this.promptService = promptService;
    }

    @PostMapping
    public String chat(@RequestBody ChatRequest request) {
        String prompt = promptService.buildPrompt(request);
        System.out.println("Generated Prompt: " + prompt);
        return openAIChatService.getChatCompletion(prompt);
    }
}
