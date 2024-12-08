package com.example.lotteryapp;

import android.content.Context;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Admin class.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28}, manifest = Config.NONE)
public class AdminTest {

    @Mock
    private Context mockContext;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockCollection;

    @Mock
    private DocumentReference mockDocumentReference;

    @Mock
    private Task<Void> mockTask;

    private Admin admin;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        admin = new Admin(mockContext, mockFirestore);
    }

    @Test
    public void testRemoveEventSuccess() {
        String eventId = "eventId";

        // Mock Firestore interactions
        when(mockFirestore.collection("events")).thenReturn(mockCollection);
        when(mockCollection.document(eventId)).thenReturn(mockDocumentReference);
        when(mockDocumentReference.delete()).thenReturn(mockTask);

        // Mock Task behavior for success
        doAnswer(invocation -> {
            OnSuccessListener<Void> successListener = invocation.getArgument(0);
            successListener.onSuccess(null);
            return mockTask;
        }).when(mockTask).addOnSuccessListener(any(OnSuccessListener.class));

        // Mock Task behavior for failure (do nothing)
        doNothing().when(mockTask).addOnFailureListener(any(OnFailureListener.class));

        // Mock Context's getString method
        when(mockContext.getString(R.string.event_deleted_successfully)).thenReturn("Event deleted successfully");

        // Execute the method under test
        admin.removeEvent(eventId);

        // Verify interactions
        verify(mockFirestore).collection("events");
        verify(mockCollection).document(eventId);
        verify(mockDocumentReference).delete();
        verify(mockTask).addOnSuccessListener(any(OnSuccessListener.class));
        verify(mockContext).getString(R.string.event_deleted_successfully);
    }

    @Test
    public void testRemoveEventFailure() {
        String eventId = "eventId";

        // Mock Firestore interactions
        when(mockFirestore.collection("events")).thenReturn(mockCollection);
        when(mockCollection.document(eventId)).thenReturn(mockDocumentReference);
        when(mockDocumentReference.delete()).thenReturn(mockTask);

        // Mock Task behavior for failure
        doAnswer(invocation -> {
            OnFailureListener failureListener = invocation.getArgument(0);
            failureListener.onFailure(new Exception("Error deleting event"));
            return mockTask;
        }).when(mockTask).addOnFailureListener(any(OnFailureListener.class));

        // Mock Task behavior for success (do nothing)
        doNothing().when(mockTask).addOnSuccessListener(any(OnSuccessListener.class));

        // Mock Context's getString method
        when(mockContext.getString(R.string.error_deleting_event)).thenReturn("Error deleting event!");

        // Execute the method under test
        admin.removeEvent(eventId);

        // Verify interactions
        verify(mockFirestore).collection("events");
        verify(mockCollection).document(eventId);
        verify(mockDocumentReference).delete();
        verify(mockTask).addOnFailureListener(any(OnFailureListener.class));
        verify(mockContext).getString(R.string.error_deleting_event);
    }
}
