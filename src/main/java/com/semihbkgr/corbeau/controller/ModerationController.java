package com.semihbkgr.corbeau.controller;

import com.semihbkgr.corbeau.service.ModeratorDetailsService;
import com.semihbkgr.corbeau.service.ModeratorService;
import com.semihbkgr.corbeau.service.RoleService;
import com.semihbkgr.corbeau.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Controller
@RequestMapping("/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ModeratorService moderatorService;
    private final RoleService roleService;
    private final SubjectService subjectService;

    @GetMapping("/login")
    public String login() {
        return "/moderation/login";
    }

    @GetMapping({"", "/", "/menu"})
    public Mono<String> panel(final Model model) {
        return Mono.from(ReactiveSecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(ModeratorDetailsService.ModeratorDetails.class::cast)
                .map(moderatorDetails -> {
                    model.addAttribute("name", moderatorDetails.getName());
                    return "/moderation/menu";
                });
    }

    @GetMapping("/profile/{name}")
    public Mono<String> profile(@PathVariable("name") String name, final Model model) {
        return Mono.from(moderatorService.findByName(name))
                .flatMap(moderator -> {
                    model.addAttribute("moderator", moderator);
                    return roleService.findById(moderator.getId());
                })
                .flatMap(role -> {
                    model.addAttribute("role", role);
                    return ReactiveSecurityContextHolder.getContext();
                })
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(ModeratorDetailsService.ModeratorDetails.class::cast)
                .map(moderatorDetails -> {
                    model.addAttribute("me", moderatorDetails.getUsername().equals(name));
                    return "/moderation/profile";
                });
    }

    @GetMapping("/subject")
    public Mono<String> subject(final Model model){
        return Mono.from(subjectService.findAll())
                .map(subjectFlux->{
                    model.addAttribute("subjects",subjectFlux);
                    return "/moderation/subject";
                });
    }

    @GetMapping("/subject/add")
    public String subjectAdd(){
        return "/moderation/subject-add";
    }

    @SuppressWarnings("MVCPathVariableInspection")
    @GetMapping("/{ignore}")
    public String redirectNotFoundUrl(){
        return "redirect:/moderation/menu";
    }

}
