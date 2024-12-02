package ca.yapper.yapperapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import ca.yapper.yapperapp.Activities.AdminActivity;
import ca.yapper.yapperapp.Activities.EntrantActivity;
import ca.yapper.yapperapp.R;

public class AdminActivityTest {
    private AdminActivity adminActivity;
    private Context mockContext;

    @Before
    public void setUp() {
        mockContext = Mockito.mock(Context.class);
        adminActivity = Mockito.spy(new AdminActivity());
        doReturn(mockContext).when(adminActivity).getApplicationContext(); // Mock application context
    }

    @Test
    public void testShowProfileSwitchMenu_switchToEntrant() {
        // Arrange
        View mockView = Mockito.mock(View.class);
        MenuItem mockMenuItem = Mockito.mock(MenuItem.class);
        when(mockMenuItem.getItemId()).thenReturn(R.id.switch_to_entrant);

        Intent expectedIntent = new Intent(mockContext, EntrantActivity.class);

        PopupMenu popupMenu = Mockito.mock(PopupMenu.class);
        doAnswer(invocation -> {
            PopupMenu.OnMenuItemClickListener listener = invocation.getArgument(0);
            listener.onMenuItemClick(mockMenuItem);
            return null;
        }).when(popupMenu).setOnMenuItemClickListener(any());

        // Act
        adminActivity.showProfileSwitchMenu(mockView);

        // Assert
        verify(adminActivity).startActivity(eq(expectedIntent));
    }
}