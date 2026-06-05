package kc.ar.ajou.mseserver.service;

import java.util.Map;

import kc.ar.ajou.mseserver.web.dto.UserResponse;

public interface UserProfileService {

	UserResponse getProfile(String userId);

	UserResponse updateProfile(String userId, Map<Integer, Integer> gameProgress);
}
