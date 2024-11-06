package com.example.lotteryapp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowProcess;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.CompletableFuture;

public class EventTest {

    @Mock
    FirebaseFirestore mockFirestore;
    @Mock
    CollectionReference mockCollection;
    @Mock
    DocumentReference mockDocument;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockFirestore.collection("events")).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
    }

    @Test
    public void testSaveEventToFirestore() {
        Event event = new Event("Dance Class", "21/10/2024", 40, "Dancey Dance");

        doAnswer(invocation -> {
            SaveEventCallback callback = invocation.getArgument(0);
            callback.onSuccess("mockDocumentId");
            return null;
        }).when(mockDocument).set(any(Event.class));

        event.saveToFirestore(new SaveEventCallback() {
            @Override
            public void onSuccess(String documentId) {
                assertNotNull(documentId);
                String QrHash = event.generateQRHash();
                assertNotNull(QrHash);

                System.out.println("Event saved successfully with Document ID: " + documentId);
                System.out.println("Event created successfully with QR Hash: " + QrHash);
            }

            @Override
            public void onFailure(Exception e) {
                fail("Failed to save event: " + e.getMessage());
            }
        });

        verify(mockDocument).set(event);
    }
}

