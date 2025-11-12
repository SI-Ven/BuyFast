package com.example.buyfast.modules.user.repository;

import com.example.buyfast.modules.user.model.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;

@Mapper
public interface UserProfileRepo {

    void insertProfile(UserProfile userProfile);

    Optional<UserProfile> findByUserId(@Param("userId") Long userId);

    void updateProfile(UserProfile userProfile);
}