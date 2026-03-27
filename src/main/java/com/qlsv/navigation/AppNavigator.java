package com.qlsv.navigation;

import com.qlsv.model.User;

public interface AppNavigator {

    void showLogin();

    void showDashboard(User user);
}
