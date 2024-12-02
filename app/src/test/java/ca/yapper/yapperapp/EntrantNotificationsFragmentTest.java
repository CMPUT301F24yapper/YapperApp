package ca.yapper.yapperapp;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;

import ca.yapper.yapperapp.EntrantFragments.EntrantNotificationsFragment;
import ca.yapper.yapperapp.UMLClasses.Notification;

import static org.mockito.Mockito.*;

public class EntrantNotificationsFragmentTest {

    private EntrantNotificationsFragment fragment;
    private View mockNoNotificationsLayout;
    private RecyclerView mockRecyclerView;
    private View mockRootView;

    @Before
    public void setUp() {
        // Create a spy for the fragment
        fragment = Mockito.spy(new EntrantNotificationsFragment());

        // Mock child views
        mockRootView = Mockito.mock(View.class);
        mockNoNotificationsLayout = Mockito.mock(View.class);
        mockRecyclerView = Mockito.mock(RecyclerView.class);

        Mockito.when(mockRootView.findViewById(R.id.no_notifications_layout)).thenReturn(mockNoNotificationsLayout);
        Mockito.when(mockRootView.findViewById(R.id.notifications_recycler_view)).thenReturn(mockRecyclerView);


        // Mock fragment's getView() and findViewById()
        doReturn(mockNoNotificationsLayout).when(fragment).getView().findViewById(R.id.no_notifications_layout);
        doReturn(mockRecyclerView).when(fragment).getView().findViewById(R.id.notifications_recycler_view);
    }

    @Test
    public void testToggleEmptyState_EmptyList() {
        // Arrange
        fragment.notificationList = new ArrayList<>(); // Clear the notification list

        // Act
        fragment.toggleEmptyState();

        // Assert
        verify(mockNoNotificationsLayout).setVisibility(View.VISIBLE);
        verify(mockRecyclerView).setVisibility(View.GONE);
    }

    @Test
    public void testToggleEmptyState_NonEmptyList() {
        // Arrange
        fragment.notificationList = new ArrayList<>();
        fragment.notificationList.add(new Notification()); // Add a mock notification

        // Act
        fragment.toggleEmptyState();

        // Assert
        verify(mockNoNotificationsLayout).setVisibility(View.GONE);
        verify(mockRecyclerView).setVisibility(View.VISIBLE);
    }
}