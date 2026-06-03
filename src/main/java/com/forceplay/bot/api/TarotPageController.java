package com.forceplay.bot.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TarotPageController {

    @GetMapping({"/tarot", "/tarot/"})
    public String tarotPage() {
        return "forward:/tarot/index.html";
    }
}
