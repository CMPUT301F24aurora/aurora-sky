# Entrant

### Responsibilities
- Join and leave waiting lists for specific events.
- Scan QR codes to view event details and join waiting lists.
- Provide and update personal information (name, email, optional phone number).
- Upload and remove profile pictures (default picture automatically generated).
- Receive notifications about lottery results (selected or not selected).
- Accept or decline invitations to sign up for events.
- Opt out of receiving notifications from organizers and admins.
- Be identified by the device (bypass username/password).
- Acknowledge geolocation requirements before joining a waiting list.

### Collaborators
- WaitingList
- Event
- GeolocationService
- NotificationService
- Organizer

---

# Organizer

### Responsibilities
- Create and manage events (name, dates, capacity, posters).
- Generate and store unique QR codes for each event.
- Manage waiting lists (view entrants, select participants, resample if necessary).
- Send notifications (sign-ups, reminders, cancellations).
- Set and manage event settings, including optional geolocation requirements.

### Collaborators
- Event
- Entrants
- WaitingList
- NotificationService
- GeolocationService

---

# Admin

### Responsibilities
- Remove events, profiles, images, and hashed QR code data.
- Browse events, profiles, and images.
- Enforce policies by removing facilities that violate guidelines.

### Collaborators
- Entrants
- Organizer
- Events
- Facility

---

# Event

### Responsibilities
- Store and manage event information (name, dates, capacity).
- Store event poster images.
- Generate and store unique QR codes for each event.
- Handle waiting lists of entrants and manage sign-ups/cancellations.
- Notify selected entrants about their status.
- Resample entrants when someone declines an invitation.

### Collaborators
- WaitingList
- Organizer
- Entrant
- NotificationService

---

# Waiting List

### Responsibilities
- Record all entrants who joined an event's waiting list.
- Allow organizers to manage entrants (select, replace, cancel).

### Collaborators
- Entrant
- Event
- GeolocationService

---

# Geolocation Service

### Responsibilities
- Handle geolocation verification for entrants joining waiting lists.
- Provide location data to organizers to visualize where entrants are joining from.

### Collaborators
- Entrant
- Organizer

---

# Facility

### Responsibilities
- Store and update facility details (profile, location).
- Manage events hosted by the facility.

### Collaborators
- Event
- Organizer

---

# Notification Service

### Responsibilities
- Send notifications to entrants about their selection status (chosen or not).
- Manage opt-in/opt-out preferences for notifications.

### Collaborators
- Entrant
- Organizer
