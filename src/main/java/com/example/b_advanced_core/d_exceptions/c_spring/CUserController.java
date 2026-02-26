package com.example.b_advanced_core.d_exceptions.c_spring;

/**
 * Контроллер
 */
//@RestController
public class CUserController {
    private final BUserService BUserService;

    public CUserController(BUserService BUserService) {
        this.BUserService = BUserService;
    }

//    @GetMapping("/users/{id}")
    public String getUser(
//            @PathVariable
            Long id
    ) {
        return BUserService.findUserName(id);
    }
}
