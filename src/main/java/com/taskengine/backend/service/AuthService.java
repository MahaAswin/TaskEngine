package com.taskengine.backend.service;

import com.taskengine.backend.dto.*;

public interface AuthService {

  AuthResponse register(RegisterRequest request);

  AuthResponse login(AuthRequest request);

  AuthResponse refresh(RefreshRequest request);

  AuthResponse refreshWithToken(String refreshToken);

  void logout(LogoutRequest request);

  void logoutWithToken(String refreshToken);

  AuthResponse oauthCallback(OAuth2CallbackRequest request);
}
