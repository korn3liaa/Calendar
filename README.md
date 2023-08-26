# Calendar

 The aim of the application is to find a time interval during the day to perform duties or set goals, e. g. reading a book for 30 minutes a day. In a word, it is meant to help you organize your free time. The algorithm searches for free time slots during the day, taking into account working hours or sleeping time, where these hours are set by the user in the "Settings" panel. There is also an option to set your own activity, e. g. scheduled training from 18:00 to 19:00. When searching for free slots, activities added manually by the user are taken into account so that they do not overlap. If the duration od the destinantion is too large the user will be imformed to split the destination to smaller parts. The search starts from the current date, if there is no more free time on a given day, the next day is searched. All activities are saved in realtime database. There is a possibility to create a few users.

# It is adviced to test the application on following user:
 login:     aaa@gmail.com
 password:  aaa111

# Tips how to use the appliaction:
 - in "Add new destination" panel the time must be passed in minutes
 - in "Add new activity" panel the start time and the stop time should be passed in following format "HH:MM"
 - description is optional

# Possible ways to improve the application:
- possibility to set a weekly activities like the theme group meetings
- make the title and time od the activities, destinantion mandatory
- set the system notification which will be a reminder eg. 10 min before the acivity should started
- add the option to set the time of that reminder
- add the option of localization where the activity should have place and count time time needed to get there 
