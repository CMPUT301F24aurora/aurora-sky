package com.example.lotteryapp;

import android.content.Context;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AdminTest {

    @Mock
    private Context mockContext;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private DocumentReference mockDocumentReference;

    private Admin admin;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        admin = new Admin(mockContext);
    }

    @Test
    public void testRemoveEventSuccess() {
        // Mock Firestore document reference and success behavior
        when(mockFirestore.collection("events").document("eventId")).thenReturn(mockDocumentReference);

        doAnswer(invocation -> {
            ((OnSuccessListener<Void>) invocation.getArgument(0)).onSuccess(null);
            return null;
        }).when(mockDocumentReference).delete();

        admin.removeEvent("eventId");

        // Verify that the success Toast message is shown
        verify(mockContext).getString(eq(R.string.event_deleted_successfully));
        verify(mockDocumentReference).delete();
    }

    @Test
    public void testRemoveEventFailure() {
        // Mock Firestore document reference and failure behavior
        when(mockFirestore.collection("events").document("eventId")).thenReturn(mockDocumentReference);

        doAnswer(invocation -> {
            ((OnFailureListener) invocation.getArgument(0)).onFailure(new Exception("Error deleting event"));
            return null;
        }).when(mockDocumentReference).delete();

        admin.removeEvent("eventId");

        // Verify that the failure Toast message is shown
        verify(mockContext).getString(eq(R.string.error_deleting_event));
        verify(mockDocumentReference).delete();
    }
}