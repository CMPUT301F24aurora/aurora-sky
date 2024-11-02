public class FacilityProfileActivity extends AppCompatActivity {

    private EditText nameField, timeField, locationField, emailField;
    private Button editButton, removeButton;
    private Facility facility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility_profile);

       
        nameField = findViewById(R.id.name_field);
        timeField = findViewById(R.id.time_field);
        locationField = findViewById(R.id.location_field);
        emailField = findViewById(R.id.email_field);
        editButton = findViewById(R.id.edit_button);
        removeButton = findViewById(R.id.remove_button);

       
        facility = (Facility) getIntent().getSerializableExtra("facility_data");
        
       
        if (facility != null) {
            nameField.setText(facility.getName());
            timeField.setText(facility.getTime());
            locationField.setText(facility.getLocation());
            emailField.setText(facility.getEmail());
        }

       
        editButton.setOnClickListener(v -> {
            facility.setName(nameField.getText().toString());
            facility.setTime(timeField.getText().toString());
            facility.setLocation(locationField.getText().toString());
            facility.setEmail(emailField.getText().toString());

            facility.updateInFirestore(new SaveFacilityCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(FacilityProfileActivity.this, "Facility updated successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(FacilityProfileActivity.this, "Failed to update facility", Toast.LENGTH_SHORT).show();
                }
            });
        });

       
        removeButton.setOnClickListener(v -> {
            facility.deleteFromFirestore(new DeleteFacilityCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(FacilityProfileActivity.this, "Facility removed successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity after deletion
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(FacilityProfileActivity.this, "Failed to remove facility", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
