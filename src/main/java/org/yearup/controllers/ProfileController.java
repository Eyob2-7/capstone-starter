package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@RequestMapping("/profile")
@PreAuthorize("hasRole('USER')")
@CrossOrigin
public class ProfileController {

    private ProfileDao profileDao;
    private UserDao userDao;

    // Constructor injection
    public ProfileController(ProfileDao profileDao, UserDao userDao) {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    // Get /profile returns the profile for the currently logged-in user
    @GetMapping
    public Profile getProfile(Principal principal) {
        try {
            // Get username from authenticated session
            String username = principal.getName();

            // Retrieve the user and their profile using the username
            User user = userDao.getByUserName(username);
            return profileDao.getByUserId(user.getId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
        }
    }

    @PutMapping
    public Profile updateProfile(@RequestBody Profile profile, Principal principal) {
        try {
            // Get username from authenticated session
            String username = principal.getName();

            // Retrieve the user and set the userId in the profile object
            User user = userDao.getByUserName(username);
            profile.setUserId(user.getId());

            // Update the profile in the database
            profileDao.update(profile);
            return profileDao.getByUserId(user.getId());

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to update profile");
        }
    }
}
