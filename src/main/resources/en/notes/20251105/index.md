# Case Study: Meeting Room Schedule Signage at the Girasol Office Intercom

This is a deployment example of tablet-based signage placed beside the intercom at Girasol Energy Corporation’s office, showing the same-day meeting room booking timetable.

<img alt="Photo of the installed tablet" src="image.png" height="400">

# Table of Contents

- dummy
{:toc}

# Deliverable

Signage that displays real-time meeting room booking status using Google Calendar.

## Installation Location

- Girasol Energy Corporation
- Entrepreneur Plaza 5F Head Office
- Wall next to the intercom

## Benefits

Previously, staff had to call out inside Office 501 to identify the visitor’s contact person. With immediate visibility of each meeting room’s reservation status, they can now quickly decide which floor to guide the visitor to.

# Components Used

- Xiaomi Android tablet
- Holder for wall mounting
- Google account for installation
- Production version of KanbanBro

# Procedure

## Purchase

Obtain the following product.

[Redmi-8-7インチディスプレイ-最大2TB拡張-低ブルーライト-最大輝度600nit](https://www.amazon.co.jp/dp/B0D97P58SS?th=1)

<img alt="Purchased tablet" src="img.png" width="400">

## Initial Device Setup

Perform the general initial setup of the tablet.

<img alt="Tablet right after boot" src="img_1.png" height="400">

- Language: Japanese / Japan region
- Network: Configure as required for the environment
- Copy apps & data: Do not copy
- Google sign-in: Sign in with the Google account used on this device
- Google services: Leave all defaults
- Google services – Device backup: Turn backup ON
- Set screen lock: Skip
- Access Assistant with "OK Google": Skip
- Access Assistant without unlocking: Skip
- Continue additional setup?: No
- Basic settings: Leave defaults
- Set password: Skip
- Continue additional setup?: No

---

Then apply all available system updates.

## Install KanbanBro

Search Google Play for `KanbanBro`.

<img alt="KanbanBro search screen" src="image-1.png" width="400">

---

Install the paid or trial version of KanbanBro using the prescribed method.

<img alt="KanbanBro installed" src="image-2.png" width="400">

## Additional Device Settings

Configure device settings so the KanbanBro screen remains constantly displayed.

<img alt="Android settings button" src="image-5.png" width="400">

<img alt="Android settings screen" src="image-6.png" width="400">

---

- Lock screen > Sleep: Never
- Apps > KanbanBro – Signage Browser > Battery > Battery saver: Unrestricted
- Apps > KanbanBro – Signage Browser > Permissions > Pause app activity if unused: Off
- Apps > KanbanBro – Signage Browser > Additional permissions > Open new windows while running in background: Always allow

## Configure KanbanBro

When KanbanBro launches, its help page is shown.

<img alt="First KanbanBro launch screen" src="image-3.png" width="400">

---

Use the device’s Back action to open/close the KanbanBro menu.

<img alt="KanbanBro menu" src="image-4.png" width="400">

### Device Behavior Settings via Bottom Buttons

From the bottom buttons, adjust settings to match the following.

<img alt="Configured bottom buttons" src="image-8.png" width="400">

---

- Auto reload: On, every 10 minutes
- Auto app restart: On, every 1 hour
- Auto app recovery: On, after 1 minute

When enabling Auto App Recovery, grant the “Display over other apps” permission when prompted.

## Display Google Calendar

### Restrict Tabs to Only Google Calendar

In KanbanBro, keep only the following Google Calendar URL open:

- https://calendar.google.com/calendar/u/0/r/day

Close the default tabs.

### Sign in to the Google Account

Sign in to the Google account when prompted, using the standard method.

### Adjust Tab Display Size

Open the viewport mode settings from the bottom buttons.

<img alt="Viewport mode settings" src="image-9.png" width="400">

---

- Override tab settings: On
- Zoom factor: e.g. 150%

---

Close the viewport mode settings to apply the calendar display size.

<img alt="Google Calendar" src="image-7.png" height="400">

## Wall Mounting

TODO
