package com.example.buyfast.modules.user.repository;

import com.example.buyfast.modules.user.model.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;

/**
 * NEW REPOSITORY INTERFACE
 * Required to correctly create, update, and delete profile data.
 */
@Mapper
public interface UserProfileRepo {

    Optional<UserProfile> findByUserId(Long userId);

    void create(UserProfile userProfile);

    void update(UserProfile userProfile);

    void deleteByUserId(Long userId);
}