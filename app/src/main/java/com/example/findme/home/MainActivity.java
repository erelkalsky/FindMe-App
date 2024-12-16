package com.example.findme.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.findme.R;
import com.example.findme.classes.api.Database;
import com.example.findme.home.cases.CasesFragment;
import com.example.findme.home.settings.SettingsFragment;
import com.example.findme.home.users.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.ListenerRegistration;

public class MainActivity extends AppCompatActivity implements Database.UserUpdateListener {

    BottomNavigationView bottomNavigationView;
    FrameLayout frameLayout;
    int currentFragmentId = R.id.menuHome;
    ListenerRegistration userUpdateListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frameLayout = findViewById(R.id.frameLayout);
        bottomNavigationView = findViewById(R.id.menu);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if(itemId == currentFragmentId) {
                    return false;
                }

                if(itemId == R.id.menuHome) {
                    loadFragment(new HomeFragment());
                    currentFragmentId = R.id.menuHome;
                } else if(itemId == R.id.menuCases) {
                    loadFragment(new CasesFragment());
                    currentFragmentId = R.id.menuCases;
                } else if(itemId == R.id.menuUsers) {
                    loadFragment(new UsersFragment());
                    currentFragmentId = R.id.menuUsers;
                } else if(itemId == R.id.menuSettings) {
                    loadFragment(new SettingsFragment());
                    currentFragmentId = R.id.menuSettings;
                }

                return true;
            }
        });

        userUpdateListenerRegistration = Database.listenForUserUpdates(this);
        loadFragment(new HomeFragment());
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onUserUpdated() {
        FrameLayout frameLayout = findViewById(R.id.frameLayout);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(frameLayout.getId());

        if(Database.user.getRole() == 1) {
            resetBottomMenu();
            bottomNavigationView.getMenu().removeItem(R.id.menuUsers);
        } else {
            resetBottomMenu();
        }

        if (currentFragment instanceof HomeFragment) {
            bottomNavigationView.getMenu().findItem(R.id.menuHome).setChecked(true);
            MenuItem menuItem = bottomNavigationView.getMenu().findItem(R.id.menuHome);
            if (menuItem != null) {
                menuItem.setChecked(true);
            } else {
                loadFragment(new HomeFragment());
            }

            ((HomeFragment) currentFragment).updateUi();

        } else if (currentFragment instanceof CasesFragment) {
            MenuItem menuItem = bottomNavigationView.getMenu().findItem(R.id.menuCases);
            if (menuItem != null) {
                menuItem.setChecked(true);
            } else {
                loadFragment(new HomeFragment());
            }

            ((CasesFragment) currentFragment).updateUi();

        } else if (currentFragment instanceof UsersFragment) {
            MenuItem menuItem = bottomNavigationView.getMenu().findItem(R.id.menuUsers);
            if (menuItem != null) {
                menuItem.setChecked(true);
            } else {
                loadFragment(new HomeFragment());
            }

        } else if (currentFragment instanceof SettingsFragment) {
            MenuItem menuItem = bottomNavigationView.getMenu().findItem(R.id.menuSettings);
            if (menuItem != null) {
                menuItem.setChecked(true);
            } else {
                loadFragment(new HomeFragment());
            }


            ((SettingsFragment) currentFragment).updateUi();
        }
    }

    private void resetBottomMenu() {
        bottomNavigationView.getMenu().clear();
        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
    }

    @Override
    public void onUserDeleted() {
        Database.logout(MainActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (userUpdateListenerRegistration != null) {
            userUpdateListenerRegistration.remove();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Add listener for user updates again when the activity is started
        userUpdateListenerRegistration = Database.listenForUserUpdates(this);
    }
}