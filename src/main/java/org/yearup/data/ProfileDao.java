package org.yearup.data;


import org.yearup.models.Profile;

public interface ProfileDao {
    Profile create(Profile profile);

    // Get the profile data for a specific user
    Profile getByUserId(int userId);

    // Update the profile for a specific user
    void update(Profile profile);
}
