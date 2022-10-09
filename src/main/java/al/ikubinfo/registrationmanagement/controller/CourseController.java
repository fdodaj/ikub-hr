package al.ikubinfo.registrationmanagement.controller;

import al.ikubinfo.registrationmanagement.dto.CourseDto;
import al.ikubinfo.registrationmanagement.dto.CourseUserDto;
import al.ikubinfo.registrationmanagement.dto.UserDto;
import al.ikubinfo.registrationmanagement.dto.ValidatedCourseDto;
import al.ikubinfo.registrationmanagement.repository.criteria.CourseCriteria;
import al.ikubinfo.registrationmanagement.repository.criteria.UserCriteria;
import al.ikubinfo.registrationmanagement.service.CourseService;
import al.ikubinfo.registrationmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class CourseController {
    private static final String REDIRECT_TO_HOMEPAGE_URL = "redirect:/courses";
    private static final String COURSES = "courses";
    private static final String COURSE = "course";

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    /**
     * Get all courses. if criteria is applied, courses are filter accordingly
     *
     * @param criteria filter object
     * @return ModelAndView -> courses filtered list
     */
    @GetMapping("/courses")
    public ModelAndView listCourses(@Valid CourseCriteria criteria) {
        Page<CourseDto> courseDtos = courseService.filterCourses(criteria);
        ModelAndView mv = new ModelAndView(COURSES);
        mv.addObject(COURSES, courseDtos);
        return mv;
    }

    /**
     * Retrieve course details
     *
     * @param id course id
     * @return ModelAndView with course details
     */
    @GetMapping("/course/{id}")
    public ModelAndView getCourseById(@Valid @PathVariable Long id) {
        ModelAndView mv = new ModelAndView("course_details");
        mv.addObject(COURSE, courseService.getCourseById(id));
        mv.addObject("users", courseService.getAllStudentsByCourseId(id));
        mv.addObject("userList", userService.filterUsers(new UserCriteria()));
        return mv;
    }

    /**
     * Retrieve course edition view
     *
     * @param id course id
     * @return ModelAndView
     */
    @GetMapping("/courses/edit/{id}")
    public ModelAndView updateCourseView(@Valid @PathVariable("id") Long id) {
        CourseDto courseDto = courseService.getCourseById(id);
        ModelAndView mv = new ModelAndView("edit_course");
        mv.addObject(COURSE, courseDto);
        return mv;
    }

    /**
     * Update course
     *
     * @param id        course id
     * @param courseDto courseDto
     * @return ModelAndView
     */
    @PostMapping("/course/{id}")
    public ModelAndView updateCourse(@PathVariable Long id, @ModelAttribute("course") @Valid ValidatedCourseDto courseDto) {
        courseService.updateCourse(courseDto);
        return new ModelAndView(REDIRECT_TO_HOMEPAGE_URL);
    }

    /**
     * Delete course (soft deletion)
     *
     * @param courseId course id
     * @return ModelAndView
     */
    @GetMapping("/courses/delete/{id}")
    public ModelAndView deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourseById(courseId);
        return new ModelAndView(REDIRECT_TO_HOMEPAGE_URL);
    }
    /**
     * Remove user from course
     *
     * @param courseId  courseId
     * @param studentId userId
     * @return ModelAndView
     */
    @PutMapping("/users/{courseId}/{studentId}")
    ResponseEntity<Void> removeUserFromCourse(@PathVariable Long courseId, @PathVariable Long studentId) {
        courseService.removeUserFromCourse(studentId, courseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve user assign to course view
     *
     * @param userId user id
     * @return ModelAndView
     */
    @GetMapping("/course/assign/{userId}")
    public ModelAndView assignCourseView(@PathVariable("userId") Long userId, CourseUserDto courseUserDto) {
        ModelAndView mv = new ModelAndView("assign_course_to_user");
        UserDto user = userService.getUserById(userId);
        courseUserDto.setUserId(user.getId());
        mv.addObject("courseUserDto", courseUserDto);
        mv.addObject(COURSES, courseService.filterCourses(new CourseCriteria()));
        return mv;
    }


    /**
     * Assigns to course
     *
     * @return ModelAndView
     */
    @PostMapping("/courses/assign-user")
    public ModelAndView assignUserToCourse(CourseUserDto courseUserDto) {
        ModelAndView modelAndView = new ModelAndView("assign_course_to_user");
        modelAndView.addObject("courseUserDto", courseUserDto);
        courseService.assignUserToCourse(courseUserDto);
        return modelAndView;
    }

    /**
     * Retrieve form of course creation
     *
     * @param course courseDto
     * @return ModelAndView
     */
    @GetMapping("/course/new")
    public ModelAndView retrieveNewCourseView(@Valid CourseDto course, BindingResult result) {
        ModelAndView mv = new ModelAndView("create_course");
        mv.addObject(COURSE, course);
        return mv;
    }

    /**
     * Save new course
     *
     * @param course CourseDto
     * @return ModelAndView
     */
    @PostMapping("/courses")
    public ModelAndView saveCourse(@Valid @ModelAttribute("course") ValidatedCourseDto course, BindingResult result, Model model) {
        model.addAttribute(COURSE, course);
        if (result.hasErrors()) {
            ModelAndView mv = new ModelAndView("create_course");
            mv.addObject(COURSE, course);
            return mv;
        }
        courseService.saveCourse(course);
        return new ModelAndView(REDIRECT_TO_HOMEPAGE_URL);
    }

}
