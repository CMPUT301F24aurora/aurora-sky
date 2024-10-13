# Entrant
### Responsiblities
- Join and leave waiting lists for specific events.
- Scan QR codes to view event details and join waiting lists.
- Provide and update personal information (name, email, optional phone number).
- Upload and remove profile pictures (default picture automatically generated)
- Receive notifications about lottery results (selected or not selected)
- Accept or decline invitations to sign up for events.
- Opt out of receiving notifications from organizers and admins.
- Be identified by the device (bypass username/password).
- Acknowledge geolocation requirements before joining a waiting list.
### Collaborators
- Waiting List
- Event
- Geolocation Service
- Notification Service

# Organizer
### Responsibilities
- Create new events with specific details (name, dates, capacity).
- Generate and store unique QR codes for each event.
- View, update, and manage event details (including uploading posters).
- Manage waiting lists: view entrants, select participants, draw replacements.
- Send notifications to entrants regarding sign-ups, reminders, and cancellations.
- Optionally set limits on waiting list size.
- Enable or disable geolocation requirements for events.

### Collaborators
- Events
- Entrants
- Waiting List
- Notification Service
- Geolocation Service

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

# Event
### Responsibilities
- Store and manage event information (name, dates, capacity).
- Store event poster images
- Generate and store unique QR codes for each event.
- Handle waiting lists of entrants and manage sign-ups/cancellations.
- Notify selected entrants about their status.
- Resample entrants when someone declines an invitation.
  
### Collaborators
- WaitingList
- Organizer
- Entrant
- NotificationService

# WaitingList
### Responsibilities:
- Record all entrants who joined an event's waiting list.
- Allow organizers to manage entrants (select, replace, cancel).

### Collaborators:
- Entrant
- Event

