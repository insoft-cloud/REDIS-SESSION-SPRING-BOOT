package com.insoft.springboot.redis.session2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 *
 * @author hrjin
 * @version 1.0
 * @since 2021.07.02
 **/
@Controller
public class SampleController {
    private static final String KEY = "name";
    private static final String VALUE = "piglet";

    /**
     * 메인 화면
     *
     * @return ModelAndView
     */
    @GetMapping("/")
    public ModelAndView main(HttpSession session) {
        if (!VALUE.equals(session.getAttribute(KEY))) {
            session.setAttribute(KEY, VALUE);
        }

        ModelAndView mv = new ModelAndView();
        mv.addObject("sessionId", session.getId());
        mv.addObject(KEY, session.getAttribute(KEY));
        mv.setViewName("main");

        return mv;
    }


}
