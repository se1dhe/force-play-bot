package com.forceplay.bot.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PresentationPageController {

    @GetMapping({"/presentation", "/presentation/"})
    public String presentationPage() {
        return "forward:/sales/force-play-presentation.html";
    }
}
